var marmoset = marmoset || {};


/**
 * @class Wraps a select tag for displaying a dropdown rubric.
 *
 * @author rwsims
 *
 * @constructor
 * @param {object} select the select tag to wrap.
 * @param {object} [hidden] the hidden input for storing the value string.
 */
marmoset.DropdownWidget = function(select, hidden) {
    this.$select = $(select);
    this.$hidden = $(hidden);
    this.valueMap = {};

    var thisWidget = this;
    this.$select.change(function(event) {
        var value = thisWidget.$select.val(),
            score = thisWidget.valueMap[value];
        $(thisWidget).trigger({
            type: 'change',
            value: value,
            score: score
        });
    });
};

/** 
 * Return the values formatted for sending as a parameter.
 *
 * @return {string} The value string. Keys and values are separated by ':',
 * pairs are separated by ','.
 */
marmoset.DropdownWidget.prototype.getValueString = function() {
    return $.map(this.valueMap, function(score, value) {
        return value + ":" + score;
    }).join(",");
};

$.template("optionTemplate",
           '<option value="{{=value}}">{{=value}} [{{=score}}]</option>');

/**
 * Redraw the select tag, provinding an option tag for each key/value pair.
 */
marmoset.DropdownWidget.prototype.redraw = function() {
    this.$select.empty();
    if (this.$hidden) {
        this.$hidden.val(this.getValueString());
    }
    if (!this.valueMap) {
        return;
    }
    var opts = $.map(this.valueMap, function(score, value) {
        return $.render({
            value: value,
            score: score
        }, 'optionTemplate');
    });
    this.$select.append(opts.join(''));
};

/**
 * Set a score for a value. If the value doesn't exist, it will be added,
 * otherwise it will be overwritten. Also redraws the widget.
 */
marmoset.DropdownWidget.prototype.put = function(name, score) {
    this.valueMap[name] = score;
    this.redraw();
    this.$select.val(name);
};

/**
 * Set the scores & values to be the same as the given map. Redraws the widget.
 *
 * @param {Map<String, String>} map of values to scores.
 */
marmoset.DropdownWidget.prototype.setValues = function(valueMap) {
    this.clear();
    var thisValueMap = this.valueMap;
    $.each(valueMap, function(value, score) {
        thisValueMap[value] = score;
    });
    this.redraw();
};

/**
 * Removes a value from the dropdown. Has no effect if the value does not exist
 * in the map. Redraws the widget.
 */
marmoset.DropdownWidget.prototype.remove = function(name) {
    delete this.valueMap[name];
    this.redraw();
};

/**
 * Removes all values from the map and redraws the widget.
 */
marmoset.DropdownWidget.prototype.clear = function() {
    this.valueMap = {};
    this.redraw();
};


/**
 * @class Edits dropdown rubric items.
 *
 * @author rwsims
 *
 * @constructor
 * @param {string} dialogId the id of the div with the dialog markup.
 */
marmoset.DropdownEditor = function(dialogId) {
    var editor = this;
    this.dialog = $(dialogId).dialog({
        autoOpen: false,
        modal: true,
        buttons: {
            "OK": function() {
                $(this).dialog("close");
                editor._save();
                editor.clearAndClose();
            },
            "Cancel": function() {
                $(this).dialog("close");
                editor.clearAndClose();
            }
        }
    });
    this.select = this.dialog.find(dialogId + "-dropdown-select");
    this.widget = new marmoset.DropdownWidget(this.select);

    this.valueInput = this.dialog.find(dialogId + "-value-input");
    this.scoreInput = this.dialog.find(dialogId + "-score-input");
    this.scoreInput.keypress(function(event) {
        if (event.which == 13) {
            event.preventDefault();
            editor._add();
        }
    });

    $(this.widget).change(function(event) {
        editor.valueInput.val(event.value);
        editor.scoreInput.val(event.score);
        editor.scoreInput.select();
    });

    this.dialog.find(dialogId + "-controls").buttonset();
    this.dialog.find(dialogId + "-add").click(function(event) {
        editor._add();
    });
    this.dialog.find(dialogId + "-delete").click(function(event) {
        var value = editor.valueInput.val();
        if (value) {
            editor.widget.remove(value);
        }
        editor.valueInput.val('');
        editor.scoreInput.val('');
        editor.valueInput.select();
    });
    this.dialog.find(dialogId + "-clear-all").click(function(event) {
        editor.widget.clear();
        editor.valueInput.val('');
        editor.scoreInput.val('');
        editor.valueInput.select();
    });
}

marmoset.DropdownEditor.prototype._add = function() {
    var value = this.valueInput.val(),
        score = this.scoreInput.val();
    if (value && score) {
        this.widget.put(value, score);
    }
    this.valueInput.select();
}

/**
 * Clear the inputs and close the dialog. Also disassociates the editor from the
 * dropdown widget being edited.
 */
marmoset.DropdownEditor.prototype.clearAndClose = function() {
    this.dialog.dialog("close");
    this.widget.clear();
    this.valueInput.val('');
    this.scoreInput.val('');
    delete this.currentWidget;
};

/**
 * Edit a dropdown widget.
 *
 * @param {marmoset.DropdownWidget} the widget to edit.
 */
marmoset.DropdownEditor.prototype.edit = function(widget) {
    this.dialog.dialog("open");
    this.currentWidget = widget;
    this.widget.setValues(widget.valueMap);
};

marmoset.DropdownEditor.prototype._save = function() {
    if (!this.currentWidget) {
        return;
    }
    this.currentWidget.setValues(this.widget.valueMap);
};


/**
 * @class Manages the dynamic list of rubrics in a table of rubrics.
 *
 * @author rwsims
 *
 * @constructor
 * @param {string} rubricTableId the id of the rubric table.
 * @param {DropdownEditor} dropdownEditor dropdown editor instance.
 */
marmoset.RubricManager = function(rubricTableId, dropdownEditor) {
    this.table = $(rubricTableId);
    this.dropdownEditor = dropdownEditor;
    this.rubricCount = 0;

    this.templates = {
        rubric: $("#rubricTemplate"),
        dropdown: $("#dropdownTemplate"),
        numeric: $("#numericTemplate"),
        checkbox: $("#checkboxTemplate")
    };
};

/**
 * Provide a prefix for use in making names & ids for elements in a template.
 * The prefix is unique across all rubrics.
 *
 * @private
 * @param {string} [name] Name to prefix.
 *
 */
marmoset.RubricManager.prototype._prefix = function(name) {
    prefix = "rubric-" + this.rubricCount;
    if (name) {
        prefix = prefix + "-" + name;
    }
    return prefix;
}

/** 
 * Render a rubric template and add it to the table. Returns the jQuery object
 * that results. Rubrics are rendered as a row in a table, and differ only in
 * the widgets used to edit the rubric.
 *
 * @private
 * @param {template} the rubric-specific (editing widgets) template to render
 * @param {Map<String, String>} value map, populated with values specific to the
 *                              rubric being added.
 *
 * @return {object} the jQuery object representing the rendered template.
 */
marmoset.RubricManager.prototype._addRubric = function(template, values) {
    this.rubricCount += 1;
    values.count = this.rubricCount;
    values.prefix = this._prefix();
    values.editWidgets = template.render(values);
    var row = this.templates.rubric.render(values);
    var result = $(row).appendTo(this.table);
    $(this).trigger('change');
};

marmoset.RubricManager.prototype._addDropdown = function(event) {
    this._addRubric(this.templates.dropdown, {
        presentation: "DROPDOWN",
        header: "Dropdown"
    });
    var select = $('#' + this._prefix("select")),
        hidden = $('#' + this._prefix("hidden")),
        widget = new marmoset.DropdownWidget(select, hidden),
        editor = this.dropdownEditor;
    $("#" + this._prefix("edit-button")).click(function(event) {
        event.preventDefault();
        editor.edit(widget);
    });
};

marmoset.RubricManager.prototype._addNumeric = function(event) {
    this._addRubric(this.templates.numeric, {
        presentation: "NUMERIC",
        header: "Numeric"
    });
};

marmoset.RubricManager.prototype._addCheckbox = function(event) {
    this._addRubric(this.templates.checkbox, {
        presentation: "CHECKBOX",
        header: "Checkbox",
    });
};

/**
 * Set the button to add a dropdown rubric.
 *
 * @param {string} id of the button to use.
 */
marmoset.RubricManager.prototype.setAddDropdownButton = function(buttonId) {
    var manager = this;
    $(buttonId).click(function(event) {
    	event.preventDefault();
        manager._addDropdown();
    });
};

/**
 * Set the button to add a numeric widget.
 *
 * @param {string} id of the button to use.
 */
marmoset.RubricManager.prototype.setAddNumericButton = function(buttonId) {
    var manager = this;
    $(buttonId).click(function(event) {
    	event.preventDefault();
        manager._addNumeric();
    });
};

/**
 * Set the button to add a checkbox widget.
 *
 * @param {string} id of the button to use.
 */
marmoset.RubricManager.prototype.setAddCheckboxButton = function(buttonId) {
    var manager = this;
    $(buttonId).click(function(event) {
    	event.preventDefault();
        manager._addCheckbox();
    });
};

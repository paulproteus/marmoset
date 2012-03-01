<%--

 Marmoset: a student project snapshot, submission, testing and code review
 system developed by the Univ. of Maryland, College Park
 
 Developed as part of Jaime Spacco's Ph.D. thesis work, continuing effort led
 by William Pugh. See http://marmoset.cs.umd.edu/
 
 Copyright 2005 - 2011, Univ. of Maryland
 
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy of
 the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 License for the specific language governing permissions and limitations under
 the License.

--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions'%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>


<!DOCTYPE HTML>
<html>
<head>

<ss:headContent title="Create code review for Project ${project.fullTitle}" />

<c:url var="jsBase" value="/js" />

<link href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.7/themes/base/jquery-ui.css" rel="stylesheet"
    type="text/css" />
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js" type="text/javascript"></script>
<script src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.7/jquery-ui.min.js" type="text/javascript"></script>
<link href="${jsBase}/timePicker.css" rel="stylesheet" type="text/css" />
<script src="${jsBase}/jquery.timePicker.js" type="text/javascript"></script>

<script type="text/javascript">
	$(function() {

	});
</script>

<style type="text/css">
form {
    margin-right: 5em;
}
ul.form-fields > li > label {
    display: block;
}
ul.form-fields > li {
    margin-top: 1em;
}
ul.form-fields li:first-child {
    margin-top: 0px;
}
ul.form-fields ul.reviewer-list {
    margin-left: 2em;
}
form ul {
    list-style-type: none;
    padding: 0px;
    margin: 0px;
}
fieldset {
    border: 1px solid black;
    margin-bottom: 1em;
}
fieldset:first-child {
    margin-top: 1em;
}
fieldset h3 {
    margin: 0px;
}
ul#rubric-list > li {
    margin-top: 1em;
    border: 1px solid black;
    padding: 0.5em;
    overflow: auto;
    width: 90%;
}
div.rubric-editing {
    float: left;
}
div.rubric-row-controls {
    float: right;
}
div.rubric-editing label:first-child {
    width: 5em;
    display: inline-block;
}
</style>

</head>

<body>
    <ss:header />
    <ss:instructorBreadCrumb />

    <div class="sectionTitle">
        <h1>
            New Code Review for project
            <c:out value="${project.projectNumber}" />
        </h1>

        <p class="sectionDescription">
            Fill out the following form to create a new code review for project
            <c:out value="${project.fullTitle}" />
        </p>
    </div>


<c:url value="/action/instructor/CreateCodeReviewAssignment" var="createAssignmentAction" />
<form action="${createAssignmentAction}" method="POST" id="code-review-creation">
    <input type="hidden" name="coursePK" value="${course.coursePK}" />
    <input type="hidden" name="projectPK" value="${project.projectPK}" />
    <fieldset id="basic-review-info">
        <ul class="form-fields">
	        <li>
	           <label for="codereview-kind">Review kind:</label>
	           <select name="kind" id="codereview-kind" required="required">
                         <option value="instructionalPrototype"
                                selected="selected"
                                >Prototype of an instructional review</option>                    
                            <option value="instructional"
                                title="Select staff members that divy up all student submissions to review">Instructional</option>
                            <c:if test="${not empty course.sections}">
                                <option value="instructionalBySection"
                                    title="One staff member does all reviews for each section">Instructional by
                                    section</option>
                            </c:if>
                            <option value="peer" title="Students review each other's code">Student peer review</option>
                             <option value="peerPrototype">Prototype of an peer review</option>                    
                       <option value="exemplar"
                                title="Students are all asked to review a example or exemplar submission by a staff member">Student
                                review of an example/exemplar submission</option>
	           </select>
	        </li>
	        <li>
	           <label for="code-review-description">Description:</label>
	           <input type="text" size="60" required="required" name="description" id="code-review-description" />
	        </li>
	        <li>
	           <label for="codereview-deadline-date">Deadline:</label>
	           <input type="date" id="codereview-deadline-date" name="deadline-date" placeholder="yyyy-mm-dd" size="12" required="required" />
	           <input type="time" id="codereview-deadline-time" name="deadline-time" placeholder="hh:mm aa" size="12" title="leave blank for 1 second before midnight" />
	        </li>
            <li id="anonymity-info">
                <label>Anonymous:</label>
                    <ul>
                        <li>
                            <input type="radio" name="anonymous" value="true" id="yes-anonymous"/>
                            <label for="yes-anonymous">Yes</label>
                        </li>
                        <li>
                            <input type="radio" name="anonymous" value="false" id="no-anonymous" checked="checked"/>
                            <label for="no-anonymous">No</label>
                        </li>
                    </ul>
            </li>
            <li id="visibility-info">
                <label>Other reviewers' comments:</label>
                     <ul>
                        <li>
                            <input type="radio" name="canSeeOthers" value="true" id="can-see-others"/>
                            <label for="can-see-others">Visible</label>
                        </li>
                        <li>
                            <input type="radio" name="canSeeOthers" value="false" id="cant-see-others" checked="checked"/>
                            <label for="cant-see-others">Hidden</label>
                        </li>
                    </ul>
            </li>
        </ul>
    </fieldset>
    <fieldset id="instructional-review-info">
    <h3>Instructional Review</h3>
        <ul class="form-fields">
            <li>
                <label>Choose reviewers:</label>
                <ul class="reviewer-list">
                    <c:forEach var="studentRegistration" items="${courseInstructors}">
                    <li>
                        <input type="checkbox" id="instructional-reviewer-${studentRegistration.studentPK}" name="reviewer-${studentRegistration.studentPK}">
                        <label for="instructional-reviewer-${studentRegistration.studentPK}"><c:out value="${studentRegistration.fullname}" /></label>
                    </li>
                    </c:forEach>
                </ul>
            </li>
        </ul>
    </fieldset>
    <fieldset id="instructional-by-section-info">
    <h3>Instructional Review by Section</h3>
        <ul class="form-fields">
            <li>
                <label>Choose a reviewer for each section:</label>
                <ul class="reviewer-list">
                    <c:forEach var="section" items="${course.sections}">
                    <li>
                        <label for="section-reviewer-${section}">
                            <c:out value="${section}" />
                        </label>
                        <select name="section-reviewer-${section}" id="section-reviewer-${section}">
                            <c:forEach var="studentRegistration" items="${courseInstructors}" >
                            <c:if test="${empty studentRegistration.section || studentRegistration.section == section }">
                            <option value="${studentRegistration.studentPK}">
                                <c:out value="${studentRegistration.fullname}" />
                            </option>
                            </c:if>
                            </c:forEach>
                        </select>
                    </li>
                    </c:forEach>
                </ul>
            </li>
        </ul>
    </fieldset>
    <fieldset id="exemplar-review-info">
    <h3>Review of an Exemplar</h3>
        <ul class="form-fields">
            <li>
                <label for="exemplar-submission">Review of:</label>
                <select name="of" id="exemplar-submission">
                    <c:forEach var="studentRegistration" items="${staffStudentSubmissions}">
                    <option value="${lastSubmission[studentRegistration.studentRegistrationPK].submissionPK}">
                        <c:out value="${studentRegistration.fullname}" />
                    </option>
                    </c:forEach>
                </select>
            </li>
        </ul>
    </fieldset>
    <fieldset id="review-assignment-info">
    <h3>Review assignment strategy</h3>
        <ul class="form-fields">
            <li>
                <label>Assign reviews:</label>
					<ul>
                        <li>
                            <input type="radio" name="peerBySection" value="true" id="yes-by-section" checked="checked"/>
                            <label for="yes-by-section">Only within a section</label>
                        </li>
                        <li>
                            <input type="radio" name="peerBySection" value="false" id="not-by-section"/>
                            <label for="not-by-section">Across sections</label>
                        </li>
                    </ul>
            </li>
            <li>
                <label for="reviews-per-submission">Reviews per submission:</label>
                <select name="numReviewers" id="reviews-per-submission">
                    <option>1</option>
                    <option>2</option>
                    <option>3</option>
                </select>
            </li>
        </ul>
    </fieldset>
    <fieldset id="review-rubrics">
    <h3>Rubrics</h3>
    <div id="rubric-controls">
        <button id="add-numeric-rubric" type="button">Add Numeric</button>
        <button id="add-dropdown-rubric" type="button">Add Dropdown</button>
        <button id="add-checkbox-rubric" type="button">Add Checkbox</button>
    </div>
    <input type="hidden" name="rubric-count" id="rubric-count" value="0" />
    <ul id="rubric-list">
    </ul>
    </fieldset>
    <div style="text-align: right">
        <button id="create-code-review">Create Code Review</button>
    </div>
</form>
<script type="text/javascript">
	function updateForm(kind) {
		console.log('code review kind: "' + kind + '"');
		switch (kind) {
		case "none":
			hideItem("instructional-review-info");
			hideItem("instructional-by-section-info");
			hideItem("anonymity-info");
	        hideItem("visibility-info");
			hideItem("exemplar-review-info");
			hideItem("review-assignment-info");
			break;
		case "instructional":
			showItem("instructional-review-info");
			hideItem("instructional-by-section-info");
			hideItem("anonymity-info");
			hideItem("visibility-info");
			hideItem("exemplar-review-info");
			hideItem("review-assignment-info");
			break;
		case "instructionalPrototype":
			hideItem("instructional-review-info");
            hideItem("instructional-by-section-info");
            hideItem("anonymity-info");
            hideItem("visibility-info");
            showItem("exemplar-review-info");
            hideItem("review-assignment-info");
            break;
        case "instructionalBySection":
			hideItem("instructional-review-info");
			showItem("instructional-by-section-info");
			hideItem("anonymity-info");
			hideItem("visibility-info");
			hideItem("exemplar-review-info");
			hideItem("review-assignment-info");
			break;
		case "peer":
			hideItem("instructional-review-info");
			hideItem("instructional-by-section-info");
			showItem("anonymity-info");
			showItem("visibility-info");
			hideItem("exemplar-review-info");
			showItem("review-assignment-info");
			break;
		case "peerPrototype":
            hideItem("instructional-review-info");
            hideItem("instructional-by-section-info");
            showItem("anonymity-info");
            showItem("visibility-info");
            showItem("exemplar-review-info");
            hideItem("review-assignment-info");
            break;
        case "exemplar":
			hideItem("instructional-review-info");
			hideItem("instructional-by-section-info");
			showItem("anonymity-info");
			showItem("visibility-info");
			showItem("exemplar-review-info");
			hideItem("review-assignment-info");
			break;
		default:
			console.log("Unknown kind");
		}
	}
	
	var $kind = $("#codereview-kind");
	$kind.change(function(event) {
		updateForm($kind.val());
	});

	$(document).ready(function() {
		$kind.change();
		$('#codereview-deadline-date').datepicker({
			minDate : -1,
			defaultDate : +7,
			dateFormat : "yy-mm-dd",
			onSelect : function(selectedDate) {
				$("#codereview-deadline-time").focus();
			}
		});

		$("#codereview-deadline-time").timePicker({
			show24Hours : false,
			step : 60,
			defaultTime : "06:00 PM"
		});
		
		$("#rubric-controls").buttonset();
		$("#create-code-review").button();
	});
</script>
    <ss:script file="jsrender.js" />
    <script id="rubricTemplate" type="text/x-jquery-tmpl">
            <li id="rubric-{{=count}}">
                <input type="hidden" name="{{=prefix}}-presentation" value="{{=presentation}}" />
                <div class="rubric-editing">
                <div>
                <label for="{{=prefix}}-name">{{=header}}: </label>
                <input type="text" id="{{=prefix}}-name" name="{{=prefix}}-name" size="20" required="required" placeholder="Name of rubric item"/>
                <input type="text" name="{{=prefix}}-description" size="50" placeholder="a longer description of this rubric item"/>
                </div>
                <div>
                <label>Edit points:</label>
                {{=editWidgets!}}
                </div>
                </div>
                <div class="rubric-row-controls">
                   <button type="button" id="{{=prefix}}-delete" value="rubric-{{=count}}">delete</button>
                </div>
            </li>
        </script>

    <script id="dropdownTemplate" type="text/x-jquery-tmpl">
            <input type="hidden" name="{{=prefix}}-value" id="{{=prefix}}-hidden"/>
            <select id="{{=prefix}}-select"></select>
            <button id="{{=prefix}}-edit-button">edit dropdown</button>
        </script>
    <script id="numericTemplate" type="text/x-jquery-tmpl">
            <label for="{{=prefix}}-min-input">min:</label>
            <input type="number" name="{{=prefix}}-min" id="{{=prefix}}-in-input" placeholder="min"  size="4">

            <label for="{{=prefix}}-max-input">max:</label>
            <input type="number" name="{{=prefix}}-max" id="{{=prefix}}-max-input" placeholder="max"  size="4">

            <label for="{{=prefix}}-default-input">default:</label>
            <input type="number" name="{{=prefix}}-default" id="{{=prefix}}-default-input" placeholder="default"  size="4">
        </script>
    <script id="checkboxTemplate" type="text/x-jquery-tmpl">
            <input type="checkbox" value="ignore" onclick="return false;"  >
            <input type="number" name="{{=prefix}}-false" title="value if not checked" size="4" value="0" required="required">
            &nbsp;
            <input type="checkbox" value="ignore"   onclick="return false;"  CHECKED >
            <input type="number" name="{{=prefix}}-true" title="value if checked"  size="4" required="required">
        </script>

    <div id="edit-dialog" style="display: none">
        <select id="edit-dialog-dropdown-select"></select>
        <div>
            <input type="text" id="edit-dialog-value-input" placeholder="name" /> <input type="text"
                id="edit-dialog-score-input" placeholder="score" />
        </div>
        <div>
            <span id="edit-dialog-controls">
                <button id="edit-dialog-add">add</button>
                <button id="edit-dialog-delete">delete</button>
                <button id="edit-dialog-clear-all">clear all</button>
            </span>
        </div>
    </div>

    <ss:script file="rubrics.js" />
    <script type="text/javascript">
		var dropdownEditor = new marmoset.DropdownEditor("#edit-dialog");
		var manager = new marmoset.RubricManager("#rubric-list",
				dropdownEditor);
		manager.setAddDropdownButton("#add-dropdown-rubric");
		manager.setAddNumericButton("#add-numeric-rubric");
		manager.setAddCheckboxButton("#add-checkbox-rubric");
		$(manager).one('change', function(event) {
			$("#rubricTable").show();
		});
		$(manager).change(function(event) {
			$("#rubric-count").val(manager.rubricCount);
		});
	</script>

    <ss:footer />
</body>
</html>

package edu.umd.review.gwt.widget;

import com.allen_sauer.gwt.dnd.client.HasDragHandle;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

import edu.umd.review.gwt.rpc.dto.RubricDto;

/**
 * Simple widget for dragging around rubric names. Stores the name of the dragged rubric
 * so it can be recovered on drop.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
public class RubricDragger extends Composite implements HasDragHandle {
  /** UiBinder interface. */
  interface WidgetBinder extends UiBinder<Widget, RubricDragger> { }
  private static WidgetBinder uiBinder = GWT.create(WidgetBinder.class);

  @UiField InlineLabel nameLabel;

  private final RubricDto rubric;


  public RubricDragger(RubricDto rubric) {
    initWidget(uiBinder.createAndBindUi(this));
    this.rubric = rubric;
    nameLabel.setText(rubric.getName());
    nameLabel.setStyleName("comment-text");
    DOM.setElementProperty(nameLabel.getElement(), "title", rubric.getDescription());
  }

  public String getRubricName() {
    return rubric.getName();
  }
  public RubricDto getRubric() {
      return rubric;
    }

  @Override
  public Widget getDragHandle() {
    return nameLabel;
  }
}

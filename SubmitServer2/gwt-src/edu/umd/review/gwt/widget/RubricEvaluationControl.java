package edu.umd.review.gwt.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto.Status;

public class RubricEvaluationControl extends Composite {
  interface WidgetBinder extends UiBinder<Widget, RubricEvaluationControl> {}
  private static WidgetBinder uiBinder = GWT.create(WidgetBinder.class);

  public interface Handler {
    void saveEvaluation();

    void editEvaluation();

    void killEvaluation();

    void cancelEvaluation();
  }

  @UiField Anchor discardLink;
  @UiField Anchor editLink;
  @UiField Anchor saveLink;
  @UiField Anchor cancelLink;

  private Status status;
  private Handler handler;

  public RubricEvaluationControl() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public void setHandler(Handler handler) {
    this.handler = handler;
  }

  public void showControls(boolean isEditing) {
    discardLink.setVisible(!isEditing && (status != Status.DEAD));
    editLink.setVisible(!isEditing && (status.isEditable()));
    saveLink.setVisible(isEditing && (status != Status.DEAD));
    cancelLink.setVisible(isEditing && (status != Status.DEAD));
  }

  @UiHandler("discardLink")
  void onDiscardClicked(ClickEvent event) {
    if (handler != null) {
      handler.killEvaluation();
    }
  }

  @UiHandler("editLink")
  void onEditClicked(ClickEvent event) {
    if (handler != null) {
      handler.editEvaluation();
    }
  }

  @UiHandler("saveLink")
  void onSaveClicked(ClickEvent event) {
    if (handler != null) {
      handler.saveEvaluation();
    }
  }

  @UiHandler("cancelLink")
  void onCancelClicked(ClickEvent event) {
    if (handler != null) {
      handler.cancelEvaluation();
    }
  }
}

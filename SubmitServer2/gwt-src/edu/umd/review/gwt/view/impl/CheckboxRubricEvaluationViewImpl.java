package edu.umd.review.gwt.view.impl;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import edu.umd.review.gwt.CodeReviewResources;
import edu.umd.review.gwt.RubricStyle;
import edu.umd.review.gwt.rpc.dto.CheckboxEvaluationDto;
import edu.umd.review.gwt.rpc.dto.CheckboxRubricDto;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto.Status;
import edu.umd.review.gwt.view.CheckboxRubricEvaluationView;
import edu.umd.review.gwt.widget.RubricEvaluationControl;

public class CheckboxRubricEvaluationViewImpl extends Composite implements
    CheckboxRubricEvaluationView {
  interface ViewBinder extends UiBinder<Widget, CheckboxRubricEvaluationViewImpl> {}

  private static RubricStyle rubricStyle = CodeReviewResources.INSTANCE.rubricStyle();

  private static ViewBinder uiBinder = GWT.create(ViewBinder.class);
  private final AuthorColorFactory colorFactory = new AuthorColorFactory();

  @UiField FlowPanel rubricPanel;
  @UiField Label titleLabel;
  @UiField CheckBox checkBox;
  @UiField TextBox explanationBox;
  @UiField RubricEvaluationControl control;

  private Status evaluationStatus;
  private Presenter presenter;

  public CheckboxRubricEvaluationViewImpl() {
    initWidget(uiBinder.createAndBindUi(this));
    rubricStyle.ensureInjected();
  }

  @Override
  public String getValue() {
    return String.valueOf(checkBox.getValue());
  }

  @Override
  public void setValue(String value) {
    setValue(value, false);
  }

  @Override
  public void setValue(String value, boolean fireEvents) {
    Preconditions.checkNotNull(value);
    checkBox.setValue(Boolean.parseBoolean(value));
    ValueChangeEvent.fire(this, value);
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  @Override
  public void setPresenter(@Nullable Presenter presenter) {
    this.presenter = presenter;
    control.setHandler(presenter);
  }

    @Override
    public void showEvaluation(CheckboxRubricDto rubric, CheckboxEvaluationDto evaluation) {
        checkBox.setValue(Boolean.parseBoolean(evaluation.getValue()));
        explanationBox.setValue(evaluation.getExplanation());
        DOM.setElementProperty(explanationBox.getElement(), "placeholder", rubric.getDescription());
        if (!Strings.isNullOrEmpty(evaluation.getExplanation())) {
          DOM.setElementProperty(explanationBox.getElement(), "title", evaluation.getExplanation());
        }
        switch(evaluation.getStatus()) {
          case NEW:
          case DRAFT:
              rubricPanel.setStylePrimaryName(rubricStyle.rubricPanelDraft());
              break;
          case LIVE:
            rubricPanel.setStylePrimaryName(rubricStyle.rubricPanelLive());
              break;
          case DEAD:
              rubricPanel.setStylePrimaryName(rubricStyle.rubricPanelDead());
              break;
          }
        control.setVisible(evaluation.isEditable());
        if (evaluation.isEditable()) {
          control.setStatus(evaluation.getStatus());
        }
        control.setStatus(evaluation.getStatus());
        evaluationStatus = evaluation.getStatus();
        titleLabel.setText(evaluation.getAuthorName() + ": " + rubric.getName());
        titleLabel.addStyleName(colorFactory.getColor(evaluation.getAuthorName()));
        DOM.setElementProperty(rubricPanel.getElement(), "title", rubric.getDescription());

    }

  @Override
  public void setEditing(boolean editing) {
    checkBox.setEnabled(editing);
    explanationBox.setReadOnly(!editing);
    control.showControls(editing);
    // we defer setting the focus until after the event loop returns, otherwise it gets lost
    if (!editing) {
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        @Override
        public void execute() {
          checkBox.setFocus(false);
        }
      });
    } else {
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        @Override
        public void execute() {
          checkBox.setFocus(true);
        }
      });
    }
  }

  @Override
  public String getExplanation() {
    return explanationBox.getValue();
  }

  @UiHandler("checkBox")
  void onEnterPressed(KeyPressEvent event) {
    int keyCode = event.getNativeEvent().getKeyCode();
    if (presenter != null && keyCode == KeyCodes.KEY_ENTER) {
      presenter.saveEvaluation();
    }
  }
}

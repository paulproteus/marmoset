package edu.umd.review.gwt.view.impl;

import java.util.List;

import javax.annotation.Nullable;

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
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import edu.umd.review.gwt.CodeReviewResources;
import edu.umd.review.gwt.RubricStyle;
import edu.umd.review.gwt.rpc.dto.DropdownEvaluationDto;
import edu.umd.review.gwt.rpc.dto.DropdownRubricDto;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto.Status;
import edu.umd.review.gwt.view.DropdownRubricEvaluationView;
import edu.umd.review.gwt.widget.RubricEvaluationControl;

public class DropdownRubricEvaluationViewImpl extends Composite implements
    DropdownRubricEvaluationView {
  interface ViewBinder extends UiBinder<Widget, DropdownRubricEvaluationViewImpl> {}
  private static ViewBinder uiBinder = GWT.create(ViewBinder.class);

  private static RubricStyle rubricStyle = CodeReviewResources.INSTANCE.rubricStyle();
  private final AuthorColorFactory colorFactory = new AuthorColorFactory();

  @UiField FlowPanel rubricPanel;
  @UiField Label titleLabel;
  @UiField ListBox choiceBox;
  @UiField TextBox explanationBox;
  @UiField RubricEvaluationControl control;

  private Presenter presenter;
  private Status evaluationStatus;

  public DropdownRubricEvaluationViewImpl() {
    initWidget(uiBinder.createAndBindUi(this));
    rubricStyle.ensureInjected();
  }

  @Override
  public void setPresenter(@Nullable Presenter presenter) {
    this.presenter = presenter;
    control.setHandler(presenter);
  }

  @Override
  public String getValue() {
    return choiceBox.getItemText(choiceBox.getSelectedIndex());
  }

  @Override
  public void setValue(String value) {
    setValue(value, false);
  }

  @Override
  public void setValue(String value, boolean fireEvents) {
    for (int i = 0; i < choiceBox.getItemCount(); i++) {
      if (value.equals(choiceBox.getItemText(i))) {
        choiceBox.setSelectedIndex(i);
      }
    }
    ValueChangeEvent.fire(this, value);
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

    @Override
    public void showEvaluation(DropdownRubricDto rubric, DropdownEvaluationDto evaluation) {
        List<String> choices = rubric.getDropdownChoices();
        choiceBox.clear();
        for (String s : choices) {
            choiceBox.addItem(s);
        }
        choiceBox.setSelectedIndex(choices.indexOf(evaluation.getValue()));
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
    choiceBox.setEnabled(editing);
    explanationBox.setReadOnly(!editing);
    control.showControls(editing);
    // we defer setting the focus until after the event loop returns, otherwise it gets lost
    if (!editing) {
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        @Override
        public void execute() {
          choiceBox.setFocus(false);
        }
      });
    } else {
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        @Override
        public void execute() {
          choiceBox.setFocus(true);
        }
      });
    }
  }

  @Override
  public String getExplanation() {
    return explanationBox.getValue();
  }

  @UiHandler("choiceBox")
  void onChoiceBoxEnter(KeyPressEvent event) {
    int keyCode = event.getNativeEvent().getKeyCode();
    if (presenter != null && keyCode == KeyCodes.KEY_ENTER) {
      presenter.saveEvaluation();
    }
  }
}

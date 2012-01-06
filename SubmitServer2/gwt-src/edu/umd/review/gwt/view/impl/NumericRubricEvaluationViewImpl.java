package edu.umd.review.gwt.view.impl;

import java.util.LinkedHashMap;

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
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;
import com.google.gwt.user.client.ui.Widget;

import edu.umd.review.gwt.CodeReviewResources;
import edu.umd.review.gwt.RubricStyle;
import edu.umd.review.gwt.rpc.dto.NumericEvaluationDto;
import edu.umd.review.gwt.rpc.dto.NumericRubricDto;
import edu.umd.review.gwt.view.NumericRubricEvaluationView;
import edu.umd.review.gwt.widget.RubricEvaluationControl;

public class NumericRubricEvaluationViewImpl extends Composite implements
    NumericRubricEvaluationView {
  interface ViewBinder extends UiBinder<Widget, NumericRubricEvaluationViewImpl> {}

  private static RubricStyle rubricStyle = CodeReviewResources.INSTANCE.rubricStyle();
  private final AuthorColorFactory colorFactory = new AuthorColorFactory();

  private static ViewBinder uiBinder = GWT.create(ViewBinder.class);

  @UiField FlowPanel rubricPanel;
  @UiField Label titleLabel;
  @UiField TextBox scoreBox;
  @UiField TextBox explanationBox;
  @UiField RubricEvaluationControl control;

  private Presenter presenter;

  public NumericRubricEvaluationViewImpl() {
    initWidget(uiBinder.createAndBindUi(this));
    rubricStyle.ensureInjected();
    scoreBox.setVisibleLength(4);
    scoreBox.setAlignment(TextAlignment.RIGHT);
  }

  @Override
  public String getValue() {
    return scoreBox.getValue();
  }

  @Override
  public String getExplanation() {
    return explanationBox.getValue();
  }

  @Override
  public void setValue(String value) {
    setValue(value, false);
  }

  @Override
  public void setValue(String value, boolean fireEvents) {
    Preconditions.checkNotNull(value);
    scoreBox.setValue(value);
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
  public void showEvaluation(NumericRubricDto rubric, NumericEvaluationDto evaluation) {
    scoreBox.setText(evaluation.getValue());
    Element element = scoreBox.getElement();
    DOM.setElementProperty(element, "type", "number");
    DOM.setElementProperty(element, "required", "required");
    LinkedHashMap<String,Integer> data =  rubric.getData();
    Integer min = data.get("min");
    Integer max = data.get("max");
    if (min != null) {
        DOM.setElementProperty(element, "min", min.toString());
        if (max != null) {
            String limits;
            if (min >= 0 && max >= 0)
                limits = min + "-" + max;
            else
                limits = min + " ...  " + max;
            DOM.setElementProperty(element, "title", limits);
        }
    }
    if (max != null) {
        DOM.setElementProperty(element, "max", max.toString());
    }


    explanationBox.setValue(evaluation.getExplanation());
    DOM.setElementProperty(explanationBox.getElement(),"placeholder", rubric.getDescription());
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
    evaluation.getStatus();
    titleLabel.setText(evaluation.getAuthorName() + ": " + rubric.getName());
    titleLabel.addStyleName(colorFactory.getColor(evaluation.getAuthorName()));
    DOM.setElementProperty(rubricPanel.getElement(), "title", rubric.getDescription());
  }

  @Override
  public void setEditing(boolean editing) {
    scoreBox.setReadOnly(!editing);
    explanationBox.setReadOnly(!editing);
    control.showControls(editing);
    // we defer setting the focus until after the event loop returns, otherwise it gets lost
    if (!editing) {
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        @Override
        public void execute() {
          scoreBox.setFocus(false);
        }
      });
    } else {
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        @Override
        public void execute() {
          scoreBox.setFocus(true);
          scoreBox.setSelectionRange(0, scoreBox.getValue().length());
        }
      });
    }
  }

  @UiHandler("scoreBox")
  void onScoreBoxEnter(KeyPressEvent event) {
    int keyCode = event.getNativeEvent().getKeyCode();
    if (presenter != null && keyCode == KeyCodes.KEY_ENTER) {
      presenter.saveEvaluation();
    }
  }
}

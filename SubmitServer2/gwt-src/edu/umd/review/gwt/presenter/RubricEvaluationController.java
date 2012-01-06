package edu.umd.review.gwt.presenter;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import edu.umd.review.common.action.DeleteRubricEvaluationAction;
import edu.umd.review.common.action.SaveRubricEvaluationAction;
import edu.umd.review.common.action.VoidResult;
import edu.umd.review.gwt.GwtUtils;
import edu.umd.review.gwt.event.EvaluationSavedEvent;
import edu.umd.review.gwt.event.PublishEvaluationEvent;
import edu.umd.review.gwt.event.RubricDiscardEvent;
import edu.umd.review.gwt.rpc.dto.RubricDto;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto.Status;

public class RubricEvaluationController {

  private RubricEvaluationDto evaluation;
  private RubricDto rubric;
  private DispatchAsync dispatch;
  private EventBus eventBus;

  RubricEvaluationController(RubricDto rubric,
                             RubricEvaluationDto evaluation,
                             DispatchAsync dispatch,
                             EventBus eventBus) {
    this.rubric = rubric;
    this.evaluation = evaluation;
    this.dispatch = dispatch;
    this.eventBus = eventBus;
  }

  public boolean saveEvaluation(String value, String explanation) {
    if (!rubric.isValidValue(value)) {
      Window.alert(rubric.getValidationMessage(value));
      return false;
    }
    if (!evaluation.getStatus().isEditable() || !evaluation.isEditable()) {
      Window.alert("Cannot edit this rubric");
      return false;
    }
    evaluation.setValue(value);
    evaluation.setExplanation(explanation);
    if (evaluation.getStatus() == Status.NEW) {
      evaluation.setStatus(Status.DRAFT);
    }
    dispatch.execute(new SaveRubricEvaluationAction(evaluation), new AsyncCallback<VoidResult>() {
      @Override
      public void onFailure(Throwable caught) {
        GwtUtils.wrapAndThrow(caught);
      }

      @Override
      public void onSuccess(VoidResult result) {
        eventBus.fireEvent(new EvaluationSavedEvent(evaluation));
      }
    });
    return true;
  }

  public void killEvaluation() {
    if (!evaluation.isEditable()) {
      Window.alert("Cannot edit this rubric");
      return;
    }
    switch (evaluation.getStatus()) {
      case DRAFT:
      case NEW:
        dispatch.execute(new DeleteRubricEvaluationAction(evaluation), new AsyncCallback<VoidResult>() {
          @Override
          public void onFailure(Throwable caught) {
            GwtUtils.wrapAndThrow(caught);
          }

          @Override
          public void onSuccess(VoidResult result) {
            eventBus.fireEvent(new RubricDiscardEvent(evaluation));
          }
        });
        break;
      case LIVE:
        evaluation.setStatus(Status.DEAD);
        dispatch.execute(new SaveRubricEvaluationAction(evaluation), new AsyncCallback<VoidResult>() {
          @Override
          public void onFailure(Throwable caught) {
            GwtUtils.wrapAndThrow(caught);
          }

          @Override
          public void onSuccess(VoidResult result) {
            eventBus.fireEvent(new RubricDiscardEvent(evaluation));
          }
        });
        break;
      case DEAD:
        break;
    }
  }

  @Deprecated
  public void publishEvaluation() {
    if (evaluation.getStatus() != Status.DRAFT || !evaluation.isEditable()) {
      Window.alert("Cannot public this rubric");
      return;
    }
    evaluation.setStatus(Status.LIVE);
    dispatch.execute(new SaveRubricEvaluationAction(evaluation), new AsyncCallback<VoidResult>() {
      @Override
      public void onFailure(Throwable caught) {
        GwtUtils.wrapAndThrow(caught);
      }

      @Override
      public void onSuccess(VoidResult result) {
        eventBus.fireEvent(new PublishEvaluationEvent(evaluation));
      }
    });
  }

  public RubricDto getRubric() {
    return rubric;
  }

  public RubricEvaluationDto getEvaluation() {
    return evaluation;
  }
}

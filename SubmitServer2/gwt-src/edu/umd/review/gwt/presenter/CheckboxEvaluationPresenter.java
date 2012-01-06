package edu.umd.review.gwt.presenter;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import edu.umd.review.gwt.event.NewRubricEvaluationEvent;
import edu.umd.review.gwt.rpc.dto.CheckboxEvaluationDto;
import edu.umd.review.gwt.rpc.dto.CheckboxRubricDto;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto.Status;
import edu.umd.review.gwt.view.CheckboxRubricEvaluationView;

public class CheckboxEvaluationPresenter extends AbstractPresenter implements
    CheckboxRubricEvaluationView.Presenter {

  private final CheckboxRubricEvaluationView view;
  private final RubricEvaluationController controller;
  private CheckboxRubricDto rubric;
  private CheckboxEvaluationDto evaluation;
  private final EventBus eventBus;

  @Inject
  public CheckboxEvaluationPresenter(@Assisted CheckboxRubricEvaluationView view,
                                     DispatchAsync dispatch,
                                     EventBus eventBus,
                                     @Assisted CheckboxRubricDto rubric,
                                     @Assisted CheckboxEvaluationDto evaluation) {
    this.view = view;
    view.setPresenter(this);
    this.controller = new RubricEvaluationController(rubric, evaluation, dispatch, eventBus);
    this.rubric = rubric;
    this.evaluation = evaluation;
    this.eventBus = eventBus;
  }

  @Override
  public void start() {
    view.showEvaluation(rubric, evaluation);
    view.setEditing(evaluation.getStatus() == Status.NEW);
    if (evaluation.getStatus() == Status.DRAFT || evaluation.getStatus() == Status.NEW) {
      eventBus.fireEvent(new NewRubricEvaluationEvent(evaluation));
    }
  }

  @Override
  public void saveEvaluation() {
    if (controller.saveEvaluation(view.getValue(), view.getExplanation())) {
      view.showEvaluation(rubric, evaluation);
      view.setEditing(false);
    }
  }

  @Override
  public void editEvaluation() {
    view.setEditing(true);
  }

  @Override
  public void killEvaluation() {
    controller.killEvaluation();
    view.showEvaluation(rubric, evaluation);
    view.setEditing(false);
  }

  @Override
  public void cancelEvaluation() {
    view.showEvaluation(rubric, evaluation);
    view.setEditing(false);
  }
}

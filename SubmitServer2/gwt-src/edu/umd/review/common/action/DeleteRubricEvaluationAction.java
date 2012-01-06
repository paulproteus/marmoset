package edu.umd.review.common.action;

import net.customware.gwt.dispatch.shared.Action;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto;

public class DeleteRubricEvaluationAction implements Action<VoidResult> {
  private RubricEvaluationDto evaluation;

  @Deprecated
  private DeleteRubricEvaluationAction() {}

  public DeleteRubricEvaluationAction(RubricEvaluationDto evaluation) {
    this.evaluation = evaluation;
  }

  public RubricEvaluationDto getEvaluation() {
    return evaluation;
  }
}

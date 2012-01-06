package edu.umd.review.common.action;

import net.customware.gwt.dispatch.shared.Action;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto;

public class SaveRubricEvaluationAction implements Action<VoidResult> {
  private RubricEvaluationDto evaluation;

  @Deprecated
  private SaveRubricEvaluationAction() {}

  public SaveRubricEvaluationAction(RubricEvaluationDto evaluation) {
    this.evaluation = evaluation;
  }

  public RubricEvaluationDto getEvaluation() {
    return evaluation;
  }
}

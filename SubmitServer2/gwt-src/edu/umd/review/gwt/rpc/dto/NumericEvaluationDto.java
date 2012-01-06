package edu.umd.review.gwt.rpc.dto;

import edu.umd.cs.marmoset.modelClasses.CodeReviewThread;
import edu.umd.cs.marmoset.modelClasses.CodeReviewer;
import edu.umd.cs.marmoset.modelClasses.RubricEvaluation;

/**
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
public class NumericEvaluationDto extends RubricEvaluationDto {


  /** @deprecated GWT only. */
  @SuppressWarnings("unused") @Deprecated
  private NumericEvaluationDto() {
    super();
  }

	public NumericEvaluationDto(NumericRubricDto rubric,
	                            @RubricEvaluation.PK int rubricEvaluationPK,
	                            @CodeReviewer.PK int authorPK,
	                            String authorName,
	                            @CodeReviewThread.PK int threadid,
	                            boolean editable, Status status) {
		super(rubric, rubricEvaluationPK, authorPK, authorName, threadid, editable, status);
  }
}

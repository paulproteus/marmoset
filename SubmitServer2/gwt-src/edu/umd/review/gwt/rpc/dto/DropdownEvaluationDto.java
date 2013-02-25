package edu.umd.review.gwt.rpc.dto;

import edu.umd.cs.marmoset.modelClasses.CodeReviewThread;
import edu.umd.cs.marmoset.modelClasses.CodeReviewer;
import edu.umd.cs.marmoset.modelClasses.RubricEvaluation;
import edu.umd.cs.marmoset.review.UniqueReviewerName;

/**
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
public class DropdownEvaluationDto extends RubricEvaluationDto {

  /** @deprecated GWT only. */
  @SuppressWarnings("unused") @Deprecated
  private DropdownEvaluationDto() {
    super();
  }

	public DropdownEvaluationDto(DropdownRubricDto rubric,
	                             @RubricEvaluation.PK int rubricEvaluationPK,
	                             @CodeReviewer.PK int authorPK,
	                             @UniqueReviewerName String authorName,
	                             @CodeReviewThread.PK int threadid,
	                             boolean editable, Status status) {
    super(rubric, rubricEvaluationPK, authorPK, authorName, threadid, editable, status);
  }

    
}

package edu.umd.review.gwt.rpc.dto;

import java.util.TreeSet;

import net.customware.gwt.dispatch.shared.Result;

import com.google.common.base.Objects;
import com.google.gwt.user.client.rpc.IsSerializable;



public class CodeReviewDto implements IsSerializable, Result {

  public static CodeReviewDto create( ReviewerDto reviewer, TreeSet<RubricDto> unscoredRubrics) {
    return new CodeReviewDto(reviewer, unscoredRubrics);
  }

  public  ReviewerDto reviewer;
  public  TreeSet<RubricDto> unscoredRubrics;

  /** @deprecated GWT use only. */
  @Deprecated
  @SuppressWarnings("unused")
  private CodeReviewDto() {

  }

  CodeReviewDto(ReviewerDto reviewer, TreeSet<RubricDto> unscoredRubrics) {
    this.reviewer = reviewer;
    this.unscoredRubrics = unscoredRubrics;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CodeReviewDto other = (CodeReviewDto) obj;
    if (reviewer == null) {
      if (other.reviewer != null) {
        return false;
      }
    } else if (!reviewer.equals(other.reviewer)) {
      return false;
    }
    if (unscoredRubrics == null) {
      if (other.unscoredRubrics != null) {
        return false;
      }
    } else if (!unscoredRubrics.equals(other.unscoredRubrics)) {
      return false;
    }
    return true;
  }

  public ReviewerDto getReviewerDto() {
    return reviewer;
  }

  public TreeSet<RubricDto> getUnscoredRubrics() {
    return unscoredRubrics;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((reviewer == null) ? 0 : reviewer.hashCode());
    result = prime * result + ((unscoredRubrics == null) ? 0 : unscoredRubrics.hashCode());
    return result;
  }

  @Override
  public String toString() {
		return Objects.toStringHelper(this)
		              .add("reviewer", reviewer)
		              .add("unscoredRubrics", unscoredRubrics)
		              .toString();
  }
}

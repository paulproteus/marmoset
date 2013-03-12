package edu.umd.review.gwt.rpc.dto;

import javax.annotation.Nonnull;

import com.google.common.base.Objects;
import com.google.gwt.user.client.rpc.IsSerializable;

import edu.umd.cs.marmoset.modelClasses.CodeReviewThread;
import edu.umd.cs.marmoset.modelClasses.CodeReviewer;
import edu.umd.cs.marmoset.modelClasses.Rubric;
import edu.umd.cs.marmoset.modelClasses.RubricEvaluation;
import edu.umd.cs.marmoset.review.UniqueReviewerName;

/**
 * Abstract base class for all rubric score DTOs. A rubric score is associated with a single thread.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 * @param <Value> The type of the values used to score the rubric, e.g. {@code Integer} for a
 *          numeric rubric.
 *
 */
public abstract class RubricEvaluationDto implements IsSerializable, Comparable<RubricEvaluationDto> {

  public enum Status implements IsSerializable {
    NEW,
    DRAFT,
    LIVE,
    DEAD,
    ;

    public boolean isEditable() {
      return (this == NEW || this == DRAFT);
    }
  }

  private RubricDto rubric;
  private @RubricEvaluation.PK int rubricEvaluationPK;
  private boolean editable = false;
  private String value;
  private @Nonnull String explanation = "";
  private @CodeReviewThread.PK int threadid;
  private int points;
  private Status status;
  private @CodeReviewer.PK int authorPK;
  private @UniqueReviewerName String authorName;

  /** @deprecated GWT use only. */
  @Deprecated
  protected RubricEvaluationDto() {
    this(null, 0, 0xdeadbeef, "?", 0, false, Status.DRAFT);
  }

  public RubricEvaluationDto(RubricDto rubric,
                             @RubricEvaluation.PK int rubricEvaluationPK,
                             @CodeReviewer.PK int authorPK,
                             @UniqueReviewerName String authorName,
                             @CodeReviewThread.PK int threadid,
                             boolean editable,
                             Status status) {
    this.rubric = rubric;
    this.rubricEvaluationPK = rubricEvaluationPK;
    this.threadid = threadid;
    this.editable = editable;
    this.status = status;
    this.authorPK = authorPK;
    this.authorName = authorName;
  }

  public @CodeReviewThread.PK
  int getThreadid() {
    return threadid;
  }

  public RubricDto getRubric() {
    return rubric;
  }

  public @Rubric.PK
  int getRubricPK() {
    return rubric.getId();
  }

  public @RubricEvaluation.PK int getRubricEvaluationPK() {
    return rubricEvaluationPK;
  }

  public @CodeReviewer.PK int getAuthorPK() {
    return authorPK;
  }

  public @UniqueReviewerName String getAuthorName() {
      return authorName;
  }

  public String getName() {
    return rubric.getName();
  }

  public boolean isEditable() {
    return editable;
  }

  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  public void setDefaultValue() {
    if (value == null)
      setValue(rubric.getDefaultValue());
  }
  public void setValue(String value) {
    rubric.assertIsValidValue(value);
    this.value = value;
    this.points = rubric.getPointsForValue(value);
  }

  public String getValue() {
      setDefaultValue();
    return value;
  }

  public @Nonnull String getExplanation() {
    return explanation;
  }

  public void setExplanation(@Nonnull String explanation) {
    this.explanation = explanation;
  }

  public int getPoints() {
    setDefaultValue();
    return this.points;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("id", getRubricPK())
        .add("name", getName())
        .toString();
  }

  @Override
  public int compareTo(RubricEvaluationDto that) {
    return rubric.compareTo(that.getRubric());
  }
}

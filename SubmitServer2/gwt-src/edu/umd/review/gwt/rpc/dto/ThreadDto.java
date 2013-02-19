package edu.umd.review.gwt.rpc.dto;

import java.util.Collection;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import net.customware.gwt.dispatch.shared.Result;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;

import edu.umd.cs.marmoset.modelClasses.CodeReviewThread;

/**
 * DTO for a thread. A thread belongs to a single file, and has 0 or more published comments, and 0
 * or 1 draft comments.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 */
public class ThreadDto implements Result, Comparable<ThreadDto> {
  private  @CodeReviewThread.PK  int id;
  private String file;
  private int line;
  private long timestamp;
  private TreeSet<CommentDto> publishedComments = Sets.newTreeSet();
  @Nullable private CommentDto draft;
  @Nullable private RubricEvaluationDto rubricEvaluation;
  private boolean needsResponse;

  /** @deprecated GWT only. */
  @SuppressWarnings("unused")
  @Deprecated
  private ThreadDto() {
    this.id = 0xdeadbeef;
    this.file = "__NO_FILE__";
    this.line = 0xdeadbeef;
  }

  public ThreadDto( @CodeReviewThread.PK  int id, String file, int line, boolean needsResponse) {
    this.id = id;
    this.file = file;
    this.line = line;
    this.needsResponse = needsResponse;
  }

  public   @CodeReviewThread.PK  int getId() {
    return id;
  }

  @CheckForNull
  public String getFile() {
    return file;
  }

  public int getLine() {
    return line;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public long getTimestamp() {
    return this.timestamp;
  }

  public Collection<CommentDto> getPublishedComments() {
    return publishedComments;
  }

  public CommentDto getLastComment() {
    if (publishedComments.size() > 0) {
      return publishedComments.last();
    }
    return null;
  }

  public void addPublishedComment(CommentDto comment) {
    this.publishedComments.add(comment);
  }

  /**
   * Set the draft for this thread. The draft can be null, which indicates that a preexisting draft
   * has been published. Cannot set a draft on a thread that already has one.
   */
  public void setDraft(CommentDto draft) {
    if (draft != null) {
      Preconditions.checkArgument(draft.isDraft(), "Can't assign published comment " + draft.getId() + " as draft");
      if (this.draft != null) {
          GWT.log("Duplication drafts " + this.draft.getId() + " and " + draft.getId());
          if (this.draft.getId() > draft.getId())
              return;
      }
  }
    this.draft = draft;
  }

  /**
   * Return the active draft for this thread authored by the current user, if there is such a draft.
   */
  public CommentDto getDraft() {
    return this.draft;
  }

  /**
   * Set the rubric for this thread; it is an error to set a rubric for a thread that already has
   * one.
   */
  public void setRubricEvaluation(RubricEvaluationDto rubricEvaluation) {
    if (this.rubricEvaluation != null && rubricEvaluation != null) {
      throw new IllegalStateException("Can't set rubric on thread that already has one.");
    }
    this.rubricEvaluation = rubricEvaluation;
  }

  public RubricEvaluationDto getRubricEvaluation() {
    return rubricEvaluation;
  }

  /**
   * Indicate whether the thread needs a response (an acknowledge comment) from a given reviewer. A
   * reviewer may respond to a thread if all of the following hold:
   * <ol>
   * <li>There is at least 1 published comment in the thread.
   * <li>The last published comment in the thread is not an acknowledge comment.
   * <li>The last published comment in the thread is not authored by the reviewer.
   * </ol>
   */
  public boolean needsResponse() {
    return needsResponse;
  }

  public boolean isVisibleInTray() {
      return (this.draft != null || needsResponse());
  }

  @Override
  public int hashCode() {
    return this.id;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ThreadDto)) {
      return false;
    }
    return this.id == ((ThreadDto) obj).id;
  }

  /** Implements comparison inconsistent with {@code equals()}. */
  @Override
  public int compareTo(ThreadDto that) {
    return ComparisonChain.start().compare(this.line, that.line)
        .compare(this.timestamp, that.timestamp).compare(this.id, that.id).result();
  }

  public boolean isEmpty() {
    return (draft == null) && (publishedComments.isEmpty()) && (rubricEvaluation == null);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("id", getId())
        .toString();
  }
}

// Copyright 2011 rwsims@gmail.com (Ryan W Sims)
package edu.umd.review.server.dao;

import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

import edu.umd.review.gwt.rpc.dto.CommentDto;
import edu.umd.review.gwt.rpc.dto.FileDto;
import edu.umd.review.gwt.rpc.dto.ReviewerDto;
import edu.umd.review.gwt.rpc.dto.RubricDto;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto;
import edu.umd.review.gwt.rpc.dto.ThreadDto;

/**
 * Interface for DAO objects that access submissions. A DAO object is created for a given
 * user and a given submission, and can only access data that relates to that combination.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 */
public interface ReviewDao {

  /**
   * Return the {@link ReivewerDto} that uniquely identifies the review associated with this DAO
   * instance. The returned {@code ReviewerDto} contains information such as username,
   * authentication token, assigned rubrics, etc.
   *
   * @see ReviewerDto
   */
  ReviewerDto getReviewer();

  /**
   * Get all the files for the current snapshot, loaded with appropriate thread and comment data.
   */
  Collection<FileDto> getFiles();

  /**
   * Create a new thread (without comments) in the given file at a specific line.
   */
  ThreadDto createThread(String file, int line);

  /**
   * Create a new thread (without comments) in the given file at a specific line, scoring the rubric
   * named {@code rubric}.
   */
  ThreadDto createThreadWithRubric(String file, int line, RubricDto rubric);

  /**
   * Delete an empty thread. A thread can only be empty if it was created solely to hold a new draft
   * comment, which has been discarded.
   */
  void deleteEmptyThread(int threadId);

  /**
   * Create a new draft comment in a thread.
   */
  CommentDto createDraft(int threadId, boolean ack);

  /**
   * Save the contents of a draft comment, but do not publish.
   */
  void saveDraft(CommentDto draft);

  /**
   * Delete a draft comment. Cannot delete published comments.
   */
  void discardDraft(CommentDto draft);

  /** Return mapping from file names to {@code ThreadDto} objects with publishable drafts. */
  Map<String, TreeSet<ThreadDto>> getThreadsWithDrafts();

  /** Publish the drafts corresponding to the ids in {@code draftIds}.
   * @param evaluationIds TODO*/
  void publishDrafts(Collection<Integer> commentIds, Collection<Integer> evaluationIds);

  /** Publish all drafts authored by the dao's user. */
  void publishAllDrafts();

  TreeSet<RubricDto> getUnusedRubrics();

  /** Update the data referenced by {@code rubric}. */
  void updateRubricScore(RubricEvaluationDto score);

  /** Delete a score, removing the association between a rubric and a thread. */
  void deleteRubricScore(RubricEvaluationDto score);
}

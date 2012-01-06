package edu.umd.review.common.action;

import java.util.Collection;
import java.util.TreeSet;

import net.customware.gwt.dispatch.shared.Action;

import com.google.common.collect.Sets;

public class PublishAction implements Action<VoidResult> {
  private TreeSet<Integer> commentIds = Sets.newTreeSet();
  private TreeSet<Integer> evalutionIds = Sets.newTreeSet();

  private PublishAction() {}

  public PublishAction(Collection<Integer> commentIds, Collection<Integer> evaluationIds) {
    this.commentIds.addAll(commentIds);
    this.evalutionIds.addAll(evalutionIds);
  }

  public TreeSet<Integer> getCommentIds() {
    return commentIds;
  }

  public TreeSet<Integer> getEvalutionIds() {
    return evalutionIds;
  }
}

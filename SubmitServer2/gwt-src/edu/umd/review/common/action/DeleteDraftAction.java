package edu.umd.review.common.action;

import net.customware.gwt.dispatch.shared.Action;
import edu.umd.review.gwt.rpc.dto.CommentDto;

public class DeleteDraftAction implements Action<VoidResult> {
  private CommentDto draft;

  @Deprecated
  private DeleteDraftAction() {}

  public DeleteDraftAction(CommentDto draft) {
    this.draft = draft;
  }

  public CommentDto getDraft() {
    return draft;
  }
}

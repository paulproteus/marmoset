package edu.umd.review.common.action;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.dispatch.shared.Action;
import edu.umd.review.gwt.rpc.dto.CommentDto;

public class SaveDraftAction implements Action<VoidResult> {
  private CommentDto draft;

  private SaveDraftAction() {}

  public SaveDraftAction(CommentDto draft) {
    this.draft = draft;
  }

  public CommentDto getDraft() {
    return draft;
  }
}

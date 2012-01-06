package edu.umd.review.common.action;

import net.customware.gwt.dispatch.shared.Action;
import edu.umd.review.gwt.rpc.dto.CommentDto;

public class CreateDraftAction implements Action<CommentDto> {
  private int threadId;
  private boolean needsResponse;

  private CreateDraftAction() {}

  public CreateDraftAction(int threadId, boolean needsResponse) {
    this.threadId = threadId;
    this.needsResponse = needsResponse;
  }

  public int getThreadId() {
    return threadId;
  }

  public boolean needsResponse() {
    return needsResponse;
  }
}

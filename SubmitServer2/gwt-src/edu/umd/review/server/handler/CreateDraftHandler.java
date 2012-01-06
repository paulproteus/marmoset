package edu.umd.review.server.handler;

import javax.servlet.http.HttpServletRequest;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.umd.review.common.action.CreateDraftAction;
import edu.umd.review.gwt.rpc.dto.CommentDto;

public class CreateDraftHandler extends AbstractDaoHandler<CreateDraftAction, CommentDto> {

  @Inject
  protected CreateDraftHandler(Provider<HttpServletRequest> requestProvider) {
    super(requestProvider);
  }

  @Override
  public CommentDto execute(CreateDraftAction action, ExecutionContext context) throws DispatchException {
    return getDao().createDraft(action.getThreadId(), !action.needsResponse());
  }

  @Override
  public Class<CreateDraftAction> getActionType() {
    return CreateDraftAction.class;
  }
}

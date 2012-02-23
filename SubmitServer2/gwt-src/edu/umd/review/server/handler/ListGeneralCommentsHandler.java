package edu.umd.review.server.handler;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import com.google.inject.Provider;

import edu.umd.review.common.action.ListGeneralCommentsAction;
import edu.umd.review.common.action.ListGeneralCommentsAction.Response;

public class ListGeneralCommentsHandler extends AbstractDaoHandler<ListGeneralCommentsAction, ListGeneralCommentsAction.Response> {
  
  @Inject
  public ListGeneralCommentsHandler(Provider<HttpServletRequest> requestProvider) {
    super(requestProvider);
  }

  @Override
  public Response execute(ListGeneralCommentsAction action, ExecutionContext context)
      throws DispatchException {
    return new Response(getDao().getGeneralCommentThreads());
  }

  @Override
  public Class<ListGeneralCommentsAction> getActionType() {
    return ListGeneralCommentsAction.class;
  }
}

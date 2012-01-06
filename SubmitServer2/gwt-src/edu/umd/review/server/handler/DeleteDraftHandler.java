package edu.umd.review.server.handler;

import javax.servlet.http.HttpServletRequest;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.umd.review.common.action.DeleteDraftAction;
import edu.umd.review.common.action.VoidResult;

public class DeleteDraftHandler extends AbstractDaoHandler<DeleteDraftAction, VoidResult> {

  @Inject
  protected DeleteDraftHandler(Provider<HttpServletRequest> requestProvider) {
    super(requestProvider);
  }

  @Override
  public VoidResult execute(DeleteDraftAction action, ExecutionContext context) throws DispatchException {
    getDao().discardDraft(action.getDraft());
    return VoidResult.get();
  }

  @Override
  public Class<DeleteDraftAction> getActionType() {
    return DeleteDraftAction.class;
  }
}

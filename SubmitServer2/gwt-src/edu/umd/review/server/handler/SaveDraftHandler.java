package edu.umd.review.server.handler;

import javax.servlet.http.HttpServletRequest;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.umd.review.common.action.SaveDraftAction;
import edu.umd.review.common.action.VoidResult;

public class SaveDraftHandler extends AbstractDaoHandler<SaveDraftAction, VoidResult> {

  @Inject
  protected SaveDraftHandler(Provider<HttpServletRequest> requestProvider) {
    super(requestProvider);
  }

  @Override
  public VoidResult execute(SaveDraftAction action, ExecutionContext context) throws DispatchException {
    getDao().saveDraft(action.getDraft());
    return VoidResult.get();
  }

  @Override
  public Class<SaveDraftAction> getActionType() {
    return SaveDraftAction.class;
  }
}

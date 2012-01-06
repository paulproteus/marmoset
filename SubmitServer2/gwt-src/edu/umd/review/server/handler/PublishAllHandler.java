package edu.umd.review.server.handler;

import javax.servlet.http.HttpServletRequest;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.umd.review.common.action.PublishAllAction;
import edu.umd.review.common.action.VoidResult;

public class PublishAllHandler extends AbstractDaoHandler<PublishAllAction, VoidResult> {

  @Inject
  protected PublishAllHandler(Provider<HttpServletRequest> requestProvider) {
    super(requestProvider);
  }

  @Override
  public VoidResult execute(PublishAllAction action, ExecutionContext context) throws DispatchException {
    getDao().publishAllDrafts();
    return VoidResult.get();
  }

  @Override
  public Class<PublishAllAction> getActionType() {
    return PublishAllAction.class;
  }
}

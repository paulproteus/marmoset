package edu.umd.review.server.handler;

import javax.servlet.http.HttpServletRequest;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.umd.review.common.action.PublishAction;
import edu.umd.review.common.action.VoidResult;

public class PublishHandler extends AbstractDaoHandler<PublishAction, VoidResult> {

  @Inject
  protected PublishHandler(Provider<HttpServletRequest> requestProvider) {
    super(requestProvider);
  }

  @Override
  public VoidResult execute(PublishAction action, ExecutionContext context) throws DispatchException {
    getDao().publishDrafts(action.getCommentIds(), action.getEvalutionIds());
    return VoidResult.get();
  }

  @Override
  public Class<PublishAction> getActionType() {
    return PublishAction.class;
  }

}

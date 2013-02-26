package edu.umd.review.server.handler;

import javax.servlet.http.HttpServletRequest;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.umd.review.common.action.PublishAllAction;
import edu.umd.review.common.action.RateReviewerAction;
import edu.umd.review.common.action.VoidResult;

public class RateReviewerHandler extends AbstractDaoHandler<RateReviewerAction, VoidResult> {

  @Inject
  protected RateReviewerHandler(Provider<HttpServletRequest> requestProvider) {
    super(requestProvider);
  }

  @Override
  public VoidResult execute(RateReviewerAction action, ExecutionContext context) throws DispatchException {
    getDao().rateReviewer(action.getReviewerName(), action.getRating());
    return VoidResult.get();
  }

  @Override
  public Class<RateReviewerAction> getActionType() {
    return RateReviewerAction.class;
  }
}

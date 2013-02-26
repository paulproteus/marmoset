package edu.umd.review.server.handler;

import javax.servlet.http.HttpServletRequest;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.umd.review.common.action.GetCodeReviewAction;
import edu.umd.review.common.action.GetCodeReviewAction.Result;
import edu.umd.review.server.dao.ReviewDao;

public class GetFilesHandler extends AbstractDaoHandler<GetCodeReviewAction, GetCodeReviewAction.Result> {

  @Inject
  GetFilesHandler(Provider<HttpServletRequest> requestProvider) {
    super(requestProvider);
  }

  @Override
  public Result execute(GetCodeReviewAction action, ExecutionContext context) throws DispatchException {
    ReviewDao dao = getDao();
	return new GetCodeReviewAction.Result(dao.getFiles(), dao.getRatings());
  }

  @Override
  public Class<GetCodeReviewAction> getActionType() {
    return GetCodeReviewAction.class;
  }
}

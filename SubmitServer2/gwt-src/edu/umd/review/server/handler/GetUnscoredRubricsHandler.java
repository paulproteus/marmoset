package edu.umd.review.server.handler;

import javax.servlet.http.HttpServletRequest;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.umd.review.common.action.GetUnscoredRubrics;
import edu.umd.review.common.action.GetUnscoredRubrics.Result;

public class GetUnscoredRubricsHandler extends
    AbstractDaoHandler<GetUnscoredRubrics, GetUnscoredRubrics.Result> {

  @Inject
  GetUnscoredRubricsHandler(Provider<HttpServletRequest> requestProvider) {
    super(requestProvider);
  }

  @Override
  public Result execute(GetUnscoredRubrics action, ExecutionContext context)
      throws DispatchException {
    return new Result(getDao().getUnusedRubrics());
  }

  @Override
  public Class<GetUnscoredRubrics> getActionType() {
    return GetUnscoredRubrics.class;
  }
}

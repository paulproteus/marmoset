package edu.umd.review.server.handler;

import javax.servlet.http.HttpServletRequest;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.umd.review.common.action.GetFilesAction;
import edu.umd.review.common.action.GetFilesAction.Result;

public class GetFilesHandler extends AbstractDaoHandler<GetFilesAction, GetFilesAction.Result> {

  @Inject
  GetFilesHandler(Provider<HttpServletRequest> requestProvider) {
    super(requestProvider);
  }

  @Override
  public Result execute(GetFilesAction action, ExecutionContext context) throws DispatchException {
    return new GetFilesAction.Result(getDao().getFiles());
  }

  @Override
  public Class<GetFilesAction> getActionType() {
    return GetFilesAction.class;
  }
}

package edu.umd.review.server.handler;

import javax.servlet.http.HttpServletRequest;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.umd.review.common.action.NewThreadAction;
import edu.umd.review.gwt.rpc.dto.ThreadDto;

public class NewThreadHandler extends AbstractDaoHandler<NewThreadAction, ThreadDto> {

  @Inject
  NewThreadHandler(Provider<HttpServletRequest> requestProvider) {
    super(requestProvider);
  }

  @Override
  public ThreadDto execute(NewThreadAction action, ExecutionContext context)
      throws DispatchException {
    if (action.getRubric() != null) {
      return getDao().createThreadWithRubric(action.getPath(), action.getLine(), action.getRubric());
    }
    return getDao().createThread(action.getPath(), action.getLine());
  }

  @Override
  public Class<NewThreadAction> getActionType() {
    return NewThreadAction.class;
  }
}

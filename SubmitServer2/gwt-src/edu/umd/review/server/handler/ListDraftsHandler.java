package edu.umd.review.server.handler;

import java.util.Map.Entry;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.umd.review.common.action.ListDraftsAction;
import edu.umd.review.common.action.ListDraftsAction.Result;
import edu.umd.review.gwt.rpc.dto.ThreadDto;

public class ListDraftsHandler extends AbstractDaoHandler<ListDraftsAction, ListDraftsAction.Result> {

  @Inject
  protected ListDraftsHandler(Provider<HttpServletRequest> requestProvider) {
    super(requestProvider);
  }

  @Override
  public Result execute(ListDraftsAction action, ExecutionContext context) throws DispatchException {
    Result result = new ListDraftsAction.Result();
    for (Entry<String, TreeSet<ThreadDto>> entry : getDao().getThreadsWithDrafts().entrySet()) {
      result.addThreads(entry.getKey(), entry.getValue());
    }
    return result;
  }

  @Override
  public Class<ListDraftsAction> getActionType() {
    return ListDraftsAction.class;
  }
}

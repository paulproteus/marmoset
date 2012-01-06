package edu.umd.review.server.handler;

import javax.servlet.http.HttpServletRequest;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.umd.review.common.action.DeleteRubricEvaluationAction;
import edu.umd.review.common.action.VoidResult;

public class DeleteRubricEvaluationHandler extends
    AbstractDaoHandler<DeleteRubricEvaluationAction, VoidResult> {

  @Inject
  protected DeleteRubricEvaluationHandler(Provider<HttpServletRequest> requestProvider) {
    super(requestProvider);
  }

  @Override
  public VoidResult execute(DeleteRubricEvaluationAction action, ExecutionContext context)
      throws DispatchException {
    getDao().deleteRubricScore(action.getEvaluation());
    return VoidResult.get();
  }

  @Override
  public Class<DeleteRubricEvaluationAction> getActionType() {
    return DeleteRubricEvaluationAction.class;
  }
}

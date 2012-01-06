package edu.umd.review.server.handler;

import javax.servlet.http.HttpServletRequest;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.umd.review.common.action.SaveRubricEvaluationAction;
import edu.umd.review.common.action.VoidResult;

public class SaveRubricEvaluationHandler extends
    AbstractDaoHandler<SaveRubricEvaluationAction, VoidResult> {

  @Inject
  SaveRubricEvaluationHandler(Provider<HttpServletRequest> requestProvider) {
    super(requestProvider);
  }

  @Override
  public VoidResult execute(SaveRubricEvaluationAction action, ExecutionContext context)
      throws DispatchException {
    getDao().updateRubricScore(action.getEvaluation());
    return VoidResult.get();
  }

  @Override
  public Class<SaveRubricEvaluationAction> getActionType() {
    return SaveRubricEvaluationAction.class;
  }
}

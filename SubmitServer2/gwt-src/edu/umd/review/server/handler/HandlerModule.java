package edu.umd.review.server.handler;

import net.customware.gwt.dispatch.server.guice.ActionHandlerModule;
import edu.umd.review.common.action.CreateDraftAction;
import edu.umd.review.common.action.DeleteDraftAction;
import edu.umd.review.common.action.DeleteRubricEvaluationAction;
import edu.umd.review.common.action.GetFilesAction;
import edu.umd.review.common.action.GetUnscoredRubrics;
import edu.umd.review.common.action.ListDraftsAction;
import edu.umd.review.common.action.ListGeneralCommentsAction;
import edu.umd.review.common.action.NewThreadAction;
import edu.umd.review.common.action.PublishAction;
import edu.umd.review.common.action.PublishAllAction;
import edu.umd.review.common.action.SaveDraftAction;
import edu.umd.review.common.action.SaveRubricEvaluationAction;

public class HandlerModule extends ActionHandlerModule {
  @Override
  protected void configureHandlers() {
    bindHandler(GetFilesAction.class, GetFilesHandler.class);
    bindHandler(GetUnscoredRubrics.class, GetUnscoredRubricsHandler.class);
    bindHandler(PublishAllAction.class, PublishAllHandler.class);
    bindHandler(CreateDraftAction.class, CreateDraftHandler.class);
    bindHandler(NewThreadAction.class, NewThreadHandler.class);
    bindHandler(SaveDraftAction.class, SaveDraftHandler.class);
    bindHandler(DeleteDraftAction.class, DeleteDraftHandler.class);
    bindHandler(ListDraftsAction.class, ListDraftsHandler.class);
    bindHandler(SaveRubricEvaluationAction.class, SaveRubricEvaluationHandler.class);
    bindHandler(DeleteRubricEvaluationAction.class, DeleteRubricEvaluationHandler.class);
    bindHandler(PublishAction.class, PublishHandler.class);
    bindHandler(ListGeneralCommentsAction.class, ListGeneralCommentsHandler.class);
  }
}

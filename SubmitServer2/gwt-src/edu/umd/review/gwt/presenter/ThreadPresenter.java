package edu.umd.review.gwt.presenter;

import java.util.logging.Logger;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.event.shared.ResettableEventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import edu.umd.review.common.action.CreateDraftAction;
import edu.umd.review.common.action.SaveDraftAction;
import edu.umd.review.common.action.VoidResult;
import edu.umd.review.gwt.GwtUtils;
import edu.umd.review.gwt.PresenterFactory;
import edu.umd.review.gwt.event.DiscardCommentEvent;
import edu.umd.review.gwt.event.DraftSavedEvent;
import edu.umd.review.gwt.event.NewDraftEvent;
import edu.umd.review.gwt.event.RubricAssignedEvent;
import edu.umd.review.gwt.event.RubricDiscardEvent;
import edu.umd.review.gwt.event.ThreadDiscardEvent;
import edu.umd.review.gwt.rpc.dto.CheckboxEvaluationDto;
import edu.umd.review.gwt.rpc.dto.CheckboxRubricDto;
import edu.umd.review.gwt.rpc.dto.CommentDto;
import edu.umd.review.gwt.rpc.dto.DropdownEvaluationDto;
import edu.umd.review.gwt.rpc.dto.DropdownRubricDto;
import edu.umd.review.gwt.rpc.dto.NumericEvaluationDto;
import edu.umd.review.gwt.rpc.dto.NumericRubricDto;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto.Status;
import edu.umd.review.gwt.rpc.dto.ThreadDto;
import edu.umd.review.gwt.view.CheckboxRubricEvaluationView;
import edu.umd.review.gwt.view.DraftView;
import edu.umd.review.gwt.view.DropdownRubricEvaluationView;
import edu.umd.review.gwt.view.NumericRubricEvaluationView;
import edu.umd.review.gwt.view.ScrollManager;
import edu.umd.review.gwt.view.ThreadView;

/**
 * Presenter that drives the view of a single thread.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public class ThreadPresenter extends AbstractPresenter implements ThreadView.Presenter,
    DiscardCommentEvent.Handler, RubricDiscardEvent.Handler {
  private static final Logger logger = Logger.getLogger(ThreadPresenter.class.getName());

  private final ThreadDto thread;
  private final ThreadView view;
  private final PresenterFactory presenterFactory;
  private final DispatchAsync dispatch;
  private final EventBus eventBus;
  private final ResettableEventBus eventBusWrapper;
  private final ScrollManager scrollManager;
  private DraftView.Presenter draftPresenter;

  @Inject
  public ThreadPresenter(@Assisted ThreadView view,
                         DispatchAsync dispatch,
                         EventBus eventBus,
                         PresenterFactory presenterFactory,
                         ScrollManager scrollManager,
                         @Assisted ThreadDto thread) {
    this.thread = thread;
    this.view = view;
    this.dispatch = dispatch;
    this.eventBus = eventBus;
    this.eventBusWrapper = new ResettableEventBus(eventBus);
    this.presenterFactory = presenterFactory;
    this.scrollManager = scrollManager;
  }

  @Override
  public void onReply() {
    if (thread.getDraft() == null) {
      startNewDraft();
    } else {
      view.getDraftView().editDraft(thread.getDraft().getContents());
    }
  }

  @Override
  public void start() {
    eventBusWrapper.addHandler(DiscardCommentEvent.TYPE, this);
    eventBusWrapper.addHandler(RubricDiscardEvent.TYPE, this);
    view.setPresenter(this);
    scrollManager.registerThread(thread, view);
    view.showAcknowledge(thread.needsResponse());
    view.setComments(thread.getPublishedComments());
    RubricEvaluationDto evaluation = thread.getRubricEvaluation();
    if (evaluation != null) {
      if (evaluation.getStatus() != Status.DEAD) {
        eventBus.fireEvent(new RubricAssignedEvent(evaluation));
      }
      showRubricEvaluation(evaluation);
    }
    if (thread.isEmpty()) {
      startNewDraft();
      return;
    }

    view.showReplyLink(!thread.getPublishedComments().isEmpty() || evaluation != null
        && evaluation.getStatus() != Status.DRAFT && evaluation.getStatus() != Status.NEW);
    if (thread.getDraft() != null) {
      openDraft(thread.getDraft());
    } else {
      view.setDraftEditorVisible(false);
    }
    view.setThreadOpen(thread.needsResponse());
  }

  private void showRubricEvaluation(RubricEvaluationDto rubricEvaluation) {
    if (rubricEvaluation instanceof NumericEvaluationDto) {
      NumericRubricEvaluationView evalView = view.getNumericEvaluationView();
      NumericRubricEvaluationView.Presenter evalPresenter = presenterFactory.makeNumericEvaluationView(evalView,
                                                                                                       (NumericRubricDto) rubricEvaluation.getRubric(),
                                                                                                       (NumericEvaluationDto) rubricEvaluation);
      evalPresenter.start();
    } else if (rubricEvaluation instanceof CheckboxEvaluationDto) {
      CheckboxRubricEvaluationView evalView = view.getCheckboxEvaluationView();
      CheckboxRubricEvaluationView.Presenter evalPresenter = presenterFactory.makeCheckboxEvaluationView(evalView,
                                                                                                         (CheckboxRubricDto) rubricEvaluation.getRubric(),
                                                                                                         (CheckboxEvaluationDto) rubricEvaluation);
      evalPresenter.start();
    } else if (rubricEvaluation instanceof DropdownEvaluationDto) {
      DropdownRubricEvaluationView evalView = view.getDropdownEvaluationView();
      DropdownRubricEvaluationView.Presenter evalPresenter = presenterFactory.makeDropdownEvaluationView(evalView,
                                                                                                         (DropdownRubricDto) rubricEvaluation.getRubric(),
                                                                                                         (DropdownEvaluationDto) rubricEvaluation);
      evalPresenter.start();
    } else
      throw new UnsupportedOperationException();
  }

  private CreateDraftAction createDraftAction(boolean needsResponse) {
    return new CreateDraftAction(thread.getId(), needsResponse);
  }

  private void startNewDraft() {
    view.waitForDraft();
    dispatch.execute(createDraftAction(true), new AsyncCallback<CommentDto>() {
      @Override
      public void onFailure(Throwable caught) {
        GwtUtils.wrapAndThrow(caught);
      }

      @Override
      public void onSuccess(CommentDto draft) {
        openDraft(draft);
        thread.setDraft(draft);
        eventBus.fireEvent(new NewDraftEvent(draft, thread));
      }
    });
  }

  @Override
  public void onDiscardComment(DiscardCommentEvent event) {
    CommentDto draft = event.getDraft();
    if (draft.getThreadId() != thread.getId()) {
      return;
    }
    thread.setDraft(null);
    view.setDraftEditorVisible(false);
    discardThreadIfEmpty();
  }

  @Override
  public void acknowledge() {
    dispatch.execute(createDraftAction(true), new AsyncCallback<CommentDto>() {
      @Override
      public void onFailure(Throwable caught) {
        GwtUtils.wrapAndThrow(caught);
      }

      @Override
      public void onSuccess(CommentDto draft) {
        eventBus.fireEvent(new NewDraftEvent(draft, thread));
        logger.info("Starting new ack draft " + draft);
        draft.setContents("Acknowledged.");
        saveDraft(draft);
      }
    });
  }

  private void saveDraft(final CommentDto draft) {
    dispatch.execute(new SaveDraftAction(draft), new AsyncCallback<VoidResult>() {
      @Override
      public void onFailure(Throwable caught) {
        GwtUtils.wrapAndThrow(caught);
      }

      @Override
      public void onSuccess(VoidResult result) {
        eventBus.fireEvent(new DraftSavedEvent(draft));
        openDraft(draft);
        thread.setDraft(draft);
      }
    });
  }

  private void openDraft(CommentDto draft) {
    DraftView draftView = view.getDraftView();
    draftPresenter = presenterFactory.makeDraftPresenter(draftView, thread, draft);
    draftPresenter.start();
    view.setDraftEditorVisible(true);
  }

  @Override
  public void finish() {
    eventBusWrapper.removeHandlers();
    if (draftPresenter != null) {
      draftPresenter.finish();
      draftPresenter = null;
    }
  }

  @Override
  public void onRubricDiscard(RubricDiscardEvent event) {
    RubricEvaluationDto evaluation = event.getRubric();
    if (evaluation.getThreadid() != thread.getId()) {
      return;
    }
    if (evaluation.getStatus() == Status.DRAFT || evaluation.getStatus() == Status.NEW) {
      view.clearRubricEvaluationView();
      thread.setRubricEvaluation(null);
      discardThreadIfEmpty();
    }
  }

  private void discardThreadIfEmpty() {
    if (!thread.isEmpty()) {
      return;
    }
    view.asWidget().removeFromParent();
    scrollManager.unregisterThread(thread);
    //rpcService.deleteEmptyThread(thread.getId());
    eventBus.fireEvent(new ThreadDiscardEvent(thread));
  }
}

package edu.umd.review.gwt.presenter;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Logger;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.allen_sauer.gwt.dnd.client.DragController;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.event.shared.ResettableEventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import edu.umd.review.common.action.GetUnscoredRubrics;
import edu.umd.review.common.action.GetUnscoredRubrics.Result;
import edu.umd.review.common.action.PublishAllAction;
import edu.umd.review.common.action.VoidResult;
import edu.umd.review.gwt.GwtUtils;
import edu.umd.review.gwt.PresenterFactory;
import edu.umd.review.gwt.event.DiscardCommentEvent;
import edu.umd.review.gwt.event.EvaluationSavedEvent;
import edu.umd.review.gwt.event.NewDraftEvent;
import edu.umd.review.gwt.event.NewRubricEvaluationEvent;
import edu.umd.review.gwt.event.PublishAllEvent;
import edu.umd.review.gwt.event.RubricAssignedEvent;
import edu.umd.review.gwt.event.RubricDiscardEvent;
import edu.umd.review.gwt.event.SessionExpiryEvent;
import edu.umd.review.gwt.event.ThreadDiscardEvent;
import edu.umd.review.gwt.rpc.dto.FileDto;
import edu.umd.review.gwt.rpc.dto.RubricDto;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto.Status;
import edu.umd.review.gwt.view.TrayFileView;
import edu.umd.review.gwt.view.TrayView;

/**
 * Presenter to drive {@link TrayView}.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
@Singleton
public class TrayPresenter extends AbstractPresenter implements TrayView.Presenter,
    DiscardCommentEvent.Handler, NewDraftEvent.Handler, RubricAssignedEvent.Handler,
    RubricDiscardEvent.Handler, ThreadDiscardEvent.Handler, NewRubricEvaluationEvent.Handler,
    EvaluationSavedEvent.Handler, SessionExpiryEvent.Handler {
  private static final Logger logger = Logger.getLogger(TrayPresenter.class.getName());
  private final Collection<? extends FileDto> files;
  private final TrayView view;
  private final PresenterFactory presenterFactory;
  private final EventBus eventBus;
  private final DragController dragController;
  private final Map<String, TrayFileView.Presenter> filePresenters = Maps.newTreeMap();
  private final ResettableEventBus eventBusWrapper;
  private final DispatchAsync dispatch;

  private int draftCount = 0;
  private boolean expired = false;

  private SortedSet<RubricDto> unscoredRubrics = Sets.newTreeSet();
  private SortedSet<RubricEvaluationDto> scoredEvaluations = Sets.newTreeSet();

  @Inject
  public TrayPresenter(@Assisted TrayView trayView,
                       DispatchAsync dispatch,
                       PickupDragController dragController,
                       PresenterFactory presenterFactory,
                       EventBus eventBus,
                       @Assisted Collection<? extends FileDto> files) {
    this.files = files;
    this.view = trayView;
    this.presenterFactory = presenterFactory;
    this.eventBus = eventBus;
    this.eventBusWrapper = new ResettableEventBus(eventBus);
    this.dragController = dragController;
    this.dispatch = dispatch;
  }

  @Override
  public void start() {
    view.reset();
    view.setPresenter(this);
    eventBusWrapper.addHandler(NewDraftEvent.TYPE, this);
    eventBusWrapper.addHandler(DiscardCommentEvent.TYPE, this);
    eventBusWrapper.addHandler(RubricAssignedEvent.TYPE, this);
    eventBusWrapper.addHandler(RubricDiscardEvent.TYPE, this);
    eventBusWrapper.addHandler(ThreadDiscardEvent.TYPE, this);
    eventBusWrapper.addHandler(NewRubricEvaluationEvent.getType(), this);
    eventBusWrapper.addHandler(EvaluationSavedEvent.TYPE, this);
    eventBusWrapper.addHandler(SessionExpiryEvent.getType(), this);
    draftCount = 0;
    for (FileDto file : files) {
      view.insertAuthors(file);
      TrayFileView fileView = view.insertFile(null);
      TrayFileView.Presenter presenter = presenterFactory.makeTrayFilePresenter(fileView, file);
      presenter.start();
      filePresenters.put(file.getPath(), presenter);
    }
    view.setUnpublished(draftCount > 0);
    dispatch.execute(new GetUnscoredRubrics(), new AsyncCallback<GetUnscoredRubrics.Result>() {
      @Override
      public void onFailure(Throwable caught) {
        throw GwtUtils.wrapAndThrow(caught);
      }

      @Override
      public void onSuccess(Result result) {
        unscoredRubrics.addAll(result.getRubrics());
        displayRubrics();
      }
    });
  }

  private void displayRubrics() {
    view.clearRubrics();
    // TODO(rwsims): need to unregister the draggable widgets?
    for (final RubricDto rubric : unscoredRubrics) {
      Widget unscoredWidget = view.addUnscoredRubric(rubric);
      dragController.makeDraggable(unscoredWidget);
    }
    for (RubricEvaluationDto evaluation : scoredEvaluations) {
      view.addScoredRubric(evaluation.getRubric(), evaluation);
    }
  }

  @Override
  public void publishAllDrafts() {
    dispatch.execute(new PublishAllAction(), new AsyncCallback<VoidResult>() {
      @Override
      public void onFailure(Throwable caught) {
        throw GwtUtils.wrapAndThrow(caught);
      }

      @Override
      public void onSuccess(VoidResult result) {
        eventBus.fireEvent(new PublishAllEvent());
      }
    });
  }

  @Override
  public void onDiscardComment(DiscardCommentEvent event) {
    draftCount -= 1;
    if (draftCount == 0) {
      view.setUnpublished(false);
    }
  }

  @Override
  public void onNewDraft(NewDraftEvent event) {
    logger.info("Adding new draft.");
    draftCount += 1;
    view.setUnpublished(true);
  }

  @Override
  public void onNewRubricEvent(NewRubricEvaluationEvent event) {
    logger.info("Adding new rubric eval.");
    draftCount += 1;
    view.setUnpublished(true);
  }

  @Override
  public String getCloseMessage() {
    if (expired || draftCount == 0) {
      return null;
    }
    return "You have unpublished drafts - are you sure you want to leave without publishing?";
  }

  @Override
  public void finish() {
    eventBusWrapper.removeHandlers();
    Iterator<TrayFileView.Presenter> iter = filePresenters.values().iterator();
    while (iter.hasNext()) {
      TrayFileView.Presenter p = iter.next();
      iter.remove();
      p.finish();
    }
  }

  @Override
  public void onRubricAssigned(RubricAssignedEvent event) {
    RubricEvaluationDto rubric = event.getEvaluation();
    if (rubric.getStatus() == Status.DEAD) {
      return;
    }
    unscoredRubrics.remove(event.getEvaluation().getRubric());
    scoredEvaluations.add(event.getEvaluation());
    displayRubrics();
  }

  @Override
  public void onRubricDiscard(RubricDiscardEvent event) {
    discardRubric(event.getRubric());
  }

  private void discardRubric(RubricEvaluationDto rubricEvaluation) {
    RubricDto rubric = rubricEvaluation.getRubric();
    unscoredRubrics.add(rubric);
    scoredEvaluations.remove(rubricEvaluation);
    displayRubrics();
    if (rubricEvaluation.getStatus() == Status.DRAFT || rubricEvaluation.getStatus() == Status.NEW) {
      draftCount--;
    }
    if (draftCount == 0) {
      view.setUnpublished(false);
    }
  }

  @Override
  public void onThreadDiscard(ThreadDiscardEvent event) {
    if (event.getThread().getRubricEvaluation() != null) {
      discardRubric(event.getThread().getRubricEvaluation());
    }
  }

  @Override
  public void onEvaluationSaved(EvaluationSavedEvent event) {
    displayRubrics();
  }

  @Override
  public void onSessionExpiry(SessionExpiryEvent event) {
    expired = true;
  }
}

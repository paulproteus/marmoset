package edu.umd.review.gwt.presenter;

import java.util.logging.Logger;

import com.google.gwt.event.shared.ResettableEventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import edu.umd.review.gwt.event.DiscardCommentEvent;
import edu.umd.review.gwt.event.DraftSavedEvent;
import edu.umd.review.gwt.event.NewDraftEvent;
import edu.umd.review.gwt.rpc.dto.CommentDto;
import edu.umd.review.gwt.rpc.dto.ThreadDto;
import edu.umd.review.gwt.view.ScrollManager;
import edu.umd.review.gwt.view.TrayThreadView;

/**
 * Presenter for {@link TrayThreadView}.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public class TrayThreadPresenter extends AbstractPresenter implements TrayThreadView.Presenter,
    DraftSavedEvent.Handler, DiscardCommentEvent.Handler, NewDraftEvent.Handler {
  private static final Logger logger = Logger.getLogger(TrayThreadPresenter.class.getName());
  private final TrayThreadView view;
  private final ThreadDto thread;
  private final ResettableEventBus eventBus;
  private final ScrollManager scrollManager;

  @Inject
  TrayThreadPresenter(@Assisted TrayThreadView view, EventBus eventBus, ScrollManager scrollManager,
      @Assisted ThreadDto thread) {
    this.view = view;
    this.thread = thread;
    this.eventBus = new ResettableEventBus(eventBus);
    this.scrollManager = scrollManager;
  }

  @Override
  public void onThreadClicked() {
    scrollManager.scrollThread(thread);
  }

  @Override
  public void start() {
    view.setPresenter(this);
    updateThreadView();
    eventBus.addHandler(DraftSavedEvent.TYPE, this);
    eventBus.addHandler(DiscardCommentEvent.TYPE, this);
    eventBus.addHandler(NewDraftEvent.TYPE, this);
  }

  private void updateThreadView() {
    if (thread.isVisibleInTray()) {
      view.setThread(thread);
    }
    view.asWidget().setVisible(thread.isVisibleInTray());
  }

  @Override
  public void onDraftSaved(DraftSavedEvent event) {
    CommentDto draft = event.getDraft();
    if (draft.getThreadId() == thread.getId()) {
      updateThreadView();
    }
  }

  @Override
  public void onDiscardComment(DiscardCommentEvent event) {
    CommentDto draft = event.getDraft();
    if (draft.getThreadId() == thread.getId()) {
      thread.setDraft(null);
      updateThreadView();
    }
  }

  @Override
  public void finish() {
    eventBus.removeHandlers();
  }

    @Override
  public void onNewDraft(NewDraftEvent event) {
      if (!event.getThread().equals(event.getThread())) {
          return;
      }
      updateThreadView();
  }
}

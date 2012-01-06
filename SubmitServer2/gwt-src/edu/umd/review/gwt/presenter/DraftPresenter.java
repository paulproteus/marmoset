package edu.umd.review.gwt.presenter;

import java.util.logging.Logger;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.common.base.Strings;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import edu.umd.review.common.action.DeleteDraftAction;
import edu.umd.review.common.action.SaveDraftAction;
import edu.umd.review.common.action.VoidResult;
import edu.umd.review.gwt.GwtUtils;
import edu.umd.review.gwt.event.DiscardCommentEvent;
import edu.umd.review.gwt.event.DraftSavedEvent;
import edu.umd.review.gwt.rpc.dto.CommentDto;
import edu.umd.review.gwt.rpc.dto.ThreadDto;
import edu.umd.review.gwt.view.DraftView;

/**
 * Implementation of {@link DraftView.Presenter}.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public class DraftPresenter extends AbstractPresenter implements DraftView.Presenter {
  private static final Logger logger = Logger.getLogger(DraftPresenter.class.getName());
  private final DraftView view;
  private final CommentDto draft;
  private final EventBus eventBus;
  private final ThreadDto thread;
  private final DispatchAsync dispatch;

  @Inject
  DraftPresenter(@Assisted DraftView view,
                 EventBus eventBus,
                 DispatchAsync dispatch,
                 @Assisted ThreadDto thread,
                 @Assisted CommentDto draft) {
    this.view = view;
    this.eventBus = eventBus;
    this.draft = draft;
    this.thread = thread;
    this.dispatch = dispatch;
  }

  @Override
  public void onSave(String text, boolean isAck) {
    if (Strings.isNullOrEmpty(text)) {
      deleteDraft();
    } else {
      draft.setContents(text);
      draft.setAcknowledgement(isAck);
      view.startSave(draft.getContents());
      dispatch.execute(new SaveDraftAction(draft), new AsyncCallback<VoidResult>() {
        @Override
        public void onFailure(Throwable caught) {
          GwtUtils.wrapAndThrow(caught);
        }

        @Override
        public void onSuccess(VoidResult result) {
          view.finishSave(draft.getContents());
          eventBus.fireEvent(new DraftSavedEvent(draft));
        }
      });
    }
  }

  private void doEdit() {
    view.editDraft(draft.getContents());
  }

  /** Do not call this when saving drafts with an RPC. Use the start/finish calls to the view. */
  private void doPreview() {
    view.previewDraft(draft.getContents());
  }

  private void deleteDraft() {
    dispatch.execute(new DeleteDraftAction(draft), new AsyncCallback<VoidResult>() {

      @Override
      public void onFailure(Throwable caught) {
        GwtUtils.wrapAndThrow(caught);
      }

      @Override
      public void onSuccess(VoidResult result) {
        eventBus.fireEvent(new DiscardCommentEvent(draft));
      }

    });
  }

  @Override
  public void onEdit() {
    doEdit();
  }

  @Override
  public void onCancel() {
    if (Strings.isNullOrEmpty(draft.getContents())) {
      deleteDraft();
    } else {
      doPreview();
    }
  }

  @Override
  public void onDiscard() {
    deleteDraft();
  }

  @Override
  public void start() {
    view.setPresenter(this);
    view.setAck(draft.isAcknowledgement());
    if (Strings.isNullOrEmpty(draft.getContents())) {
      draft.setContents("");
      doEdit();
    } else {
      doPreview();
    }
  }
}

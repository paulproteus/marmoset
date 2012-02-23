package edu.umd.review.gwt.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.EventBus;

import edu.umd.review.gwt.rpc.dto.CommentDto;
import edu.umd.review.gwt.rpc.dto.ThreadDto;

/**
 * Event fired when a new draft is created.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
public class NewDraftEvent extends GwtEvent<NewDraftEvent.Handler> {
  public static void fire(EventBus eventBus, ThreadDto thread, CommentDto draft) {
    eventBus.fireEvent(new NewDraftEvent(draft, thread));
  }
  
  public static final Type<Handler> TYPE = new Type<NewDraftEvent.Handler>();

  /** Handler for new draft events. */
  public interface Handler extends EventHandler {
    void onNewDraft(NewDraftEvent event);
  }

  private final CommentDto draft;
  private final ThreadDto thread;

  public NewDraftEvent(CommentDto draft, ThreadDto thread) {
    this.draft = draft;
    this.thread = thread;
  }

    public ThreadDto getThread() {
        return thread;
    }

  public CommentDto getDraft() {
    return draft;
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onNewDraft(this);
  }
}

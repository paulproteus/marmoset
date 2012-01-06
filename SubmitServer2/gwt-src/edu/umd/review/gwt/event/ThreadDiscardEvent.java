package edu.umd.review.gwt.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.umd.review.gwt.rpc.dto.ThreadDto;

/**
 * Event fired when a thread is discarded.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
public class ThreadDiscardEvent extends GwtEvent<ThreadDiscardEvent.Handler> {
  public static final GwtEvent.Type<Handler> TYPE = new GwtEvent.Type<Handler>();

  /** Handler for thread discard events. */
  public interface Handler extends EventHandler {
    void onThreadDiscard(ThreadDiscardEvent event);
  }

  private final ThreadDto thread;

  public ThreadDiscardEvent(ThreadDto thread) {
    this.thread = thread;
  }

  public ThreadDto getThread() {
    return thread;
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onThreadDiscard(this);
  }
}

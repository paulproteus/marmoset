package edu.umd.review.gwt.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when a thread is to be displayed.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public class OpenThreadEvent extends GwtEvent<OpenThreadEvent.Handler> {
  public static final Type<Handler> TYPE = new Type<OpenThreadEvent.Handler>();

  private final long thread;

  public OpenThreadEvent(long thread) {
    this.thread = thread;
  }

  public long getThread() {
    return this.thread;
  }

  /** Handler for open thread events. */
  public interface Handler extends EventHandler {
    void onOpenThread(OpenThreadEvent event);
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onOpenThread(this);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return TYPE;
  }
}

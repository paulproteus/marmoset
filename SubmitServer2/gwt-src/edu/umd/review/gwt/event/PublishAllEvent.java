package edu.umd.review.gwt.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when all drafts get published.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
public class PublishAllEvent extends GwtEvent<PublishAllEvent.Handler> {
  public static final GwtEvent.Type<Handler> TYPE = new GwtEvent.Type<PublishAllEvent.Handler>();

  /** Interface for handlers that deal with {@link PublishAllEvent}s. */
  public interface Handler extends EventHandler {
    void onPublishAllEvent(PublishAllEvent event);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onPublishAllEvent(this);
  }
}

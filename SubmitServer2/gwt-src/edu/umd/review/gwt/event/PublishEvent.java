package edu.umd.review.gwt.event;

import java.util.Collection;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when drafts have been published.
 *
 * @author Ryan W Sims <rwsims@umd.edu>
 *
 */
public class PublishEvent extends GwtEvent<PublishEvent.Handler> {
  public static final Type<Handler> TYPE = new Type<PublishEvent.Handler>();
  private final Collection<Integer> draftIds;

  public PublishEvent(Collection<Integer> draftIds) {
    this.draftIds = draftIds;
  }

  public Collection<Integer> getDrafts() {
    return draftIds;
  }

  /** Handler for publish draft events. */
  public interface Handler extends EventHandler {
    void onPublish(PublishEvent event);
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onPublish(this);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return TYPE;
  }
}

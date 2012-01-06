package edu.umd.review.gwt.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class SessionExpiryEvent extends GwtEvent<SessionExpiryEvent.Handler> {
  private static final GwtEvent.Type<Handler> type = new GwtEvent.Type<Handler>();
  public static GwtEvent.Type<Handler> getType() {
    return type;
  }

  public interface Handler extends EventHandler {
    void onSessionExpiry(SessionExpiryEvent event);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return getType();
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onSessionExpiry(this);
  }
}

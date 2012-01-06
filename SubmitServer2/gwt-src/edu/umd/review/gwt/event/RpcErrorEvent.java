package edu.umd.review.gwt.event;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/** Event thrown when an RPC error occurs. */
public class RpcErrorEvent extends GwtEvent<RpcErrorEvent.Handler> {
  public static final Type<RpcErrorEvent.Handler> TYPE = new Type<RpcErrorEvent.Handler>();

  private final Throwable caught;
  private final Action<? extends Result> action;

  public RpcErrorEvent(Action<? extends Result> action, Throwable caught) {
    this.action = action;
    this.caught = caught;
  }

  public interface Handler extends EventHandler {
    void onRpcError(RpcErrorEvent event);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onRpcError(this);
  }
}

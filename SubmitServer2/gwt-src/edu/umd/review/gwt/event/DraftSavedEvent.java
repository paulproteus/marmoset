package edu.umd.review.gwt.event;

import com.google.common.base.Preconditions;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.umd.review.gwt.rpc.dto.CommentDto;

/**
 * Event fired when a draft comment is saved; widgets that display a draft should listen for this
 * event so they can update their displays.
 *
 * @author Ryan W Sims <rwsims@umd.edu>
 *
 */
public class DraftSavedEvent extends GwtEvent<DraftSavedEvent.Handler> {
  public static final Type<Handler> TYPE = new Type<DraftSavedEvent.Handler>();

  /** Handler for draft saved events. */
  public interface Handler extends EventHandler {
    void onDraftSaved(DraftSavedEvent event);
  }

  private final CommentDto draft;

  public DraftSavedEvent(CommentDto draft) {
    Preconditions.checkArgument(draft.isDraft(), "Can only fire with a draft");
    this.draft = draft;
  }

  public CommentDto getDraft() {
    return draft;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onDraftSaved(this);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return TYPE;
  }
}

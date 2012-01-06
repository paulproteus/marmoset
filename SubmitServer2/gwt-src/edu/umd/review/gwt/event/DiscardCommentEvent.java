package edu.umd.review.gwt.event;

import com.google.common.base.Preconditions;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.umd.review.gwt.rpc.dto.CommentDto;

/**
 * Event fired when a draft comment is deleted.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public class DiscardCommentEvent extends GwtEvent<DiscardCommentEvent.Handler> {
  public static final GwtEvent.Type<Handler> TYPE = new GwtEvent.Type<Handler>();

  /** Interface for objects that listen for discard comment events. */
  public interface Handler extends EventHandler {
    void onDiscardComment(DiscardCommentEvent event);
  }

  private final CommentDto draft;

  public DiscardCommentEvent(CommentDto draft) {
    Preconditions.checkArgument(draft.isDraft(), "Can't discard published comment");
    this.draft = draft;
  }

  public CommentDto getDraft() {
    return this.draft;
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onDiscardComment(this);
  }
}

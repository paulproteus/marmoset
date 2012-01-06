package edu.umd.review.gwt.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto;

/**
 * Event fired when a rubric is discarded.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
public class RubricDiscardEvent extends GwtEvent<RubricDiscardEvent.Handler> {
  public static final GwtEvent.Type<Handler> TYPE = new GwtEvent.Type<Handler>();

  /** Handler for rubric discard events. */
  public interface Handler extends EventHandler {
    void onRubricDiscard(RubricDiscardEvent event);
  }

  private final RubricEvaluationDto rubric;

  public RubricDiscardEvent(RubricEvaluationDto rubric) {
    this.rubric = rubric;
  }

  public RubricEvaluationDto getRubric() {
    return rubric;
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onRubricDiscard(this);
  }
}

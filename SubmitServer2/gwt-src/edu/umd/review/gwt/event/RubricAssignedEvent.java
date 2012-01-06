package edu.umd.review.gwt.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto;

/**
 * Event fired when a rubric is assigned to a thread.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
public class RubricAssignedEvent extends GwtEvent<RubricAssignedEvent.Handler> {
  public static final Type<Handler> TYPE = new GwtEvent.Type<RubricAssignedEvent.Handler>();

  /** Handler for rubric assigned events. */
  public interface Handler extends EventHandler {
    void onRubricAssigned(RubricAssignedEvent event);
  }

  private final RubricEvaluationDto rubric;

  public RubricAssignedEvent(RubricEvaluationDto rubric) {
    this.rubric = rubric;
  }

  public RubricEvaluationDto getEvaluation() {
    return rubric;
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onRubricAssigned(this);
  }
}

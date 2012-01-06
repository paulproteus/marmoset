package edu.umd.review.gwt.event;

import com.google.common.base.Preconditions;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto.Status;

/**
 * Event fired when a draft comment is saved; widgets that display a draft should listen for this
 * event so they can update their displays.
 *
 * @author Ryan W Sims <rwsims@umd.edu>
 *
 */
public class EvaluationSavedEvent extends GwtEvent<EvaluationSavedEvent.Handler> {
  public static final Type<Handler> TYPE = new Type<EvaluationSavedEvent.Handler>();

  /** Handler for draft saved events. */
  public interface Handler extends EventHandler {
    void onEvaluationSaved(EvaluationSavedEvent event);
  }

  private final RubricEvaluationDto evaluation;

  public EvaluationSavedEvent(RubricEvaluationDto evaluation) {
    Preconditions.checkArgument(evaluation.getStatus() == Status.NEW
        || evaluation.getStatus() == Status.DRAFT,
        "Can only fire with a draft evaluation");
    this.evaluation = evaluation;
  }

  public RubricEvaluationDto getEvaluation() {
    return evaluation;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onEvaluationSaved(this);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return TYPE;
  }
}

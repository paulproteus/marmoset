package edu.umd.review.gwt.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto;

public class NewRubricEvaluationEvent extends GwtEvent<NewRubricEvaluationEvent.Handler> {
  private static final Type<Handler> TYPE = new Type<NewRubricEvaluationEvent.Handler>();
  public static Type<Handler> getType() {
    return TYPE;
  }

  private final RubricEvaluationDto evaluation;

  public NewRubricEvaluationEvent(RubricEvaluationDto evaluation) {
    this.evaluation = evaluation;
  }

  public RubricEvaluationDto getEvaluation() {
    return evaluation;
  }


  public interface Handler extends EventHandler {
    void onNewRubricEvent(NewRubricEvaluationEvent event);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onNewRubricEvent(this);
  }
}

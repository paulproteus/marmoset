package edu.umd.review.gwt.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto;

public class PublishEvaluationEvent extends GwtEvent<PublishEvaluationEvent.Handler> {
  private static final GwtEvent.Type<Handler> type = new GwtEvent.Type<Handler>();
  public static GwtEvent.Type<Handler> getType() {
    return type;
  }

  private final RubricEvaluationDto evaluation;

  public PublishEvaluationEvent(RubricEvaluationDto evaluation) {
    this.evaluation = evaluation;
  }

  public RubricEvaluationDto getEvaluation() {
    return evaluation;
  }

  public interface Handler extends EventHandler {
    void onPublishEvaluationEvent(PublishEvaluationEvent event);
  }

  @Override
  public com.google.gwt.event.shared.GwtEvent.Type<Handler> getAssociatedType() {
    return type;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onPublishEvaluationEvent(this);
  }
}

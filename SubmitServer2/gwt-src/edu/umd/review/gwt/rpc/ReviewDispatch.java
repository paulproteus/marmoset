package edu.umd.review.gwt.rpc;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.customware.gwt.dispatch.client.AbstractDispatchAsync;
import net.customware.gwt.dispatch.client.ExceptionHandler;
import net.customware.gwt.dispatch.client.standard.StandardDispatchServiceAsync;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.umd.review.gwt.view.StatusView;

@Singleton
public class ReviewDispatch extends AbstractDispatchAsync {
  private static final Logger logger = Logger.getLogger(ReviewDispatch.class.getName());
  private final StandardDispatchServiceAsync service;
  private final StatusView statusView;

  @Inject
  ReviewDispatch(StandardDispatchServiceAsync service, ExceptionHandler handler, StatusView statusView) {
    super(handler);
    this.service = service;
    this.statusView = statusView;
  }

  @Override
  public <A extends Action<R>, R extends Result> void execute(final A action,
                                                              final AsyncCallback<R> callback) {
    service.execute(action, new AsyncCallback<Result>() {
      public void onFailure(Throwable caught) {
        logger.log(Level.SEVERE, "RPC failure", caught);
        statusView.showError("RPC call failed, please reload.");
        ReviewDispatch.this.onFailure(action, caught, callback);
      }

      public void onSuccess(Result result) {
        ReviewDispatch.this.onSuccess(action, (R) result, callback);
      }
    });
  }
}

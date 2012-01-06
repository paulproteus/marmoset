package edu.umd.review.gwt;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.dispatch.client.standard.StandardDispatchServiceAsync;

import com.google.common.base.Strings;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.logging.client.SimpleRemoteLogHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;

import edu.umd.review.common.CommonConstants;
import edu.umd.review.common.action.GetFilesAction;
import edu.umd.review.common.action.GetFilesAction.Result;
import edu.umd.review.gwt.event.PublishAllEvent;
import edu.umd.review.gwt.event.PublishEvent;
import edu.umd.review.gwt.gin.ReviewGinjector;
import edu.umd.review.gwt.view.PublishDraftsView;
import edu.umd.review.gwt.view.SnapshotView;
import edu.umd.review.gwt.view.StatusView;
import edu.umd.review.gwt.view.TrayView;
import edu.umd.review.gwt.view.impl.PublishDraftsViewImpl;
import edu.umd.review.gwt.view.impl.TrayViewImpl;

/**
 * Production entry point class.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 */
public class CodeReview extends Composite implements EntryPoint, ValueChangeHandler<String>,
  PublishEvent.Handler, PublishAllEvent.Handler {
  /** UiBinder interface. */
  interface CodeReviewBinder extends UiBinder<Widget, CodeReview> { }
  private static final CodeReviewBinder binder = GWT.create(CodeReviewBinder.class);
  private static ReviewGinjector injector = GWT.create(ReviewGinjector.class);

  @UiField SimplePanel contentPanel;
  @UiField(provided = true) TrayViewImpl trayView;
  @UiField Anchor backAnchor;
  @UiField Label titleLabel;

  private PresenterFactory presenterFactory;
  private SnapshotView snapshotView;
  private PublishDraftsView publishView;
  private TrayView.Presenter trayPresenter;
  private SnapshotView.Presenter snapshotPresenter;
  private DispatchAsync dispatch;
  private final StatusView statusView;

  CodeReview() {
    presenterFactory = injector.getPresenterFactory();
    dispatch = injector.getDispatch();
    snapshotView = injector.getSnapshotView();
    publishView = new PublishDraftsViewImpl();
    trayView = injector.getTrayView();
    statusView = injector.getStatusView();
    initWidget(binder.createAndBindUi(this));
  }

  private native CodeReviewSummary getSummary() /*-{
    return $wnd.reviewSummary;
  }-*/;

  static final Logger ROOT_LOGGER = Logger.getLogger("");
  @Override
  public void onModuleLoad() {
    // Add a remote logging handler to the root logger; all log messages with level SEVERE or higher
    // will be logged to the server. Note that this handler does no batching, retrying, or anything
    // of the sort.
    // TODO(rwsims): Implement a more sophisticated remote logging handler.
    SimpleRemoteLogHandler remoteHandler = new SimpleRemoteLogHandler();
    remoteHandler.setLevel(Level.SEVERE);
    ROOT_LOGGER.addHandler(remoteHandler);

    RootLayoutPanel.get().add(this);
    EventBus eventBus = injector.getEventBus();
    eventBus.addHandler(PublishEvent.TYPE, this);
    eventBus.addHandler(PublishAllEvent.TYPE, this);
    CodeReviewSummary summary = getSummary();

    StandardDispatchServiceAsync service = injector.getDispatchService();
    ((ServiceDefTarget) service).setRpcRequestBuilder(new DaoRequestBuilder(summary.daoKey()));

    Window.setTitle(summary.title());
    titleLabel.setText(summary.title());
    summary.setBacklink(backAnchor);
    History.addValueChangeHandler(this);
    History.fireCurrentHistoryState();
  }

  /** RequestBuilder to put the DAO key in a header in each request. */
  private static class DaoRequestBuilder extends RpcRequestBuilder {
    private final String daoKey;

    DaoRequestBuilder(String daoKey) {
      this.daoKey = daoKey;
    }

    protected RequestBuilder doCreate(String serviceEntryPoint) {
      RequestBuilder builder = super.doCreate(serviceEntryPoint);
      builder.setHeader(CommonConstants.DAO_KEY_HEADER, daoKey);
      return builder;
    };
  }

  private void loadSubmission() {
    contentPanel.setWidget(snapshotView);
    dispatch.execute(new GetFilesAction(), new AsyncCallback<GetFilesAction.Result>() {
      @Override
      public void onFailure(Throwable caught) {
        GwtUtils.wrapAndThrow(caught);
      }

      @Override
      public void onSuccess(Result result) {
         if (trayPresenter != null) {
           trayPresenter.finish();
           trayPresenter = null;
         }
         trayPresenter = presenterFactory.makeTrayPresenter(trayView, result.getFiles());
         trayPresenter.start();
         if (snapshotPresenter != null) {
           snapshotPresenter.finish();
           snapshotPresenter = null;
         }
         snapshotPresenter = presenterFactory.makeSnapshotPresenter(snapshotView, result.getFiles());
         snapshotPresenter.start();
      }
    });
  }

  private void loadPublish() {
    contentPanel.setWidget(publishView);
    PublishDraftsView.Presenter pubPresenter = presenterFactory.makePublishPresenter(publishView);
    pubPresenter.start();
  }

  @Override
  public void onValueChange(ValueChangeEvent<String> event) {
    trayView.reset();
    snapshotView.reset();
    publishView.reset();
    String token = event.getValue();
    if (Strings.isNullOrEmpty(token) || token.equals(ClientConstants.REVIEW_TOKEN)) {
      loadSubmission();
    } else if (token.equals(ClientConstants.PUBLISH_TOKEN)) {
      loadPublish();
    }
  }

  @Override
  public void onPublish(PublishEvent event) {
    loadSubmission();
  }

  @Override
  public void onPublishAllEvent(PublishAllEvent event) {
    loadSubmission();
  }
}

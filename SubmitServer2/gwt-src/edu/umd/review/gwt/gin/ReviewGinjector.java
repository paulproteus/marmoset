package edu.umd.review.gwt.gin;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.dispatch.client.standard.StandardDispatchServiceAsync;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.web.bindery.event.shared.EventBus;

import edu.umd.review.gwt.CodeReviewModule;
import edu.umd.review.gwt.PresenterFactory;
import edu.umd.review.gwt.presenter.PresenterModule;
import edu.umd.review.gwt.rpc.DispatchModule;
import edu.umd.review.gwt.view.ScrollManager;
import edu.umd.review.gwt.view.SnapshotView;
import edu.umd.review.gwt.view.StatusView;
import edu.umd.review.gwt.view.impl.TrayViewImpl;

/**
 * GIN injector interface.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
@GinModules({
  PresenterModule.class,
  CodeReviewModule.class,
  DispatchModule.class
  })
public interface ReviewGinjector extends Ginjector {
  EventBus getEventBus();
  PresenterFactory getPresenterFactory();
  SnapshotView getSnapshotView();
  ScrollManager getScrollManager();
  TrayViewImpl getTrayView();
  DispatchAsync getDispatch();
  StandardDispatchServiceAsync getDispatchService();
  StatusView getStatusView();
}

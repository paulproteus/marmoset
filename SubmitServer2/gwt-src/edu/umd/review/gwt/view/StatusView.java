package edu.umd.review.gwt.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import edu.umd.review.gwt.event.RpcErrorEvent;
import edu.umd.review.gwt.view.impl.StatusViewImpl;

/** View for displaying status messages. */
@ImplementedBy(StatusViewImpl.class)
public interface StatusView extends IsWidget {
  public interface Presenter extends RpcErrorEvent.Handler {}

  void showError(String message);

  void showStatus(String message);
}

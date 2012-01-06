package edu.umd.review.gwt.view.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import edu.umd.review.gwt.view.FileView;
import edu.umd.review.gwt.view.ScrollManager;
import edu.umd.review.gwt.view.SnapshotView;

/**
 * UiBinder Implementation of {@link SnapshotView}.
 *
 * @author rwsims@gmail.com (Ryan W Sims)
 */
@Singleton
public class SnapshotViewImpl extends Composite implements SnapshotView {
  /** UiBinder interface for SnapshotViewImpl. */
  interface SnapshotViewImplUiBinder extends UiBinder<Widget, SnapshotViewImpl> { }
  private static SnapshotViewImplUiBinder uiBinder = GWT.create(SnapshotViewImplUiBinder.class);

  @UiField(provided = true) ScrollPanel scrollPanel;
  @UiField FlowPanel mainPanel;
  @UiField FocusPanel outer;

  private Presenter presenter;
  private final Provider<FileView> fileViewProvider;
  private final ScrollManager scrollManager;

  @Inject
  public SnapshotViewImpl(ScrollManager scrollManager, Provider<FileView> fileViewProvider) {
    this.fileViewProvider = fileViewProvider;
    this.scrollManager = scrollManager;
    this.scrollPanel = scrollManager.getScrollPanel();
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public FileView addFileView(FileView before) {
    FileView newView = fileViewProvider.get();
    if (before != null) {
      int idx = mainPanel.getWidgetIndex(before);
      mainPanel.insert(newView, idx);
    } else {
      mainPanel.add(newView);
    }
    return newView;
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void reset() {
    mainPanel.clear();
  }

  @Override
  public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
    return outer.addKeyPressHandler(handler);
  }

  @Override
  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return outer.addKeyDownHandler(handler);
  }

  @Override
  public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
    return outer.addMouseMoveHandler(handler);
  }

  @Override
  public void expireSession() {
    // defer the reload so all event handlers complete before trying to reload.
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        Window.Location.reload();
      }
    });
  }
}

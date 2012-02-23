package edu.umd.review.gwt.view.impl;

import java.util.Collection;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.inject.Singleton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.umd.review.gwt.rpc.dto.ThreadDto;
import edu.umd.review.gwt.view.GeneralCommentsView;
import edu.umd.review.gwt.view.ThreadView;

@Singleton
public class GeneralCommentsViewImpl extends Composite implements GeneralCommentsView {
  interface ViewBinder extends UiBinder<Widget, GeneralCommentsViewImpl> {}
  private static ViewBinder uiBinder = GWT.create(ViewBinder.class);

  @UiField FlowPanel mainPanel;
  @UiField FlowPanel threadPanel;
  
  @CheckForNull
  private Presenter presenter;
  
  public GeneralCommentsViewImpl() {
    initWidget(uiBinder.createAndBindUi(this));
  }
  
  @Override
  public void setPresenter(@Nullable Presenter presenter) {
    this.presenter = presenter;
  }
  
  @Override
  public void clear() {
    threadPanel.clear();
  }

  @Override
  public ThreadView newThreadView() {
    ThreadViewImpl threadView = new ThreadViewImpl();
    threadPanel.add(threadView);
    return threadView;
  }
  
  @Override
  public void setVisible(boolean visible) {
    mainPanel.setVisible(visible);
  }
  
  @UiHandler("newThread")
  void onNewThreadClicked(ClickEvent event) {
    if (presenter != null) {
      presenter.createNewThread();
    }
  }
}

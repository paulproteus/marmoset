package edu.umd.review.gwt.view.impl;

import java.util.Collection;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.inject.Singleton;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.AbstractDropController;
import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.umd.review.gwt.rpc.dto.ThreadDto;
import edu.umd.review.gwt.view.GeneralCommentsView;
import edu.umd.review.gwt.view.ThreadView;
import edu.umd.review.gwt.widget.RubricDragger;

@Singleton
public class GeneralCommentsViewImpl extends Composite implements GeneralCommentsView {
  interface ViewBinder extends UiBinder<Widget, GeneralCommentsViewImpl> {}
  private static ViewBinder uiBinder = GWT.create(ViewBinder.class);
  
  interface Style extends CssResource {
    String hovering();
  }
  
  @UiField Style style;
  @UiField FlowPanel mainPanel;
  @UiField FlowPanel threadPanel;
  
  @CheckForNull
  private Presenter presenter;
  private ViewDropController controller;
  
  public GeneralCommentsViewImpl() {
    initWidget(uiBinder.createAndBindUi(this));
    controller = new ViewDropController(mainPanel);
  }
  
  @Override
  public void setPresenter(@Nullable Presenter presenter) {
    if (this.presenter != null && presenter == null) {
      this.presenter.unregisterDropController(controller);
    }
    this.presenter = presenter;
    if (this.presenter != null) {
      this.presenter.registerDropController(controller);
    }
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
  
  private class ViewDropController extends AbstractDropController {
    private final FlowPanel panel;

    public ViewDropController(FlowPanel panel) {
      super(panel);
      this.panel = Preconditions.checkNotNull(panel);
    }
    
    @Override
    public void onEnter(DragContext context) {
      super.onEnter(context);
      panel.addStyleName(style.hovering());
    }
    
    @Override
    public void onLeave(DragContext context) {
      super.onLeave(context);
      panel.removeStyleName(style.hovering());
    }
    
    @Override
    public void onDrop(DragContext context) {
      if (presenter == null) {
        return;
      }
      super.onDrop(context);
      presenter.newThreadWithRubric(((RubricDragger) context.draggable).getRubric());
    }
  }
}

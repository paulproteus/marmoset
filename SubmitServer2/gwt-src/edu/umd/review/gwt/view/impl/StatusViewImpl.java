package edu.umd.review.gwt.view.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Singleton;

import edu.umd.review.gwt.view.StatusView;

@Singleton
public class StatusViewImpl extends Composite implements StatusView {
  interface ViewBinder extends UiBinder<Widget, StatusViewImpl> {}
  private static ViewBinder uiBinder = GWT.create(ViewBinder.class);

  interface Style extends CssResource{
    String popupError();
    String popupMessage();
  }

  private static final int popupTop = 20;
  private Timer hideTimer;

  @UiField Style style;
  @UiField PopupPanel popup;
  @UiField Label messageLabel;

  public StatusViewImpl() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void showError(String message) {
    popup.setStyleName(style.popupError());
    setAndShow(message);
  }

  @Override
  public void showStatus(String message) {
    popup.setStyleName(style.popupMessage());
    setAndShow(message);
  }

  private void setAndShow(String message) {
    if (hideTimer != null) {
      hideTimer.cancel();
    }
    messageLabel.setText(message);
    popup.setPopupPositionAndShow(new PositionCallback() {
      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
        popup.setPopupPosition((Window.getClientWidth() - offsetWidth) / 2, popupTop);
      }
    });
    hideTimer = new Timer() {
      @Override
      public void run() {
        popup.hide();
        hideTimer = null;
      }
    };
    hideTimer.schedule(5000);
  }
}

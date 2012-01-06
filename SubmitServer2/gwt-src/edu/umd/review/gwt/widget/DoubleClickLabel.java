package edu.umd.review.gwt.widget;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

/**
 * Composite to wrap a Label in a double click handler.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 */
public class DoubleClickLabel extends Composite implements HasDoubleClickHandlers {
  private Label innerLabel;

  public DoubleClickLabel(String text) {
    if (Strings.isNullOrEmpty(text)) {
      // The label div collapses if we don't put anything in it, which screws with styling and
      // event handling
      this.innerLabel = new Label(" ");
    } else {
      this.innerLabel = new Label(text);
    }
    initWidget(innerLabel);
  }

  public String getText() {
    return this.innerLabel.getText();
  }

  @Override
  public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {
    return addDomHandler(handler, DoubleClickEvent.getType());
  }
}

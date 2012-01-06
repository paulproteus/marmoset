package edu.umd.review.gwt.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

@Deprecated
public class SwitchingTextBox extends Composite implements HasValue<String>{
  interface WidgetBinder extends UiBinder<Widget, SwitchingTextBox> {}
  private static WidgetBinder uiBinder = GWT.create(WidgetBinder.class);

  @UiField TextBox box;
  @UiField Label label;

  public SwitchingTextBox() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public void setEditing(boolean isEditing) {
    label.setVisible(!isEditing);
    box.setVisible(isEditing);
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  @Override
  public String getValue() {
    return box.getValue();
  }

  @Override
  public void setValue(String value) {
    setValue(value, false);
  }

  public void setPlaceholder(String placeholder) {
    DOM.setElementProperty(box.getElement(), "placeholder", placeholder);
  }

  @Override
  public void setValue(String value, boolean fireEvents) {
    box.setValue(value, false);
    label.setText(value);
    if (fireEvents) {
      ValueChangeEvent.fire(this, value);
    }
  }
}

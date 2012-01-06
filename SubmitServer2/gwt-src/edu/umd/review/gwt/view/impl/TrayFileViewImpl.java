package edu.umd.review.gwt.view.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.umd.review.gwt.rpc.dto.FileDto;
import edu.umd.review.gwt.view.TrayFileView;
import edu.umd.review.gwt.view.TrayThreadView;

/**
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public class TrayFileViewImpl extends Composite implements TrayFileView {
  /** Gwt UiBinder interface for {@link TrayFileViewImpl}. */
  interface TrayFileViewImplUiBinder extends UiBinder<Widget, TrayFileViewImpl> { };
  private static TrayFileViewImplUiBinder uiBinder = GWT.create(TrayFileViewImplUiBinder.class);

  @UiField Anchor link;
  @UiField FlowPanel panel;
  @UiField HTMLPanel outer;

  private Presenter presenter;

  TrayFileViewImpl() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public TrayThreadView insertThread(TrayThreadView before) {
    TrayThreadView newView = new TrayThreadViewImpl();
    if (before != null) {
      int idx = panel.getWidgetIndex(before);
      panel.insert(newView, idx);
    } else {
      panel.add(newView);
    }
    return newView;
  }

  @Override
  public void setFile(FileDto file) {
    link.setText(file.getPath());
  }

  @Override
  public void setSelected(boolean selected) {
    outer.setStyleDependentName("active", selected);
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @UiHandler("link")
  void onLinkClicked(ClickEvent event) {
    presenter.openThisFile();
  }
}

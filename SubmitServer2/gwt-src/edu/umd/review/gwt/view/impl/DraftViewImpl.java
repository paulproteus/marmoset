package edu.umd.review.gwt.view.impl;

import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import edu.umd.review.gwt.view.DraftView;

/**
 * Implementation of {@link DraftView}.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public class DraftViewImpl extends Composite implements DraftView {
  /** UiBinder interface for {@link DraftViewImpl}. */
  interface ViewBinder extends UiBinder<Widget, DraftViewImpl> { }
  private static final ViewBinder binder = GWT.create(ViewBinder.class);

  @UiField HTML preview;
  @UiField TextArea edit;
  @UiField FlowPanel editPanel;
  @UiField FlowPanel previewPanel;
  @UiField CheckBox replyRequested;
  @UiField Anchor editLink;
  @UiField InlineLabel savingLabel;

  private DraftView.Presenter presenter;

  public DraftViewImpl() {
    initWidget(binder.createAndBindUi(this));
    DOM.setElementProperty(replyRequested.getElement(), "title", "Check this box to request a reply");
  }

  @Override
  public void showReplyRequested(boolean show, boolean isAuthor) {
    replyRequested.setVisible(show);
  }


  @Override
  public void setAck(boolean ack) {
    replyRequested.setValue(!ack);
  }

  @Override
  public void previewDraft(String text) {
    SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
    htmlBuilder.appendEscaped(text);
    preview.setHTML(htmlBuilder.toSafeHtml());
    preview.setVisible(true);
    edit.setVisible(false);
    editPanel.setVisible(false);
    previewPanel.setVisible(true);
    replyRequested.setEnabled(false);
    if (!replyRequested.getValue()) {
      replyRequested.setVisible(false);
    }
    editLink.setVisible(true);
    savingLabel.setVisible(false);
  }

  @Override
  public void startSave(String text) {
    SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
    htmlBuilder.appendEscaped(text);
    preview.setHTML(htmlBuilder.toSafeHtml());
    preview.setVisible(true);
    edit.setVisible(false);
    editPanel.setVisible(false);
    previewPanel.setVisible(true);
    replyRequested.setEnabled(false);
    if (!replyRequested.getValue()) {
      replyRequested.setVisible(false);
    }
    editLink.setVisible(false);
    savingLabel.setVisible(true);
  }

  @Override
  public void finishSave(String text) {
    previewDraft(text);
  }

  @Override
  public void editDraft(String text) {
    edit.setValue(text);
    edit.setVisible(true);
    preview.setVisible(false);
    editPanel.setVisible(true);
    previewPanel.setVisible(false);
    replyRequested.setEnabled(true);
    replyRequested.setVisible(true);
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        // we defer setting the focus until after the event loop returns, otherwise it gets lost
        edit.setFocus(true);
      }
    });
  }

  @UiHandler("saveLink")
  void onSave(ClickEvent event) {
    presenter.onSave(edit.getValue(), !replyRequested.getValue());
  }

  @UiHandler("editLink")
  void onEdit(ClickEvent event) {
    presenter.onEdit();
  }

  @UiHandler("cancelLink")
  void onCancel(ClickEvent event) {
    presenter.onCancel();
  }

  @UiHandler("discardLink")
  void onDiscard(ClickEvent event) {
    presenter.onDiscard();
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = Preconditions.checkNotNull(presenter);
  }
}

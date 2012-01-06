package edu.umd.review.gwt.view.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import edu.umd.review.gwt.rpc.dto.CommentDto;
import edu.umd.review.gwt.view.CheckboxRubricEvaluationView;
import edu.umd.review.gwt.view.DraftView;
import edu.umd.review.gwt.view.DropdownRubricEvaluationView;
import edu.umd.review.gwt.view.NumericRubricEvaluationView;
import edu.umd.review.gwt.view.ThreadView;
import edu.umd.review.gwt.widget.CommentWidget;

/**
 * Implementation of {@link ThreadView}.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public class ThreadViewImpl extends Composite implements ThreadView, IsWidget {
  /** UiBinder interface. */
  interface ViewBinder extends UiBinder<Widget, ThreadViewImpl> {}

  private static final ViewBinder binder = GWT.create(ViewBinder.class);

  @UiField FlowPanel commentPanel;
  @UiField FlowPanel buttonPanel;
  @UiField DraftViewImpl draftView;
  @UiField Anchor replyLink;
  @UiField Anchor ackLink;
  @UiField Label needAckLabel;
  @UiField Label waitingLabel;
  @UiField SimplePanel rubricPanel;

  private Presenter presenter;

  @Inject
  public ThreadViewImpl() {
    initWidget(binder.createAndBindUi(this));
    draftView.asWidget().setVisible(false);
    waitingLabel.setVisible(false);
  }

  @UiHandler("replyLink")
  void onReply(ClickEvent event) {
    presenter.onReply();
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }


  @Override
  public void setComments(Iterable<CommentDto> comments) {
    CommentWidget openMe = null;
    for (CommentDto comment : comments) {
      CommentWidget widget = new CommentWidget(comment);
      if (!comment.isJustAcknowledgement())
        openMe = widget;
      commentPanel.add(widget);
    }
    if (openMe == null) {
      return;
    }
    // opens last published non-justAck comment in thread
    openMe.setOpen(true);
  }

  @Override
  public void showAcknowledge(boolean show) {
    needAckLabel.setVisible(show);
    ackLink.setVisible(show);
    if (show) {
      buttonPanel.setTitle("Thread needs a response");
    }
  }

  @Override
  public void setThreadOpen(boolean open) {
    setStyleDependentName("open", open);
  }

  @Override
  public void waitForDraft() {
    waitingLabel.setVisible(true);
    buttonPanel.setVisible(false);
    draftView.setVisible(false);
  }

  @Override
  public DraftView getDraftView() {
    return draftView;
  }

  @Override
  public void showReplyLink(boolean b) {
      replyLink.setVisible(b);
  }

  @Override
  public void setDraftEditorVisible(boolean visible) {
    draftView.setVisible(visible);
    buttonPanel.setVisible(!visible);
    if (visible) {
      waitingLabel.setVisible(false);
    }
  }

  @UiHandler("ackLink")
  void onAckClicked(ClickEvent event) {
    presenter.acknowledge();
  }

  @Override
  public NumericRubricEvaluationView getNumericEvaluationView() {
    NumericRubricEvaluationView evalView = new NumericRubricEvaluationViewImpl();
    rubricPanel.setWidget(evalView);
    rubricPanel.setVisible(true);
    return evalView;
  }

  @Override
  public DropdownRubricEvaluationView getDropdownEvaluationView() {
    DropdownRubricEvaluationView evalView = new DropdownRubricEvaluationViewImpl();
    rubricPanel.setWidget(evalView);
    rubricPanel.setVisible(true);
    return evalView;
  }

  @Override
  public CheckboxRubricEvaluationView getCheckboxEvaluationView() {
    CheckboxRubricEvaluationView evalView = new CheckboxRubricEvaluationViewImpl();
    rubricPanel.setWidget(evalView);
    rubricPanel.setVisible(true);
    return evalView;
  }

  @Override
  public void clearRubricEvaluationView() {
    rubricPanel.clear();
  }
}

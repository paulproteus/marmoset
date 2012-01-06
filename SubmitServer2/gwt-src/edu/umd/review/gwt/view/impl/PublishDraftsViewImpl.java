package edu.umd.review.gwt.view.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Singleton;

import edu.umd.review.gwt.ClientConstants;
import edu.umd.review.gwt.rpc.dto.CommentDto;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto.Status;
import edu.umd.review.gwt.rpc.dto.ThreadDto;
import edu.umd.review.gwt.view.PublishDraftsView;

/**
 * Implementation of {@link PublishDraftsView}.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
@Singleton
public class PublishDraftsViewImpl extends Composite implements PublishDraftsView {
  /** UiBinder interface. */
  interface ViewBinder extends UiBinder<Widget, PublishDraftsViewImpl> {}

  private static final ViewBinder binder = GWT.create(ViewBinder.class);

  @UiField FlowPanel mainPanel;

  private Presenter presenter;

  public PublishDraftsViewImpl() {
    initWidget(binder.createAndBindUi(this));
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void setDrafts(Map<String, TreeSet<ThreadDto>> comments) {
    for (Entry<String, TreeSet<ThreadDto>> submission : comments.entrySet()) {
      FlowPanel filePanel = new FlowPanel();
      filePanel.setStyleName("publish-snapshot");
      Label fileLabel = new Label(submission.getKey());
      fileLabel.setStyleName("shot-label");
      filePanel.add(fileLabel);
      FlexTable threadTable = new FlexTable();
      threadTable.setText(0, 0, "Publish?");
      threadTable.setText(0, 1, "Info");
      DOM.setElementProperty(threadTable.getCellFormatter().getElement(0, 1),
                             "title",
                              "For rubric evaluations, show the score; " +
                              "for comments show whether a reply is requested.");
      threadTable.setText(0, 2, "Comment");
      threadTable.getRowFormatter().addStyleName(0, "publish-table-header");
      filePanel.add(threadTable);
      int row = 1;
      for (final ThreadDto thread : submission.getValue()) {
        threadTable.setCellSpacing(0);
        threadTable.setStyleName("publish-thread-table");
        for (CommentDto comment : thread.getPublishedComments()) {
          threadTable.setText(row, 2, comment.getSnippet(100));
          threadTable.getCellFormatter().addStyleName(row, 2, "comment-text");
          row++;
        }

        CheckBox pubBox = new CheckBox();
        threadTable.setWidget(row, 0, pubBox);
        threadTable.getCellFormatter().addStyleName(row, 0, "publish-thread-checkbox-cell");
        pubBox.setValue(true);
        pubBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
          @Override
          public void onValueChange(ValueChangeEvent<Boolean> event) {
            presenter.setPublishStatus(thread, event.getValue());
          }
        });

        RubricEvaluationDto rubricEvaluation = thread.getRubricEvaluation();
        if (rubricEvaluation != null
            && (rubricEvaluation.getStatus() == Status.DRAFT || rubricEvaluation.getStatus() == Status.NEW)
            && rubricEvaluation.isEditable()) {
          String explanation = rubricEvaluation.getExplanation();
          if (explanation == null || explanation.isEmpty()) {
            explanation = rubricEvaluation.getRubric().getDescription();
          }
          threadTable.setText(row, 2, explanation);
          threadTable.setText(row,
                              1,
                              rubricEvaluation.getPoints() + ": " + rubricEvaluation.getName());
        } else if (thread.getDraft() != null) {
          if (!thread.getDraft().isAcknowledgement()) {
            threadTable.setText(row, 1, "Reply requested");
          }
          threadTable.setText(row, 2, thread.getDraft().getSnippet(100));
        }
        threadTable.getCellFormatter().addStyleName(row, 1, "comment-text");
        threadTable.getCellFormatter().addStyleName(row, 2, "comment-text");
        threadTable.getRowFormatter().addStyleName(row, "publish-draft-row");
        row++;
      }

      mainPanel.add(filePanel);
    }
  }

  @UiHandler("publishButton")
  void onPublishClick(ClickEvent event) {
    Preconditions.checkNotNull(presenter, "Presenter must be set before publishing");
    presenter.doPublish();
  }

  @UiHandler("cancelButton")
  void onCancelClick(ClickEvent event) {
    History.newItem(ClientConstants.REVIEW_TOKEN);
  }

  @Override
  public void reset() {
    mainPanel.clear();
  }
}

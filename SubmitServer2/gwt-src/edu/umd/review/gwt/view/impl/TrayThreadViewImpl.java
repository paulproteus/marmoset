package edu.umd.review.gwt.view.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import edu.umd.review.gwt.rpc.dto.CommentDto;
import edu.umd.review.gwt.rpc.dto.ThreadDto;
import edu.umd.review.gwt.view.TrayThreadView;

/**
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public class TrayThreadViewImpl extends Composite implements TrayThreadView {
  /** UiBinder interface for {@link TrayThreadViewImpl}. */
  interface TrayThreadViewImplUiBinder extends UiBinder<Widget, TrayThreadViewImpl> { }
  private static final TrayThreadViewImplUiBinder uiBinder = GWT
      .create(TrayThreadViewImplUiBinder.class);

  @UiField HTML description;
  @UiField FlowPanel rubricPanel;
  @UiField FocusPanel outer;

  private final AuthorColorFactory colorFactory = new AuthorColorFactory();
  private Presenter presenter;

  public TrayThreadViewImpl() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void setThread(ThreadDto thread) {
    // ThreadDto's line property is zero-indexed
    SafeHtmlBuilder snipBuilder = new SafeHtmlBuilder();
    snipBuilder.appendEscaped((thread.getLine() + 1) + ": ");
    CommentDto draft = thread.getDraft();
    if (draft != null) {
      snipBuilder.appendHtmlConstant("* ");
      String snippet = draft.getSnippet();
      if (!Strings.isNullOrEmpty(snippet)) {
            snipBuilder.appendEscaped(snippet);
      } else {
          snipBuilder.appendHtmlConstant("<i>empty draft</i>");
      }
      description.setStyleName(colorFactory.getColor(draft.getAuthor()));
    } else if (thread.getLastComment() != null) {
      CommentDto lastComment = thread.getLastComment();
      snipBuilder.appendEscaped(lastComment.getSnippet());
      description.setStyleName(colorFactory.getColor(lastComment.getAuthor()));
    } else if (thread.getRubricEvaluation() != null) {
      snipBuilder.appendEscaped(thread.getRubricEvaluation().getName());
      description.setStyleName(colorFactory.getColor(thread.getRubricEvaluation().getAuthorName()));
    } else {
      throw new IllegalStateException("Can't set snippet for empty thread " + thread.getId());
    }
    description.setHTML(snipBuilder.toSafeHtml());
  }

  @VisibleForTesting
  String getDescription() {
    return description.getText();
  }

  @Override
  public void displayRubricItem(boolean display) {
  }

  @UiHandler("outer")
  void onMouseOver(MouseOverEvent event) {
    outer.addStyleDependentName("active");
  }

  @UiHandler("outer")
  void onMouseOut(MouseOutEvent event) {
    outer.removeStyleDependentName("active");
  }

  @UiHandler("outer")
  void clicked(ClickEvent event) {
    presenter.onThreadClicked();
  }
}

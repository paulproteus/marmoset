package edu.umd.review.gwt.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import edu.umd.review.gwt.rpc.dto.CommentDto;
import edu.umd.review.gwt.view.impl.AuthorColorFactory;

/**
 * Widget to display a published comment.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
public class CommentWidget extends Composite implements IsWidget {
  /** UiBinder interface. */
  interface WidgetBinder extends UiBinder<Widget, CommentWidget> { }
  private static final WidgetBinder binder = GWT.create(WidgetBinder.class);

  @UiField Label header;
  @UiField HTML commentHtml;

  private final CommentDto comment;
  private final AuthorColorFactory colorFactory = new AuthorColorFactory();
  private boolean open = false;

  public CommentWidget(CommentDto comment) {
    initWidget(binder.createAndBindUi(this));
    this.comment = comment;
    SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
    htmlBuilder.appendEscaped(comment.getContents());
    commentHtml.setHTML(htmlBuilder.toSafeHtml());
    setOpen(false);
    header.addStyleName(colorFactory.getColor(comment.getAuthor()));
  }

  public void setOpen(boolean open) {
    this.open = open;
    commentHtml.setVisible(open);
    if (open) {
      header.setText(comment.getAuthor());
      header.addStyleDependentName("open");
    } else {
      header.setText(comment.getSnippet(40));
      header.removeStyleDependentName("open");
    }
  }

  @UiHandler("header")
  void onHeaderClick(ClickEvent event) {
    setOpen(!open);
  }
}

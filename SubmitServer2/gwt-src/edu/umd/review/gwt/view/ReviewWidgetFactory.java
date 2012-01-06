package edu.umd.review.gwt.view;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import edu.umd.review.gwt.widget.DoubleClickLabel;

/**
 * Factory for creating various reusable widgets with custom properties.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public final class ReviewWidgetFactory {
  private static final NumberFormat lineNumberFormat = NumberFormat.getFormat("#:");

  private ReviewWidgetFactory() { }

  /** Create a {@link Anchor} widget with given text and styled for hover-underlining. */
  public static Anchor createClickableAnchor(String text) {
    Anchor anchor = new Anchor(text);
    anchor.setStylePrimaryName("hover-link");
    return anchor;
  }

  /** Create a label styled for display of a line number. */
  public static Widget createLineNumberWidget(int line) {
    Label label = new Label(lineNumberFormat.format(line));
    label.setStylePrimaryName("line-number");
    return label;
  }

  /** Create a label for displaying code. The label will listen for double click events. */
  public static DoubleClickLabel createLineLabel(String line) {
    DoubleClickLabel label = new DoubleClickLabel(line);
    label.setStylePrimaryName("code-text");
    return label;
  }
}

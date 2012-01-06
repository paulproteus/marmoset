package edu.umd.review.gwt;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.Anchor;

public final class CodeReviewSummary extends JavaScriptObject {
  protected CodeReviewSummary() {}

  public final native String daoKey() /*-{
    return this.daoKey;
  }-*/;

  public final native String title() /*-{
    return this.title;
  }-*/;

  private final native String backlinkText() /*-{
    return this.backlinkText;
  }-*/;

  private final native String backlinkUrl() /*-{
    return this.backlinkUrl;
  }-*/;

  public void setBacklink(Anchor backlink) {
    String text = backlinkText();
    String url = backlinkUrl();
    if (!Strings.isNullOrEmpty(text) && !Strings.isNullOrEmpty(url)) {
        backlink.setText(text);
        backlink.setHref(url);
    }
  }
}

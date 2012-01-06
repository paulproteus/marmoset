package edu.umd.review.gwt.event;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.inject.Inject;

import edu.umd.review.gwt.view.ScrollManager;

/**
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
public class HotkeyHandler implements KeyPressHandler {
  private final ScrollManager scrollManager;
  private int disabled;

  @Inject
  public HotkeyHandler(ScrollManager scrollManager) {
    this.scrollManager = scrollManager;
  }

  @Override
  public void onKeyPress(KeyPressEvent event) {
    // if event comes from a text input element, skip it
    Element e = Element.as(event.getNativeEvent().getEventTarget());
    if (e.getTagName().equals("TEXTAREA") || e.getTagName().equals("INPUT")) {
      return;
    }
    switch (event.getCharCode()) {
      case 'n':
        // fallthrough
      case 'j':
        scrollManager.scrollNextThread();
        break;
      case 'p':
        // fallthrough
      case 'k':
        scrollManager.scrollPrevThread();
        break;
      default:
        // do nothing
        return;
    }
  }
}

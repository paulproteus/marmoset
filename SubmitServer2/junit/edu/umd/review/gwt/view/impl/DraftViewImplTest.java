package edu.umd.review.gwt.view.impl;

import com.google.gwt.junit.client.GWTTestCase;

public class DraftViewImplTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "edu.umd.review.CodeReview";
  }

  public void testStartSaveDisablesEditing() {
    DraftViewImpl view = new DraftViewImpl();
    view.startSave("foo");
    assertTrue(view.previewPanel.isVisible());
    assertFalse(view.editPanel.isVisible());
    assertFalse(view.editLink.isVisible());
    assertTrue(view.savingLabel.isVisible());
    assertEquals("foo", view.preview.getText());
  }

  public void testFinishSaveEnablesEditing() {
    DraftViewImpl view = new DraftViewImpl();
    view.startSave("foo");
    view.finishSave("foo");
    assertTrue(view.previewPanel.isVisible());
    assertFalse(view.editPanel.isVisible());
    assertTrue(view.editLink.isVisible());
    assertFalse(view.savingLabel.isVisible());
    assertEquals("foo", view.preview.getText());
  }

  public void testReplyRequestedVisibility() {
    DraftViewImpl view = new DraftViewImpl();
    assertFalse(view.replyRequested.isVisible());
    assertFalse(view.replyRequested.getValue());

    view.editDraft("foo");
    assertTrue(view.replyRequested.isVisible());

    view.replyRequested.setValue(false);
    view.startSave("foo");
    assertFalse(view.replyRequested.isVisible());
    view.finishSave("foo");
    assertFalse(view.replyRequested.isVisible());

    view.editDraft("foo");
    assertTrue(view.replyRequested.isVisible());

    view.replyRequested.setValue(true);
    view.startSave("foo");
    assertTrue(view.replyRequested.isVisible());
    view.finishSave("foo");
    assertTrue(view.replyRequested.isVisible());
  }
}

package edu.umd.review.gwt.view.impl;

import com.google.gwt.junit.client.GWTTestCase;

public class ThreadViewImplTest extends GWTTestCase {
	@Override
  public String getModuleName() {
	  return "edu.umd.review.CodeReview";
  }

	public void testWaitForDraft() {
		ThreadViewImpl view = new ThreadViewImpl();
		assertFalse(view.draftView.isVisible());
		assertFalse(view.waitingLabel.isVisible());
		assertTrue(view.buttonPanel.isVisible());

		view.waitForDraft();
		assertFalse(view.draftView.isVisible());
		assertTrue(view.waitingLabel.isVisible());
		assertFalse(view.buttonPanel.isVisible());

		view.setDraftEditorVisible(true);
		assertTrue(view.draftView.isVisible());
		assertFalse(view.waitingLabel.isVisible());
		assertFalse(view.buttonPanel.isVisible());
	}
}

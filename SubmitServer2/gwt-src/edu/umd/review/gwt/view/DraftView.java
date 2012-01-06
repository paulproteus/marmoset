package edu.umd.review.gwt.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import edu.umd.review.gwt.presenter.DraftPresenter;
import edu.umd.review.gwt.presenter.IsPresenter;

/**
 * View for editing & saving draft comments.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public interface DraftView extends IsWidget {
	/**
	 * Display {@code text} rendered as a a comment. Do not call this method after saving a draft; use
	 * {@link #startSave(String)} and {@link #finishSave(String)} instead.
	 */
  void previewDraft(String text);

	/**
	 * Set view into "waiting for save to complete" state. Editing is disabled in this state, and a
	 * waiting message is shown to the user.
	 */
	void startSave(String text);

	/** Set view into "save completed" state; enabling editing. */
	void finishSave(String text);

  /** Edit the draft with current value {@code text}. */
  void editDraft(String text);

  void setPresenter(Presenter presenter);

  /** Set whether to show the acknowledge link, i.e. whether the reviewer is an author.
   * @param isAuthor TODO*/
  void showReplyRequested(boolean show, boolean isAuthor);

  void setAck(boolean ack);

  /** Interface for presenters that drive a draft view. */
  @ImplementedBy(DraftPresenter.class)
  public interface Presenter extends IsPresenter {
    void onSave(String text, boolean isAck);

    void onEdit();

    void onCancel();

    void onDiscard();
  }
}

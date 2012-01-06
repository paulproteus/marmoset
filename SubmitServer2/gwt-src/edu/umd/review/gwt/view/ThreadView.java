package edu.umd.review.gwt.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import edu.umd.review.gwt.presenter.IsPresenter;
import edu.umd.review.gwt.presenter.ThreadPresenter;
import edu.umd.review.gwt.rpc.dto.CommentDto;
import edu.umd.review.gwt.view.impl.ThreadViewImpl;

/**
 * Interface for widget that displays thread.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
@ImplementedBy(ThreadViewImpl.class)
public interface ThreadView extends IsWidget {

  /** Set presenter to drive the view. */
  void setPresenter(Presenter presenter);

  /**
   * Set the published comments to be displayed for this thread, overwriting any already-displayed
   * comments.
   */
  void setComments(Iterable<CommentDto> comments);

  void setThreadOpen(boolean closed);

  public void showReplyLink(boolean b);


  /** Set whether to display an "Acknowledge" link. */
  void showAcknowledge(boolean show);

  /** Puts the view in a "waiting" state while the new draft RPC round-trips to the server. */
  // TODO(rwsims): It would be nice if the user could start editing right away.
  void waitForDraft();

  /**
   * Return this threads draft view. Can reuse a single view across multiple drafts by passing it to
   * new draft presenters.
   */
  DraftView getDraftView();

  /** Set whether the draft editor is currently visible. */
  void setDraftEditorVisible(boolean visible);

  NumericRubricEvaluationView getNumericEvaluationView();
  DropdownRubricEvaluationView getDropdownEvaluationView();
  CheckboxRubricEvaluationView getCheckboxEvaluationView();

  void clearRubricEvaluationView();

  /** Interface for presenters that can drive thread views. */
  @ImplementedBy(ThreadPresenter.class)
  public interface Presenter extends IsPresenter {

    /** Reply to the thread. This will open the thread. */
    void onReply();

    void acknowledge();
  }
}

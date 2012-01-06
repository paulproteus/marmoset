package edu.umd.review.gwt.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import edu.umd.review.gwt.presenter.IsPresenter;
import edu.umd.review.gwt.presenter.TrayThreadPresenter;
import edu.umd.review.gwt.rpc.dto.ThreadDto;

/**
 * View for a thread displayed inside a {@link TrayFileView}. Optionally displays the thread's
 * associated rubric item.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public interface TrayThreadView extends IsWidget {
  void setPresenter(Presenter presenter);

  void setThread(ThreadDto thread);

  void displayRubricItem(boolean display);

  /** Presenter to drive a {@link TrayThreadView}. */
  @ImplementedBy(TrayThreadPresenter.class)
  public interface Presenter extends IsPresenter {
    void onThreadClicked();
  }
}

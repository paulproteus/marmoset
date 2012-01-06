package edu.umd.review.gwt.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import edu.umd.review.gwt.presenter.IsPresenter;
import edu.umd.review.gwt.presenter.TrayFilePresenter;
import edu.umd.review.gwt.rpc.dto.FileDto;
import edu.umd.review.gwt.view.impl.TrayFileViewImpl;

/**
 * View for a single file in the comment tray.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
@ImplementedBy(TrayFileViewImpl.class)
public interface TrayFileView extends IsWidget {
  void setPresenter(Presenter presenter);

	/**
	 * Insert a thread view before the given view. If {@code before} is null, insert after all other
	 * views.
	 */
  TrayThreadView insertThread(TrayThreadView before);

  void setFile(FileDto file);

  void setSelected(boolean selected);

  /** Presenter to drive a {@link TrayFileView}. */
  @ImplementedBy(TrayFilePresenter.class)
  public interface Presenter extends IsPresenter {
    /** Fire an event to request opening the file owned by this presenter. */
    void openThisFile();
  }
}

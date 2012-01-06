package edu.umd.review.gwt.view;

import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.dom.client.HasMouseMoveHandlers;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import edu.umd.review.gwt.presenter.IsPresenter;
import edu.umd.review.gwt.presenter.SnapshotPresenter;
import edu.umd.review.gwt.view.impl.SnapshotViewImpl;

/**
 * View that can display all the files in a single snapshot.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
@ImplementedBy(SnapshotViewImpl.class)
public interface SnapshotView extends IsWidget, HasKeyPressHandlers, HasKeyDownHandlers, HasMouseMoveHandlers {

  void reset();

  FileView addFileView(FileView before);

  void setPresenter(Presenter presenter);

  void expireSession();

  /** Presenter for a snapshot view. */
  @ImplementedBy(SnapshotPresenter.class)
  interface Presenter extends IsPresenter {

  }
}

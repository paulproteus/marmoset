package edu.umd.review.gwt.view;

import com.allen_sauer.gwt.dnd.client.DragController;
import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import edu.umd.review.gwt.presenter.FilePresenter;
import edu.umd.review.gwt.presenter.IsPresenter;
import edu.umd.review.gwt.rpc.dto.FileDto;
import edu.umd.review.gwt.rpc.dto.RubricDto;
import edu.umd.review.gwt.view.impl.FileViewImpl;

/**
 * Interface for display of a single file, with comments.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
@ImplementedBy(FileViewImpl.class)
public interface FileView extends IsWidget {
  /** Set the name of the file under display. */
  void setFileName(String name);

  /** Set the file to display. 
   * @param elideCode TODO*/
  void setFile(FileDto file, boolean elideCode);

  /** Return a view for a thread at {@code line}.
   * @param before {@code ThreadView} to insert before, if null insert at end of list
   */
  ThreadView getThreadView(int line, ThreadView before);

  /** Delete a thread view. */
  void deleteThreadView(int line, ThreadView view);

  /** Set the presenter to drive the view of this file. */
  void setPresenter(Presenter presenter);

  /** Interface for presenters that can drive the view of a file. */
  @ImplementedBy(FilePresenter.class)
  public interface Presenter extends IsPresenter {
    /** Start a new thread on {@code line}. */
    void onNewThreadAction(int line);

    /** Start a new thread on {@code line} scoring {@code rubric}.*/
    void newThreadWithRubric(int line, RubricDto rubric);

    /**
     * Register a drop controller for a line of code. Rubrics get dropped on lines to start a scored
     * thread.
     */
    void registerDropController(DropController controller);
    void unregisterDropController(DropController controller);
    
    void showFile(boolean elideCode);
  }
}

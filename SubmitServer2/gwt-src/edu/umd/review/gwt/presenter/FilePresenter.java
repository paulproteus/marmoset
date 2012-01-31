package edu.umd.review.gwt.presenter;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.allen_sauer.gwt.dnd.client.DragController;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import edu.umd.review.common.action.NewThreadAction;
import edu.umd.review.gwt.GwtUtils;
import edu.umd.review.gwt.PresenterFactory;
import edu.umd.review.gwt.rpc.dto.FileDto;
import edu.umd.review.gwt.rpc.dto.RubricDto;
import edu.umd.review.gwt.rpc.dto.ThreadDto;
import edu.umd.review.gwt.view.FileView;
import edu.umd.review.gwt.view.ScrollManager;
import edu.umd.review.gwt.view.ThreadView;

/**
 * Presenter to drive the view of a single file with comments.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public class FilePresenter extends AbstractPresenter implements FileView.Presenter {
  private static final Logger logger = Logger.getLogger(FilePresenter.class.getName());
  private final FileView view;
  private final PresenterFactory presenterFactory;
  private final DispatchAsync dispatch;
  private final FileDto file;
  private final ScrollManager scrollManager;
  private final PickupDragController dragController;
  private final Map<Integer, ThreadView.Presenter> threadPresenters = Maps.newTreeMap();

  @Inject
  FilePresenter(@Assisted FileView view, PresenterFactory presenterFactory,
      ScrollManager scrollManager, PickupDragController dragController, DispatchAsync dispatch,
      @Assisted FileDto file) {
    this.view = view;
    this.file = file;
    this.dispatch = dispatch;
    this.presenterFactory = presenterFactory;
    this.scrollManager = scrollManager;
    this.dragController = dragController;
  }

  @Override
  public void start() {
    view.setPresenter(this);
    view.setFileName(file.getPath());
    showFile();
  }

  @Override
  public void registerDropController(DropController controller) {
    dragController.registerDropController(controller);
  }

  @Override
  public void onNewThreadAction(final int line) {
    dispatch.execute(new NewThreadAction(file.getPath(), line), new AsyncCallback<ThreadDto>() {

      @Override
      public void onFailure(Throwable caught) {
        GwtUtils.wrapAndThrow(caught);
      }

      @Override
      public void onSuccess(ThreadDto thread) {
        displayNewThread(line, thread);
      }

    });
  }

  @Override
  public void newThreadWithRubric(final int line, RubricDto rubric) {
    dispatch.execute(new NewThreadAction(file.getPath(), line, rubric), new AsyncCallback<ThreadDto>() {

      @Override
      public void onFailure(Throwable caught) {
        GwtUtils.wrapAndThrow(caught);
      }

      @Override
      public void onSuccess(ThreadDto thread) {
        displayNewThread(line, thread);
      }

    });
  }

  @Override
  public void finish() {
    Iterator<Map.Entry<Integer, ThreadView.Presenter>> iter = threadPresenters.entrySet()
        .iterator();
    while (iter.hasNext()) {
      Map.Entry<Integer, ThreadView.Presenter> entry = iter.next();
      iter.remove();
      entry.getValue().finish();
    }
  }

  @Override
  public DragController getDragController() {
    return dragController;
  }

  private void displayNewThread(final int line, ThreadDto thread) {
    ThreadView threadView = view.getThreadView(line, null);
    ThreadView.Presenter threadPresenter = presenterFactory.makeThreadPresenter(threadView,
        thread);
    threadPresenter.start();

    threadPresenters.put(thread.getId(), threadPresenter);
  }

  private void showFile() {
    view.setFile(file);
    scrollManager.registerFile(file.getPath(), view);
    for (ThreadDto thread : file.getThreads()) {
      ThreadView threadView = view.getThreadView(thread.getLine(), null);
      ThreadView.Presenter threadPresenter = presenterFactory.makeThreadPresenter(threadView,
          thread);
      threadPresenter.start();
      ThreadView.Presenter old = threadPresenters.put(thread.getId(), threadPresenter);
      if (old != null) {
        old.finish();
      }
    }
  }
}

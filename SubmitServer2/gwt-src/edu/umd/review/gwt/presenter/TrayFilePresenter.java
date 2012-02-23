package edu.umd.review.gwt.presenter;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.shared.ResettableEventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import edu.umd.review.gwt.PresenterFactory;
import edu.umd.review.gwt.event.NewDraftEvent;
import edu.umd.review.gwt.rpc.dto.FileDto;
import edu.umd.review.gwt.rpc.dto.ThreadDto;
import edu.umd.review.gwt.view.ScrollManager;
import edu.umd.review.gwt.view.TrayFileView;
import edu.umd.review.gwt.view.TrayThreadView;

/**
 * Presenter to drive {@link TrayFileView}.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public class TrayFilePresenter extends AbstractPresenter implements TrayFileView.Presenter,
        NewDraftEvent.Handler {
  private static final Logger logger = Logger.getLogger(TrayFilePresenter.class.getName());
  private final TrayFileView view;
  private final FileDto file;
  private final PresenterFactory presenterFactory;
  private final ScrollManager scrollManager;
  private final ResettableEventBus eventBusWrapper;

  private List<TrayThreadView.Presenter> threadPresenters = Lists.newArrayList();
  private SortedMap<ThreadDto, TrayThreadView> threadViews = Maps.newTreeMap();

  @Inject
  TrayFilePresenter(@Assisted TrayFileView view, PresenterFactory presenterFactory,
      ScrollManager scrollManager, EventBus eventBus, @Assisted FileDto file) {
    this.view = view;
    this.file = file;
    this.presenterFactory = presenterFactory;
    this.scrollManager = scrollManager;
    this.eventBusWrapper = new ResettableEventBus(eventBus);
  }

  @Override
  public void start() {
    view.setPresenter(this);
    view.setFile(file);
    for (ThreadDto thread : file.getThreads()) {
      TrayThreadView threadView = view.insertThread(null);
      threadViews.put(thread, threadView);
      TrayThreadView.Presenter presenter = presenterFactory.makeTrayThreadPresenter(threadView,
          thread);
      threadPresenters.add(presenter);
      presenter.start();
    }
    eventBusWrapper.addHandler(NewDraftEvent.TYPE, this);
  }

  @Override
  public void openThisFile() {
    scrollManager.scrollFile(file.getPath());
  }

  @Override
  public void finish() {
      eventBusWrapper.removeHandlers();
    Iterator<TrayThreadView.Presenter> iter = threadPresenters.iterator();
    while (iter.hasNext()) {
      TrayThreadView.Presenter p = iter.next();
      iter.remove();
      p.finish();
    }
    threadViews.clear();
  }

    @Override
  public void onNewDraft(NewDraftEvent event) {
      String eventFile = event.getThread().getFile();
      if (eventFile != null && !eventFile.equals(file.getPath())) {
          return;
      }
      if (threadViews.containsKey(event.getThread())) {
          // Leave it to the thread presenter to handle the new draft event
          return;
      }
      // If we get here, this new draft must also have started a new thread, since there's no thread
      // view already associated with it.
      TrayThreadView before = null;
      for (Entry<ThreadDto, TrayThreadView> entry : threadViews.entrySet()) {
          if (entry.getKey().compareTo(event.getThread()) > 0) {
              before = entry.getValue();
              break;
          }
      }
      TrayThreadView threadView = view.insertThread(before);
    TrayThreadView.Presenter presenter = presenterFactory.makeTrayThreadPresenter(threadView,
                                                                                  event.getThread());
    threadPresenters.add(presenter);
    threadViews.put(event.getThread(), threadView);
    presenter.start();
  }
}

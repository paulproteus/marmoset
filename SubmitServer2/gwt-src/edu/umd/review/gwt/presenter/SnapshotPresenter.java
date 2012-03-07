package edu.umd.review.gwt.presenter;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Provider;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.shared.ResettableEventBus;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import edu.umd.review.gwt.PresenterFactory;
import edu.umd.review.gwt.event.HotkeyHandler;
import edu.umd.review.gwt.event.SessionExpiryEvent;
import edu.umd.review.gwt.rpc.dto.FileDto;
import edu.umd.review.gwt.view.FileView;
import edu.umd.review.gwt.view.GeneralCommentsView;
import edu.umd.review.gwt.view.SnapshotView;

/**
 * Handles the presentation of a single snapshot.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public class SnapshotPresenter extends AbstractPresenter implements SnapshotView.Presenter,
    SessionExpiryEvent.Handler {
  private static final Logger logger = Logger.getLogger(SnapshotPresenter.class.getName());
  private static final int millisPerMinute = 60000;
  private static final int timeoutMinutes = 5;

  private final SnapshotView view;
  private final Collection<? extends FileDto> files;
  private final ResettableEventBus eventBus;
  private final PresenterFactory presenterFactory;
  private final HotkeyHandler hotkey;
  private final Map<String, FileView> fileViews = Maps.newTreeMap();
  private final Map<String, FileView.Presenter> filePresenters = Maps.newTreeMap();
  private final Timer heartbeat;
  private final Provider<GeneralCommentsView.Presenter> generalCommmentsPresenterProvider;

  private boolean isElided = true;
  private int idleMinutes;
  private GeneralCommentsView.Presenter generalCommentsPresenter;

  @Inject
  SnapshotPresenter(@Assisted SnapshotView view, EventBus eventBus,
      PresenterFactory presenterFactory, HotkeyHandler hotkey, @Assisted Collection<? extends FileDto> files,
      Provider<GeneralCommentsView.Presenter> generalCommentsPresenterProvider) {
    this.view = view;
    this.files = files;
    this.presenterFactory = presenterFactory;
    this.hotkey = hotkey;
    this.generalCommmentsPresenterProvider = generalCommentsPresenterProvider;
    this.eventBus = new ResettableEventBus(eventBus);
    heartbeat = new Timer() {
      @Override
      public void run() {
        idleMinutes += 1;
        if (idleMinutes > timeoutMinutes) {
          SnapshotPresenter.this.eventBus.fireEvent(new SessionExpiryEvent());
        }
      }
    };
  }

  @Override
  public void start() {
    view.reset();
    view.setPresenter(this);
    view.addKeyPressHandler(hotkey);
    eventBus.addHandler(SessionExpiryEvent.getType(), this);
    view.setElisionStatus(isElided);
    for (FileDto file : files) {
      FileView fileView = view.addFileView(null);
      fileViews.put(file.getPath(), fileView);
      FileView.Presenter filePresenter = presenterFactory.makeFilePresenter(fileView, file);
      filePresenter.start();
      filePresenter.showFile(isElided);
      filePresenters.put(file.getPath(), filePresenter);
    }
    heartbeat.scheduleRepeating(millisPerMinute);
    view.addMouseMoveHandler(new MouseMoveHandler() {
      @Override
      public void onMouseMove(MouseMoveEvent event) {
        resetIdle();
      }
    });
    // TODO(rwsims): Adding a keypress handler here to reset the idle time seems to a) not work and
    // b) screw up the hotkey handling. This requires investigation.
    generalCommentsPresenter = generalCommmentsPresenterProvider.get();
    generalCommentsPresenter.start();
  }

  @Override
  public void finish() {
    if (generalCommentsPresenter != null) {
      generalCommentsPresenter.finish();
      generalCommentsPresenter = null;
    }
    Iterator<FileView.Presenter> iter = filePresenters.values().iterator();
    while (iter.hasNext()) {
      FileView.Presenter p = iter.next();
      iter.remove();
      p.finish();
    }
    eventBus.removeHandlers();
  }

  private void resetIdle() {
    idleMinutes = 0;
  }

  @Override
  public void onSessionExpiry(SessionExpiryEvent event) {
    logger.info("Session expiry, reloading window.");
    view.expireSession();
  }
  
  @Override
  public void toggleElision() {
    isElided = !isElided;
    for (FileView.Presenter filePresenter : filePresenters.values()) {
    	filePresenter.showFile(isElided);
    }
    view.setElisionStatus(isElided);
  }
}

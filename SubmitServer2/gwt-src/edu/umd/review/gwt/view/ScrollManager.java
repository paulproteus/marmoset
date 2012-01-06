package edu.umd.review.gwt.view;

import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.inject.Singleton;

import edu.umd.review.gwt.rpc.dto.ThreadDto;

/**
 * Class to manage objects that can get scrolled into view: comments, threads, files. Creates an
 * instance of a ScrollPanel, which needs to be the panel that contains all registered objects.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
@Singleton
public class ScrollManager {
  private static final Logger logger = Logger.getLogger(ScrollManager.class.getName());

  private final ScrollPanel panel = new ScrollPanel();
  private final SortedMap<ThreadDto, ThreadView> threadViews = Maps.newTreeMap();
  private final Map<Integer, ThreadDto> threadIdMap = Maps.newHashMap();
  private final Map<String, FileView> fileViewMap = Maps.newHashMap();

  public ScrollPanel getScrollPanel() {
    return this.panel;
  }

  public void registerThread(ThreadDto thread, ThreadView view) {
    threadViews.put(thread, view);
    threadIdMap.put(thread.getId(), thread);
  }

  public void unregisterThread(ThreadDto thread) {
    threadViews.remove(thread);
    threadIdMap.remove(thread.getId());
  }

  public void scrollNextThread() {
    if (threadViews.size() > 100) {
      logger.log(Level.WARNING, "Iterating over large number of threads: please rewrite me");
    }
    if (threadViews.isEmpty()) {
      return;
    }
    SortedSet<Integer> tops = Sets.newTreeSet();
    for (ThreadView view : threadViews.values()) {
      tops.add(view.asWidget().getAbsoluteTop() - panel.getAbsoluteTop());
    }
    // We take a tail set on 1 because we want comments with tops strictly greater than zero, but
    // tailSet gives greater than or equal to semantics.
    SortedSet<Integer> tail = tops.tailSet(1);
    if (tail.isEmpty()) {
      panel.setVerticalScrollPosition(panel.getMaximumVerticalScrollPosition());
    } else {
      int pos = panel.getVerticalScrollPosition();
      pos += tail.first();
      panel.setVerticalScrollPosition(pos);
    }
  }

  public void scrollPrevThread() {
    if (threadViews.size() > 100) {
      logger.log(Level.WARNING, "Iterating over large number of threads: please rewrite me");
    }
    if (threadViews.isEmpty()) {
      return;
    }
    SortedSet<Integer> tops = Sets.newTreeSet();
    for (ThreadView view : threadViews.values()) {
      tops.add(view.asWidget().getAbsoluteTop() - panel.getAbsoluteTop());
    }
    SortedSet<Integer> head = tops.headSet(0);
    if (head.isEmpty()) {
      panel.setVerticalScrollPosition(0);
    } else {
      int pos = panel.getVerticalScrollPosition();
      pos += head.last();
      panel.setVerticalScrollPosition(pos);
    }
  }

  public void scrollThread(ThreadDto thread) {
    Preconditions.checkNotNull(thread);
    scrollToWidget(threadViews.get(thread));
  }

  public void scrollThread(int threadId) {
    Preconditions.checkArgument(threadId > 0);
    scrollThread(threadIdMap.get(threadId));
  }

  public void registerFile(String path, FileView view) {
    fileViewMap.put(path, view);
  }

  public void unregisterFile(String path) {
    fileViewMap.remove(path);
  }

  public void scrollFile(String path) {
    scrollToWidget(fileViewMap.get(path));
  }

  private void scrollToWidget(IsWidget widget) {
    if (widget == null) {
      return;
    }
    int pos = panel.getVerticalScrollPosition();
    int delta = widget.asWidget().getAbsoluteTop() - panel.getAbsoluteTop();
    panel.setVerticalScrollPosition(pos + delta);
  }
}

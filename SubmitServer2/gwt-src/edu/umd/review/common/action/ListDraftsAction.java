package edu.umd.review.common.action;

import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;

import net.customware.gwt.dispatch.shared.Action;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.umd.review.gwt.rpc.dto.ThreadDto;

public class ListDraftsAction implements Action<ListDraftsAction.Result> {
  public static class Result implements ActionResult {
    private TreeMap<String, TreeSet<ThreadDto>> threadsByFile = Maps.newTreeMap();

    public TreeMap<String, TreeSet<ThreadDto>> getThreadsByFile() {
      return threadsByFile;
    }

    public void addThreads(String file, Collection<ThreadDto> threads) {
      threadsByFile.put(file, Sets.newTreeSet(threads));
    }
  }
}

package edu.umd.review.common.action;

import java.util.Collection;
import java.util.TreeSet;

import net.customware.gwt.dispatch.shared.Action;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import edu.umd.review.gwt.rpc.dto.ThreadDto;

public class ListGeneralCommentsAction implements Action<ListGeneralCommentsAction.Response> {  
  public static class Response implements ActionResult {
    private TreeSet<ThreadDto> threads = Sets.newTreeSet();
    
    private Response() {}
    
    public Response(Collection<? extends ThreadDto> threads) {
      Preconditions.checkNotNull(threads);
      this.threads.addAll(threads);
    }
    
    public TreeSet<ThreadDto> getThreads() {
      return threads;
    }
  }
}

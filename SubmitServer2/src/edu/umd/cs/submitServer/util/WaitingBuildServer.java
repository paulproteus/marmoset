package edu.umd.cs.submitServer.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Submission;

public class WaitingBuildServer {
  private static ConcurrentLinkedQueue<WaitingBuildServer> waiting = new ConcurrentLinkedQueue<WaitingBuildServer>();
  
  public static boolean offerSubmission(@Nonnull Project project, @Nonnull Submission submission) {
     for(Iterator<WaitingBuildServer> i = waiting.iterator(); i.hasNext(); ) {
      WaitingBuildServer bs = i.next();
      if (bs.offer(project, submission)) {
        i.remove();
        return true;
      }
    }
    return false;
  }
  
  public static @CheckForNull Submission waitForSubmission(Collection<Integer> courses, long milliseconds) throws InterruptedException {
    WaitingBuildServer bs = new WaitingBuildServer(courses);
    
    waiting.add(bs);
    Submission s = null;
    try {
       s = bs.poll(milliseconds, TimeUnit.MILLISECONDS);
       
       return s;
    } finally {
      if (s == null) {
        waiting.remove(bs);
      }
    }
  }
  
  private final Set<Integer> courses;
  
  private final SynchronousQueue<Submission> queue = new SynchronousQueue<Submission>();
  
  private WaitingBuildServer(Collection<Integer> courses) {
    this.courses = new HashSet<Integer>(courses);
  }
  
  private @CheckForNull Submission poll() throws InterruptedException {
    return poll(5, TimeUnit.MINUTES);
  }
  private  @CheckForNull Submission poll(long timeout, TimeUnit units) throws InterruptedException {
    return queue.poll(timeout, units);
  }
  private boolean offer(Project project, Submission submission) {
    if (!courses.contains(project.getCoursePK()))
      return false;
    return queue.offer(submission);
  }

}

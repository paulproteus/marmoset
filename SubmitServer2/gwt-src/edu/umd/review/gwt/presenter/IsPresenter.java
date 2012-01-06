package edu.umd.review.gwt.presenter;

/**
 * Interface for presenters. Presenters <b>should not</b> do any initialization of the view or
 * anything else in their constructor, that is what the {@code start()} method is for. In other
 * words, in testing a Presenter should not have to set any mock expectations on its constructor
 * arguments.
 *
 * This makes it easier to test presenters, and makes it easier to recycle them into a pool of
 * presenters, should that become desirable.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public interface IsPresenter {
  /** Initialize the view and any necessary bindings. */
  public void start();

  /**
   * Unregister all event handlers and prepare presenter for garbage collection. Presenters should
   * recursively finish any presenters they create.
   */
  public void finish();
}

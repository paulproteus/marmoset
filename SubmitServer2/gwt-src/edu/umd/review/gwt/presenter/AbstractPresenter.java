package edu.umd.review.gwt.presenter;

/**
 * Abstract base class for all presenters.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
public abstract class AbstractPresenter implements IsPresenter {

  @Override
  public void finish() {
    // do nothing unless overriden.
  }
}

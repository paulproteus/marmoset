package edu.umd.review.server.handler;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.DispatchException;
import net.customware.gwt.dispatch.shared.Result;

/** Abstract action handler that does nothing on rollback. */
public abstract class AbstractActionHandler<A extends Action<R>, R extends Result> implements
    ActionHandler<A, R> {

  public void rollback(A action, R result, ExecutionContext arg2) throws DispatchException {};
}

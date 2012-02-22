package edu.umd.review.gwt.presenter;

import javax.inject.Inject;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.common.base.Preconditions;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.umd.review.common.action.NewThreadAction;
import edu.umd.review.gwt.GwtUtils;
import edu.umd.review.gwt.PresenterFactory;
import edu.umd.review.gwt.rpc.dto.ThreadDto;
import edu.umd.review.gwt.view.GeneralCommentsView;
import edu.umd.review.gwt.view.ThreadView;

public class GeneralCommentsPresenter implements GeneralCommentsView.Presenter {
  private final GeneralCommentsView view;
  private final DispatchAsync dispatch;
  private final PresenterFactory presenterFactory;

  @Inject
  public GeneralCommentsPresenter(GeneralCommentsView view, DispatchAsync dispatch, PresenterFactory presenterFactory) {
    this.view = Preconditions.checkNotNull(view);
    this.dispatch = Preconditions.checkNotNull(dispatch);
    this.presenterFactory = Preconditions.checkNotNull(presenterFactory);
  }
  
  @Override
  public void start() {
    this.view.setPresenter(this);
  }

  @Override
  public void finish() {
    this.view.setPresenter(null);
  }

  @Override
  public void createNewThread() {
    dispatch.execute(new NewThreadAction(), new AsyncCallback<ThreadDto>() {
      @Override
      public void onFailure(Throwable caught) {
        GwtUtils.wrapAndThrow(caught);
      }

      @Override
      public void onSuccess(ThreadDto thread) {
        ThreadView threadView = view.newThreadView();
        ThreadView.Presenter threadPresenter = presenterFactory.makeThreadPresenter(threadView,
                                                                                    thread);
        threadPresenter.start();
      }
    });
  }
}

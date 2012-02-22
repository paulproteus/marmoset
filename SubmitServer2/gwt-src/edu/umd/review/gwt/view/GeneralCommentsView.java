package edu.umd.review.gwt.view;

import java.util.Collection;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import edu.umd.review.gwt.presenter.IsPresenter;
import edu.umd.review.gwt.rpc.dto.ThreadDto;
import edu.umd.review.gwt.view.impl.GeneralCommentsViewImpl;

@ImplementedBy(GeneralCommentsViewImpl.class)
public interface GeneralCommentsView extends IsWidget {
  public interface Presenter extends IsPresenter {
    void createNewThread();
  }
  
  void setVisible(boolean visible);
  void setThreads(Collection<ThreadDto> threads);
  ThreadView newThreadView();
}

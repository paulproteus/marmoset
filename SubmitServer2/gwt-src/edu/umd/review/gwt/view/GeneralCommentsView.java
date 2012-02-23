package edu.umd.review.gwt.view;

import java.util.Collection;

import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import edu.umd.review.gwt.presenter.IsPresenter;
import edu.umd.review.gwt.rpc.dto.RubricDto;
import edu.umd.review.gwt.rpc.dto.ThreadDto;
import edu.umd.review.gwt.view.impl.GeneralCommentsViewImpl;

@ImplementedBy(GeneralCommentsViewImpl.class)
public interface GeneralCommentsView extends IsWidget {
  public interface Presenter extends IsPresenter {
    void createNewThread();
    void registerDropController(DropController controller);
    void unregisterDropController(DropController controller);
    void newThreadWithRubric(RubricDto rubric);
  }
  
  void setPresenter(Presenter presenter);
  void setVisible(boolean visible);
  void clear();
  ThreadView newThreadView();
}

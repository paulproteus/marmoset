package edu.umd.review.gwt.view;

import java.util.Map;
import java.util.TreeSet;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import edu.umd.review.gwt.presenter.IsPresenter;
import edu.umd.review.gwt.presenter.PublishDraftsPresenter;
import edu.umd.review.gwt.rpc.dto.ThreadDto;
import edu.umd.review.gwt.view.impl.PublishDraftsViewImpl;

/**
 * View for publishing draft comments within a single snapshot.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
@ImplementedBy(PublishDraftsViewImpl.class)
public interface PublishDraftsView extends IsWidget {

  /** Specify the presenter backing this view. */
  void setPresenter(Presenter presenter);

  /**
   * Set the collection of threads with drafts to publish.
   */
  void setDrafts(Map<String, TreeSet<ThreadDto>> drafts);

  void reset();

  /**
   * Interface for a publish drafts presenter.
   */
  @ImplementedBy(PublishDraftsPresenter.class)
  public interface Presenter extends IsPresenter {
    /** Set whether a draft should be published. */
    void setPublishStatus(ThreadDto draft, boolean status);

    /** Publish all marked drafts. */
    void doPublish();
  }
}

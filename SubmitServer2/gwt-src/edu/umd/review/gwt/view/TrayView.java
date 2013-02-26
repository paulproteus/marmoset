package edu.umd.review.gwt.view;

import java.util.Collection;
import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.ImplementedBy;

import edu.umd.review.gwt.presenter.IsPresenter;
import edu.umd.review.gwt.presenter.TrayPresenter;
import edu.umd.review.gwt.rpc.dto.FileDto;
import edu.umd.review.gwt.rpc.dto.RubricDto;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto;
import edu.umd.review.gwt.view.impl.TrayViewImpl;

/**
 * View to display rubrics, files and threads for the current snapshot.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
@ImplementedBy(TrayViewImpl.class)
public interface TrayView extends IsWidget {

  void setPresenter(Presenter presenter);

  /** Reset the view to be ready for a different snapshot. */
  void reset();

  /**
   * Insert a new file item view.
   *
   * @param before file view that should be after the new one, null if new view goes at the end.
   * @return the newly-inserted view.
   */
  TrayFileView insertFile(TrayFileView before);


  void insertAuthors(boolean isAuthor, Collection<? extends FileDto> files, Map<String, Integer> ratings);

  void setUnpublished(boolean visible);

  void clearRubrics();

  Widget addUnscoredRubric(RubricDto rubric);

  void removeUnscoredRubric(RubricDto rubric);

  void addScoredRubric(RubricDto rubric, RubricEvaluationDto evaluation);

  void removeScoredRubric(RubricDto rubric);

  /**
   * Presenter to drive a {@link TrayView}.
   */
  @ImplementedBy(TrayPresenter.class)
  interface Presenter extends IsPresenter {
    void publishAllDrafts();
    void rateReviewer(String reviewer, int rating);

    /**
     * Return a message to display on closing, or null if closing should go ahead without
     * confirmation.
     */
    String getCloseMessage();
  }
}

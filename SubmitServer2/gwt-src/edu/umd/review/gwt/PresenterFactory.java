package edu.umd.review.gwt;

import java.util.Collection;

import edu.umd.review.gwt.rpc.dto.CheckboxEvaluationDto;
import edu.umd.review.gwt.rpc.dto.CheckboxRubricDto;
import edu.umd.review.gwt.rpc.dto.CommentDto;
import edu.umd.review.gwt.rpc.dto.DropdownEvaluationDto;
import edu.umd.review.gwt.rpc.dto.DropdownRubricDto;
import edu.umd.review.gwt.rpc.dto.FileDto;
import edu.umd.review.gwt.rpc.dto.NumericEvaluationDto;
import edu.umd.review.gwt.rpc.dto.NumericRubricDto;
import edu.umd.review.gwt.rpc.dto.ThreadDto;
import edu.umd.review.gwt.view.CheckboxRubricEvaluationView;
import edu.umd.review.gwt.view.DraftView;
import edu.umd.review.gwt.view.DropdownRubricEvaluationView;
import edu.umd.review.gwt.view.FileView;
import edu.umd.review.gwt.view.NumericRubricEvaluationView;
import edu.umd.review.gwt.view.PublishDraftsView;
import edu.umd.review.gwt.view.SnapshotView;
import edu.umd.review.gwt.view.ThreadView;
import edu.umd.review.gwt.view.TrayFileView;
import edu.umd.review.gwt.view.TrayThreadView;
import edu.umd.review.gwt.view.TrayView;

/**
 * Factory class for creating presenter instances.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public interface PresenterFactory {
  TrayView.Presenter makeTrayPresenter(TrayView view, Collection<FileDto> files);
  DraftView.Presenter makeDraftPresenter(DraftView view, ThreadDto thread, CommentDto draft);
  ThreadView.Presenter makeThreadPresenter(ThreadView view, ThreadDto thread);
  SnapshotView.Presenter makeSnapshotPresenter(SnapshotView view, Collection<FileDto> files);
  FileView.Presenter makeFilePresenter(FileView view, FileDto file);
  TrayFileView.Presenter makeTrayFilePresenter(TrayFileView view, FileDto file);
  TrayThreadView.Presenter makeTrayThreadPresenter(TrayThreadView view, ThreadDto thread);
  PublishDraftsView.Presenter makePublishPresenter(PublishDraftsView view);
  NumericRubricEvaluationView.Presenter makeNumericEvaluationView(NumericRubricEvaluationView view,
                                                                  NumericRubricDto rubric,
                                                                  NumericEvaluationDto evaluation);
  DropdownRubricEvaluationView.Presenter makeDropdownEvaluationView(DropdownRubricEvaluationView view,
                                                                    DropdownRubricDto rubric,
                                                                    DropdownEvaluationDto evaluation);
  CheckboxRubricEvaluationView.Presenter makeCheckboxEvaluationView(CheckboxRubricEvaluationView view,
                                                                    CheckboxRubricDto rubric,
                                                                    CheckboxEvaluationDto evaluation);
}

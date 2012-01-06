package edu.umd.review.gwt.view;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

import edu.umd.review.gwt.presenter.IsPresenter;
import edu.umd.review.gwt.rpc.dto.CheckboxEvaluationDto;
import edu.umd.review.gwt.rpc.dto.CheckboxRubricDto;
import edu.umd.review.gwt.widget.RubricEvaluationControl;

public interface CheckboxRubricEvaluationView extends HasValue<String>, IsWidget {

	public interface Presenter extends IsPresenter, RubricEvaluationControl.Handler {}
  void setPresenter(Presenter presenter);

	void showEvaluation(CheckboxRubricDto rubic, CheckboxEvaluationDto evaluation);

	void setEditing(boolean editing);

  String getExplanation();
}

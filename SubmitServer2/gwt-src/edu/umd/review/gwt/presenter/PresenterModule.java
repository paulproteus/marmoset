package edu.umd.review.gwt.presenter;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.web.bindery.event.shared.EventBus;

import edu.umd.review.gwt.PresenterFactory;
import edu.umd.review.gwt.view.CheckboxRubricEvaluationView;
import edu.umd.review.gwt.view.DraftView;
import edu.umd.review.gwt.view.DropdownRubricEvaluationView;
import edu.umd.review.gwt.view.FileView;
import edu.umd.review.gwt.view.GeneralCommentsView;
import edu.umd.review.gwt.view.NumericRubricEvaluationView;
import edu.umd.review.gwt.view.PublishDraftsView;
import edu.umd.review.gwt.view.SnapshotView;
import edu.umd.review.gwt.view.ThreadView;
import edu.umd.review.gwt.view.TrayFileView;
import edu.umd.review.gwt.view.TrayThreadView;
import edu.umd.review.gwt.view.TrayView;

/**
 * GIN bindings relating to presenters.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
public class PresenterModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(EventBus.class).to(SimpleEventBus.class).asEagerSingleton();
    bind(GeneralCommentsView.Presenter.class).to(GeneralCommentsPresenter.class);

    install(new GinFactoryModuleBuilder()
      .implement(TrayView.Presenter.class, TrayPresenter.class)
      .implement(DraftView.Presenter.class, DraftPresenter.class)
      .implement(ThreadView.Presenter.class, ThreadPresenter.class)
      .implement(SnapshotView.Presenter.class, SnapshotPresenter.class)
      .implement(FileView.Presenter.class, FilePresenter.class)
      .implement(TrayFileView.Presenter.class, TrayFilePresenter.class)
      .implement(TrayThreadView.Presenter.class, TrayThreadPresenter.class)
      .implement(PublishDraftsView.Presenter.class, PublishDraftsPresenter.class)
      .implement(NumericRubricEvaluationView.Presenter.class, NumericEvaluationPresenter.class)
      .implement(CheckboxRubricEvaluationView.Presenter.class, CheckboxEvaluationPresenter.class)
      .implement(DropdownRubricEvaluationView.Presenter.class, DropdownEvaluationPresenter.class)
      .build(PresenterFactory.class));
  }
}

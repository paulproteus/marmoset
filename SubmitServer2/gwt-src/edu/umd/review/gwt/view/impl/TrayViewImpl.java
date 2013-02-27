package edu.umd.review.gwt.view.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;

import org.cobogw.gwt.user.client.ui.Rating;

import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import edu.umd.cs.marmoset.review.UniqueReviewerName;
import edu.umd.review.gwt.ClientConstants;
import edu.umd.review.gwt.rpc.dto.CommentDto;
import edu.umd.review.gwt.rpc.dto.FileDto;
import edu.umd.review.gwt.rpc.dto.RubricDto;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto.Status;
import edu.umd.review.gwt.rpc.dto.ThreadDto;
import edu.umd.review.gwt.view.ScrollManager;
import edu.umd.review.gwt.view.TrayFileView;
import edu.umd.review.gwt.view.TrayView;
import edu.umd.review.gwt.widget.RubricDragger;

/**
 * Implementation of {@link TrayView}.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
@Singleton
public class TrayViewImpl extends Composite implements TrayView, Window.ClosingHandler {
  /** UiBinder interface for {@code TrayViewImpl}. */
  interface TrayViewImplUiBinder extends UiBinder<Widget, TrayViewImpl> { }
  private static TrayViewImplUiBinder uiBinder = GWT.create(TrayViewImplUiBinder.class);

  @UiField FlowPanel authorPanel;
  @UiField FlowPanel rubricPanel;
  @UiField FlowPanel unscoredRubricPanel;
  @UiField FlowPanel scoredRubricPanel;
  @UiField FlowPanel filePanel;
  @UiField Hyperlink link;
  @UiField Anchor publishAllLink;
  @UiField Label unpublishedLabel;
  @UiField Label rubricLabel;
  @UiField Label unscoredLabel;
  @UiField Label scoredLabel;

  private Presenter presenter;
  private final AuthorColorFactory colorFactory = new AuthorColorFactory();
  private final Set<String> authorSet = Sets.newTreeSet();
  private final Provider<TrayFileView> fileViewProvider;
  private final ScrollManager scrollManager;
  private boolean isAuthor;

  @Inject
  public TrayViewImpl(Provider<TrayFileView> fileViewProvider, ScrollManager scrollManager) {
    this.fileViewProvider = fileViewProvider;
    initWidget(uiBinder.createAndBindUi(this));
    link.setTargetHistoryToken(ClientConstants.PUBLISH_TOKEN);
    this.scrollManager = scrollManager;
    Window.addWindowClosingHandler(this);
  }

  @UiHandler("publishAllLink")
  void onPublishAll(ClickEvent event) {
    presenter.publishAllDrafts();
  }

  @Override
  public void reset() {
    unscoredRubricPanel.clear();
    scoredRubricPanel.clear();
    filePanel.clear();
  }

  @Override
  public TrayFileView insertFile(TrayFileView before) {
    TrayFileView newFile = fileViewProvider.get();
    if (before != null) {
      int idx = filePanel.getWidgetIndex(before);
      filePanel.insert(newFile, idx);
    } else {
      filePanel.add(newFile);
    }
    return newFile;
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void insertAuthors(boolean isAuthor, Collection<? extends FileDto> files, Map<String, Integer> ratings) {
    this.isAuthor = isAuthor;
    authorSet.clear();
    for (FileDto file : files)
      for (ThreadDto thread : file.getThreads()) {
        for (CommentDto comment : thread.getPublishedComments()) {
          @UniqueReviewerName String author = comment.getAuthor();
          authorSet.add(author);
        }
      }
    authorPanel.clear();

    for (@UniqueReviewerName String author : authorSet) {
      authorPanel.add(getAuthorWidget(author, ratings.get(author)));
    }

  }

  private Widget getAuthorWidget(final @UniqueReviewerName String authorName, @CheckForNull Integer r) {
    Label l = new Label(authorName);
    String color = colorFactory.getColor(authorName);
    l.addStyleName(color);
    HorizontalPanel panel = new HorizontalPanel();
    panel.add(l);
    panel.setSpacing(5);
    panel.setStyleName(color);
    panel.setWidth("100%");
    if (r != null) {

      boolean unrated = r.intValue() == 0;
      final Rating rating = new Rating(r, 5);
      if (isAuthor) {
        if (unrated)
          rating.setTitle("Please rate this reviewer");
        else
          rating.setTitle("Click to change your rating");
        rating.addValueChangeHandler(new ValueChangeHandler<Integer>() {
          public void onValueChange(ValueChangeEvent<Integer> event) {
            rating.setTitle("Click to change your rating");
            presenter.rateReviewer(authorName, event.getValue());
          }
        });
      } else {
        if (unrated)
          rating.setTitle("Not rated");
        else
          rating.setTitle("Author's rating of this reviewer");
        rating.setReadOnly(true);
      }
      panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
      panel.add(rating);
    }

    return panel;
  }

  @Override
  public void setUnpublished(boolean visible) {
    unpublishedLabel.setVisible(visible);
    link.setVisible(visible);
    publishAllLink.setVisible(visible);
  }

  @Override
  public Widget addUnscoredRubric(RubricDto rubric) {
    rubricLabel.setVisible(true);
    rubricPanel.setVisible(true);
    unscoredLabel.setVisible(true);
    unscoredRubricPanel.setVisible(true);
    RubricDragger dragger = new RubricDragger(rubric);
    unscoredRubricPanel.add(dragger);
    return dragger;
  }

  @Override
  public void addScoredRubric(RubricDto rubric, final RubricEvaluationDto evaluation) {
    rubricLabel.setVisible(true);
    rubricPanel.setVisible(true);
    String name = evaluation.getPoints() + " " + rubric.getName();
    if (evaluation.getStatus() == Status.DRAFT
        || evaluation.getStatus() == Status.NEW)
        name = "* " + name;
    Anchor rubricLink = new Anchor(name);
    DOM.setElementProperty(rubricLink.getElement(), "title", rubric.getDescription());
    rubricLink.setStylePrimaryName("link");
    rubricLink.addStyleDependentName("block");
    rubricLink.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        scrollManager.scrollThread(evaluation.getThreadid());
      }
    });
    scoredRubricPanel.add(rubricLink);
  }

  @Override
  public void onWindowClosing(ClosingEvent event) {
    event.setMessage(presenter.getCloseMessage());
  }

  @Override
  public void removeUnscoredRubric(RubricDto rubric) {
    for (int i = 0; i < unscoredRubricPanel.getWidgetCount(); i++) {
      RubricDragger dragger = (RubricDragger) unscoredRubricPanel.getWidget(i);
      if (dragger.getRubric().equals(rubric)) {
        unscoredRubricPanel.remove(dragger);
        break;
      }
    }
    if (unscoredRubricPanel.getWidgetCount() == 0) {
      unscoredRubricPanel.setVisible(false);
      unscoredLabel.setVisible(false);
    }
  }

  @Override
  public void removeScoredRubric(RubricDto rubric) {
    for (int i = 0; i < scoredRubricPanel.getWidgetCount(); i++) {
      Anchor rubricAnchor = (Anchor) scoredRubricPanel.getWidget(i);
      if (rubricAnchor.getText().equals(rubric.getName())) {
        rubricAnchor.removeFromParent();
        break;
      }
    }
    if (scoredRubricPanel.getWidgetCount() == 0) {
      scoredRubricPanel.setVisible(false);
      scoredLabel.setVisible(false);
    }
  }

   @Override
  public void clearRubrics() {
    unscoredRubricPanel.clear();
    scoredRubricPanel.clear();
  }
}

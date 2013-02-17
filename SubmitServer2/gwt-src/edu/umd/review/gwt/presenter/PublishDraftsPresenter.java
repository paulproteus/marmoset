package edu.umd.review.gwt.presenter;


import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import edu.umd.review.common.action.ListDraftsAction;
import edu.umd.review.common.action.ListDraftsAction.Result;
import edu.umd.review.common.action.PublishAction;
import edu.umd.review.common.action.VoidResult;
import edu.umd.review.gwt.ClientConstants;
import edu.umd.review.gwt.GwtUtils;
import edu.umd.review.gwt.rpc.dto.CommentDto;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto;
import edu.umd.review.gwt.rpc.dto.ThreadDto;
import edu.umd.review.gwt.view.PublishDraftsView;

/**
 * Presenter to drive a view of drafts to publish.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
public class PublishDraftsPresenter extends AbstractPresenter
    implements PublishDraftsView.Presenter {
  private final DispatchAsync dispatch;
  private final PublishDraftsView view;
  private final EventBus eventBus;

  private Map<CommentDto, Boolean> commentStatusMap = Maps.newHashMap();
  private Map<RubricEvaluationDto, Boolean> evaluationStatusMap = Maps.newHashMap();

  @Inject
  PublishDraftsPresenter(@Assisted PublishDraftsView view, DispatchAsync dispatch,
      EventBus eventBus) {
    this.view = view;
    this.dispatch = dispatch;
    this.eventBus = eventBus;
  }

  @Override
  public void setPublishStatus(ThreadDto thread, boolean status) {
    if (thread.getDraft() != null) {
      commentStatusMap.put(thread.getDraft(), status);
    }
    if (thread.getRubricEvaluation() != null) {
      evaluationStatusMap.put(thread.getRubricEvaluation(), status);
    }
  }

  @Override
  public void doPublish() {
    final HashSet<Integer> commentsToPublish = Sets.newHashSet();
    final HashSet<Integer> evaluationsToPublish = Sets.newHashSet();
    for (Entry<CommentDto, Boolean> entry : commentStatusMap.entrySet()) {
      if (entry.getValue()) {
        commentsToPublish.add(entry.getKey().getId());
      }
    }
    for (Entry<RubricEvaluationDto, Boolean> entry : evaluationStatusMap.entrySet()) {
      if (entry.getValue()) {
        evaluationsToPublish.add(entry.getKey().getRubricEvaluationPK());
      }
    }
    dispatch.execute(new PublishAction(commentsToPublish, evaluationsToPublish), new AsyncCallback<VoidResult>() {

      @Override
      public void onFailure(Throwable caught) {
        GwtUtils.wrapAndThrow(caught);
      }

      @Override
      public void onSuccess(VoidResult result) {
        History.newItem(ClientConstants.REVIEW_TOKEN);
      }
    });
  }

  @Override
  public void start() {
    view.setPresenter(this);
    dispatch.execute(new ListDraftsAction(), new AsyncCallback<ListDraftsAction.Result>() {
      @Override
      public void onFailure(Throwable caught) {
        GwtUtils.wrapAndThrow(caught);
      }

      @Override
      public void onSuccess(Result result) {
        Map<String, TreeSet<ThreadDto>> drafts = result.getThreadsByFile();
        view.setDrafts(drafts);
        for (Collection<ThreadDto> ds : drafts.values()) {
          for (ThreadDto thread : ds) {
            setPublishStatus(thread, true);
          }
        }
      }
    });
  }
}

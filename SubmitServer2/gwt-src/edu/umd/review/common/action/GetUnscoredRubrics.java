package edu.umd.review.common.action;

import java.util.Collection;
import java.util.TreeSet;

import net.customware.gwt.dispatch.shared.Action;

import com.google.common.collect.Sets;

import edu.umd.review.gwt.rpc.dto.RubricDto;


public class GetUnscoredRubrics implements Action<GetUnscoredRubrics.Result> {

  public static class Result implements ActionResult {
    private TreeSet<RubricDto> rubrics = Sets.newTreeSet();

    private Result() {}

    public Result(Collection<RubricDto> rubrics) {
      this.rubrics.addAll(rubrics);
    }

    public TreeSet<RubricDto> getRubrics() {
      return this.rubrics;
    }
  }
}

package edu.umd.review.common.action;

import net.customware.gwt.dispatch.shared.Action;
import edu.umd.review.gwt.rpc.dto.RubricDto;
import edu.umd.review.gwt.rpc.dto.ThreadDto;

public class NewThreadAction implements Action<ThreadDto> {
  private String path;
  private int line;
  private RubricDto rubric;

  public NewThreadAction() {
    this(null, 0, null);
  }

  public NewThreadAction(String path, int line) {
    this(path, line, null);
  }

  public NewThreadAction(String path, int line, RubricDto rubric) {
    this.path = path;
    this.line = line;
    this.rubric = rubric;
  }

  public String getPath() {
    return path;
  }

  public int getLine() {
    return line;
  }

  public RubricDto getRubric() {
    return rubric;
  }
}

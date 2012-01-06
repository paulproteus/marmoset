package edu.umd.review.common.action;

import net.customware.gwt.dispatch.shared.Result;

public class VoidResult implements Result {
  private static final VoidResult instance = new VoidResult();

  public static VoidResult get() {
    return instance;
  }

  private VoidResult() {}
}

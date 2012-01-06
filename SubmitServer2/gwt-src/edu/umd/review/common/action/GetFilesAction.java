package edu.umd.review.common.action;

import java.util.Collection;
import java.util.TreeSet;

import net.customware.gwt.dispatch.shared.Action;
import edu.umd.review.gwt.rpc.dto.FileDto;

public class GetFilesAction implements Action<GetFilesAction.Result> {

  public static class Result implements ActionResult {
    private TreeSet<FileDto> files = new TreeSet<FileDto>();

    /** @deprecated GWT-only. */
    @Deprecated
    private Result() {}

    public Result(Collection<FileDto> files) {
      this.files.addAll(files);
    }

    public TreeSet<FileDto> getFiles() {
      return files;
    }
  }
}

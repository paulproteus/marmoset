package edu.umd.review.common.action;

import java.util.ArrayList;
import java.util.Collection;

import net.customware.gwt.dispatch.shared.Action;
import edu.umd.review.gwt.rpc.dto.FileDto;

public class GetFilesAction implements Action<GetFilesAction.Result> {

  public static class Result implements ActionResult {
    private ArrayList<FileDto> files = new ArrayList<FileDto>();

    /** @deprecated GWT-only. */
    @Deprecated
    private Result() {}

    public Result(Collection<FileDto> files) {
      this.files.addAll(files);
    }

    public Collection<? extends FileDto> getFiles() {
      return files;
    }
  }
}

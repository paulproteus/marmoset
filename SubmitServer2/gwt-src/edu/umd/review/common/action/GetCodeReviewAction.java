package edu.umd.review.common.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.shared.Action;
import edu.umd.review.gwt.rpc.dto.FileDto;

public class GetCodeReviewAction implements Action<GetCodeReviewAction.Result> {

  public static class Result implements ActionResult {
    private ArrayList<FileDto> files = new ArrayList<FileDto>();
    private Map<String, Integer> ratings = new HashMap<String, Integer>(); 
  

	/** @deprecated GWT-only. */
    @Deprecated
    private Result() {}

    public Result(Collection<FileDto> files, Map<String, Integer> ratings) {
      this.files.addAll(files);
      this.ratings.putAll(ratings);
     
    }

    public Collection<? extends FileDto> getFiles() {
      return files;
    }
    public Map<String, Integer> getRatings() {
        return ratings;
      }
    
    
  }
}

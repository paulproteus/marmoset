// Copyright 2011 rwsims@gmail.com (Ryan W Sims)
package edu.umd.review.gwt;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;

/**
 * @author rwsims@gmail.com (Ryan W Sims)
 */
public interface CodeReviewResources extends ClientBundle {
  CodeReviewResources INSTANCE = GWT.create(CodeReviewResources.class);

  @Source("colors.css")
  AuthorColors colors();

  @Source("rubrics.css")
  RubricStyle rubricStyle();
}

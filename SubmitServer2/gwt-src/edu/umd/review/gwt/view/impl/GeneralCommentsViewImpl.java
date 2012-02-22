package edu.umd.review.gwt.view.impl;

import java.util.Collection;

import javax.inject.Singleton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import edu.umd.review.gwt.rpc.dto.ThreadDto;
import edu.umd.review.gwt.view.GeneralCommentsView;
import edu.umd.review.gwt.view.ThreadView;

@Singleton
public class GeneralCommentsViewImpl extends Composite implements GeneralCommentsView {
  interface ViewBinder extends UiBinder<Widget, GeneralCommentsViewImpl> {}
  private static ViewBinder uiBinder = GWT.create(ViewBinder.class);

  public GeneralCommentsViewImpl() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setThreads(Collection<ThreadDto> threads) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public ThreadView newThreadView() {
    // TODO Auto-generated method stub
    return null;
  }
}

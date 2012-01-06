package edu.umd.review.gwt.rpc;

import net.customware.gwt.dispatch.client.DefaultExceptionHandler;
import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.dispatch.client.ExceptionHandler;
import net.customware.gwt.dispatch.client.standard.StandardDispatchService;
import net.customware.gwt.dispatch.client.standard.StandardDispatchServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class DispatchModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(ExceptionHandler.class).to(DefaultExceptionHandler.class).in(Singleton.class);
    bind(DispatchAsync.class).to(ReviewDispatch.class);
  }

  @Provides @Singleton
  StandardDispatchServiceAsync getDispatchService() {
    return GWT.create(StandardDispatchService.class);
  }
}

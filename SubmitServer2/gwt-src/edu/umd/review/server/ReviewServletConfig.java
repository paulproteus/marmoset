package edu.umd.review.server;

import net.customware.gwt.dispatch.server.guice.GuiceStandardDispatchServlet;
import net.customware.gwt.dispatch.server.guice.ServerDispatchModule;

import com.google.gwt.logging.server.RemoteLoggingServiceImpl;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

import edu.umd.review.server.handler.HandlerModule;

/**
 * Servlet configuration for review app.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public class ReviewServletConfig extends GuiceServletContextListener {
  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new CodeReviewServletModule(),
                                new ReviewServerModule(),
                                new ServerDispatchModule(MarmosetLoggingDispatch.class),
                                new HandlerModule());
  }

  private static class CodeReviewServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
     bind(RemoteLoggingServiceImpl.class).in(Singleton.class);
     serve("/codereview/remote_logging").with(RemoteLoggingServiceImpl.class);

     // gwt-dispatch command RPC servlet
     serve("/codereview/dispatch").with(GuiceStandardDispatchServlet.class);
    }
  }
}

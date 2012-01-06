package edu.umd.review.server;

import javax.servlet.ServletContext;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import edu.umd.cs.submitServer.SubmitServerDatabaseProperties;

/**
 * Guice config for the review server.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public class ReviewServerModule extends AbstractModule {
  @Override
  protected void configure() {
  }

  @Provides
  SubmitServerDatabaseProperties getDatabaseProperties(ServletContext context) {
  	return new SubmitServerDatabaseProperties(context);
  }
}

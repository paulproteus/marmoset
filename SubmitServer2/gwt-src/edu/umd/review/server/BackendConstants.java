package edu.umd.review.server;

/**
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public final class BackendConstants {
  public static final String DERBY_URL = "jdbc:derby:memory:testDb;";
  public static final String DERBY_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
  public static final String DERBY_SHUTDOWN_URL = DERBY_URL +  "shutdown=true";

  public static final String SESSION_USERNAME = "username";
  public static final String SESSION_USERID = "userId";
  public static final String SESSION_SNAPSHOT = "snapshot";
  public static final String SESSION_SUBMISSION = "submission";
  public static final String SESSION_ISAUTHOR = "is_author";

  private BackendConstants() { }
}

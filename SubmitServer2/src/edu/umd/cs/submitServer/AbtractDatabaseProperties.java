package edu.umd.cs.submitServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;

public abstract class AbtractDatabaseProperties {

	protected final String databaseURL;
	protected final String databaseUser;
	protected final String databasePassword;
	protected final String contextPath;

	/**
	 * @param databaseURL
	 * @param databaseName
	 * @param databaseOptions
	 * @param databaseUser
	 * @param databasePassword
	 * @param databaseDriver
	 * @throws ClassNotFoundException
	 */
	public AbtractDatabaseProperties(ServletContext servletContext,
			String databaseURL, String databaseName,
			String databaseOptions, String databaseUser,
			String databasePassword, String databaseDriver)  {

		contextPath = servletContext.getContextPath();
		String driver =getInitParameterOrOverride(databaseDriver,
				servletContext);
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Unable to load sql driver " + driver);
		}

		String url = getInitParameterOrOverride(databaseURL,
				servletContext);
		String name =  getInitParameterOrOverride(databaseName,
				servletContext);
		String options = getInitParameterOrOverride(databaseOptions,
				servletContext);


		if ("ssl".equals(options))
			options = "verifyServerCertificate=false"+
			"&useSSL=true"+
			"&requireSSL=true";
		if (name != null) {
			// allow database name in URL to be overridden
			int index = url.lastIndexOf('/');
			if (index >= 0)
				url = url.substring(0, index + 1)+  databaseName;
			else
				url = url + "/" + databaseName;
		}
		if (options != null && options.length() > 0)
			url = url + "?" + options;

		this.databaseURL = url;
		this.databaseUser =  getInitParameterOrOverride(databaseUser,
				servletContext);
		this.databasePassword =  getInitParameterOrOverride(databasePassword,
				servletContext);
	}
	
	public String getContextPath() {
		return contextPath;
	}

	/**
	 * Retrieve either an initParameter or its over-ridden value from the given
	 * servletContext.
	 * <p>
	 * Tomcat allows you to set initParameters in two different places
	 * (tomcat/conf/web.xml for the entire tomcat server, or in
	 * webapp/WEB-INF/web.xml of the web-app's warfile, which will be limited to
	 * only that web-app). You unfortunately <b>cannot</b> set initParameters in
	 * both files, or the server will fail when it tries to load. This is very,
	 * very annoying.
	 * <p>
	 * This method (a hack that fixes this limitation) first looks for an
	 * "override" parameter, which is an initParameter in webapp/WEB-INF/web.xml
	 * with the suffix "__override" that has the same name as an initParameter
	 * in tomcat/conf/web.xml. For example, tomcat/conf/web.xml may have
	 * "database.user" set to "root", while webapp/WEB-INF/web.xml will have
	 * "database.user__override" set to "normal_user" instead.
	 * <p>
	 * This method returns the override parameter, if any; otherwise if an
	 * override parameter is not available, then the method instead returns the
	 * regular initParameter.
	 *
	 * @param key
	 *            The key of the initParameter.
	 * @param servletContext
	 *            The servletContext.
	 * @return The value that the given initParameter key is bound to in the
	 *         given servletContext.
	 */
	protected static String getInitParameterOrOverride(String key, ServletContext servletContext) {
		String value = servletContext.getInitParameter(key + "__override");
		if (value != null)
			return value;
		return servletContext.getInitParameter(key);
	}


	public Connection getConnection() throws SQLException {
		if (databaseURL == null)
			throw new IllegalArgumentException("databaseServer not set");
		if (databaseUser == null)
			throw new IllegalArgumentException("databaseUser not set");
		if (databasePassword == null)
			throw new IllegalArgumentException("databasePassword not set");

		Connection conn;
		try {
			conn = DriverManager.getConnection(databaseURL, databaseUser,
					databasePassword);
		} catch (SQLException e) {
			throw new SQLException("Failed to connect to " + databaseURL, e);
		}
		return conn;
	}

	public void releaseConnection(Connection conn) throws SQLException {
		if (conn != null)
			conn.close();
	}

}
/**
 * Marmoset: a student project snapshot, submission, testing and code review
 * system developed by the Univ. of Maryland, College Park
 * 
 * Developed as part of Jaime Spacco's Ph.D. thesis work, continuing effort led
 * by William Pugh. See http://marmoset.cs.umd.edu/
 * 
 * Copyright 2005 - 2011, Univ. of Maryland
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

/*
 * Created on Jan 9, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.submitServer.GenericLDAPAuthenticationService;
import edu.umd.cs.submitServer.IAuthenticationService;
import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.SubmitServerDatabaseProperties;
import edu.umd.cs.submitServer.SubmitServerUtilities;
import edu.umd.cs.submitServer.policy.ChosenSubmissionPolicy;

/**
 * @author jspacco
 *
 *         Base class for all servlets in the SubmitServer. Provides methods for
 *         getting and releasing database connections as well as access to
 *         certain well-defined loggers.
 *
 *         XXX Note that static methods in a subclass of HttpServlet are not
 *         useful because most web containers will use a separate classloader
 *         for each servlet, so there's not as much sharing happening.
 */
public abstract class SubmitServerServlet extends HttpServlet implements
		SubmitServerConstants {
	// XXX Turn off BuildServerMonitor
	// private BuildServerMonitor buildServerMonitor;
	/**
	 * XXX We have to use the getInstance() method to make sure there is one
	 * shared buildServerMonitor because most webapp containers load each
	 * servlet with its own classloader, so marking the field static doesn't
	 * help. BuildServerMonitor is being placed into a separate jarfile and
	 * placed in tomcat/shared/lib so that it's loaded by a classloader that's
	 * shared by all the webapps (and therefore all servlets within a single
	 * webapp). For more details about where jarfiles/warfiles are being
	 * installed, see the "generic.install" target in build.xml. Also note that
	 * this works for tomcat but I don't know the conventions for other
	 * servlet-container providers.
	 *
	 * @return The BuildServerMonitor singleton.
	 */
	/*
	 * protected BuildServerMonitor getBuildServerMonitor() { if
	 * (buildServerMonitor==null) {
	 * buildServerMonitor=BuildServerMonitor.getInstance(); } return
	 * buildServerMonitor; }
	 */

	/**
	 * Logger for authentication information.
	 */
	private static Logger authenticationLog = Logger
			.getLogger(AUTHENTICATION_LOG);

	protected Logger getAuthenticationLog() {
		return authenticationLog;
	}

	/**
	 * Generic logger object for all servlets to use.
	 */
	private Logger submitServerServletLog;

	protected synchronized Logger getSubmitServerServletLog() {
		if (submitServerServletLog == null) {
			submitServerServletLog = Logger
					.getLogger(SubmitServerServlet.class);
			if ("true".equals(getServletContext().getInitParameter("DEBUG"))) {
				submitServerServletLog.setLevel(Level.DEBUG);
			}
		}
		return submitServerServletLog;
	}

	/**
	 * If true, all parameter parsers should throw an exception when a regex
	 * filter fails to match a parameter value. Otherwise we just output an
	 * error to the log and return a scrubbed version of the value.
	 *
	 * @return true if parsers should enforce strict paramter checking
	 */
	public boolean strictParameterChecking() {
		return strictParameterChecking;
	}

	private boolean strictParameterChecking = true;

	private SubmitServerDatabaseProperties submitServerDatabaseProperties;
	
	protected SubmitServerDatabaseProperties getDatabaseProps() {
		return submitServerDatabaseProperties;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		super.init();

		ServletContext servletContext = getServletContext();

			submitServerDatabaseProperties = new SubmitServerDatabaseProperties(
					servletContext);

		getSubmitServerServletLog().debug(
				"Initializing logger for " + getClass());

		strictParameterChecking = "true".equalsIgnoreCase(getServletContext()
				.getInitParameter("strict.parameter.checking"));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.GenericServlet#log(java.lang.String,
	 * java.lang.Throwable)
	 */
	@Override
	public void log(String msg, Throwable throwable) {
		getSubmitServerServletLog().info(msg, throwable);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.GenericServlet#log(java.lang.String)
	 */
	@Override
	public void log(String msg) {
		getSubmitServerServletLog().info(msg);
	}

	/**
	 * Gets a connection to the database.
	 *
	 * @return a connection to the database.
	 * @throws SQLException
	 */
	protected Connection getConnection() throws SQLException {
		return submitServerDatabaseProperties.getConnection();
	}

	/**
	 * Releases a database connection. Swallows (or handles) any SQLExceptions
	 * that happen since there's nothing the web application can do if a
	 * database connection cannot be closed.
	 *
	 * @param conn
	 *            the connection to release
	 */
	protected void releaseConnection(Connection conn) {
		try {
			submitServerDatabaseProperties.releaseConnection(conn);
		} catch (SQLException e) {
			getSubmitServerServletLog().warn("Unable to close connection", e);
		}
	}

	protected void handleSQLException(SQLException e) {
		// TODO Get rid of this method or make it throw a ServletException
		// This method could in theory be used for logging and checking out
		// SQLExceptions
		// but this method is *NOT* called everywhere where an SQLException is
		// thrown
		// so it would probably be better to get rid of this method altogether.
	}

	protected void rollbackIfUnsuccessfulAndAlwaysReleaseConnection(
			boolean transactionSuccess, HttpServletRequest req, Connection conn) {
		try {
			if (!transactionSuccess && conn != null) {
				// TODO Log a stack trace as well!
				String reqStr = req.getRequestURI();
				if (req.getQueryString() != null) {
					reqStr += "?" + req.getQueryString();
				}
				getSubmitServerServletLog().warn(
						"Unable to rollback connection: " + reqStr);
				conn.rollback();
			}
		} catch (SQLException ignore) {
			getSubmitServerServletLog().warn("Unable to rollback connection",
					ignore);
			// ignore
		}
		releaseConnection(conn);
	}

	private IAuthenticationService authenticationService;

	/**
	 * Gets the implementatino of IAuthenticationService used to authenticate
	 * people for to the submitServer. The idea is that other institutions can
	 * write their own implementations of IAuthenticationService to authenticate
	 * however they want.
	 * <p>
	 * The IAuthenticationService object is lazily initialized and so it's
	 * possible that some errors that might not be caught at init() time
	 *
	 * @return The concrete implementation of IAuthenticationService used by
	 *         this web application for authentication.
	 * @throws ServletException
	 */
	protected synchronized IAuthenticationService getIAuthenticationService()
			throws ServletException {
		// Return cached copy if we've already loaded it
		if (authenticationService != null)
			return authenticationService;
		// Otherwise we *MUST* be able to load the class used for authentication
		// or we're screwed.
		String authenticationServiceClassname = getServletContext()
				.getInitParameter(AUTHENTICATION_LDAP_SERVICE);
		if (authenticationServiceClassname == null)
		    authenticationServiceClassname = GenericLDAPAuthenticationService.class.getName();
		getSubmitServerServletLog()
				.debug("authenticationServiceClass: "
						+ authenticationServiceClassname);
		authenticationService = (IAuthenticationService) SubmitServerUtilities
				.createNewInstance(authenticationServiceClassname);
		authenticationService.initialize(getServletContext());
		return authenticationService;
	}

	private static Map<String, ChosenSubmissionPolicy> bestSubmissionPolicyMap = new HashMap<String, ChosenSubmissionPolicy>();

	/**
	 * Returns an instance of the BestSubmissionPolicy class with the given
	 * className. Returns an instance of DefaultBestSubmissionPolicy if
	 * className is null.
	 *
	 * @param className
	 *            The name of the BestSubmissionPolicy subclass to return.
	 * @return An instance of a subclass of BestSubmissionPolicy.
	 * @throws ServletException
	 *             If anything goes wrong the exception will be wrapped with a
	 *             ServletException, which will be thrown.
	 */
	public static ChosenSubmissionPolicy getBestSubmissionPolicy(String className)
			throws ServletException {
		if (bestSubmissionPolicyMap.containsKey(className))
			return bestSubmissionPolicyMap.get(className);
		ChosenSubmissionPolicy bestSubmissionPolicy = (ChosenSubmissionPolicy) SubmitServerUtilities
				.createNewInstance(className);
		bestSubmissionPolicyMap.put(className, bestSubmissionPolicy);
		return bestSubmissionPolicy;
	}


	public String getContextLink(HttpServletRequest request) {
		return request.getScheme() + "://"
	       + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();

	}
	public String getProjectLinkForStudent(Project project) {
		return "/view/project.jsp?projectPK="
		+ project.getProjectPK();

	}
	
	public static String stripNewlines(String s) {
        if (s == null)
            return s;
        return s.replaceAll("[\r\n]", " ");
    }
	public static void printProperty(PrintWriter out, String key, String value) {
        if (value == null || value.length() == 0) return;
        out.println(key+"=" + stripNewlines(value));
    }
	public static void printComment(PrintWriter out, String comment) {
        if (comment == null || comment.length() == 0) return;
        out.println("# " +  stripNewlines(comment));
    }
}

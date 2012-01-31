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
 * Created on Jan 11, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.filters;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.SubmitServerDatabaseProperties;
import edu.umd.cs.submitServer.SubmitServerUtilities;
import edu.umd.cs.submitServer.policy.ChosenSubmissionPolicy;

/**
 * @author jspacco
 * 
 *         Base class for all filters in the SubmitServer. Provides utility
 *         methods for getting and releasing database connections, and
 *         establishes a general logger for all filters.
 * 
 */
public abstract class SubmitServerFilter implements Filter, SubmitServerConstants {
    private Logger authenticationLog;

    protected Logger getAuthenticationLog() {
        if (authenticationLog == null) {
            authenticationLog = Logger.getLogger(AUTHENTICATION_LOG);
        }
        return authenticationLog;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // Not sure what needs to be done in here...
    }

    protected SubmitServerDatabaseProperties submitServerDatabaseProperties;

    protected ServletContext servletContext;
    private static Logger submitServerFilterLog = Logger.getLogger(SubmitServerFilter.class);

    static protected Logger getSubmitServerFilterLog() {
        return submitServerFilterLog;
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


    @OverridingMethodsMustInvokeSuper
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        servletContext = filterConfig.getServletContext();
        if ("true".equals(servletContext.getInitParameter("DEBUG"))) {
            submitServerFilterLog.setLevel(Level.DEBUG);
        }

        submitServerDatabaseProperties = new SubmitServerDatabaseProperties(servletContext);
        
        strictParameterChecking = "true".equalsIgnoreCase(servletContext.getInitParameter("strict.parameter.checking"));
    }

    /**
     * Gets a connection to the database.
     * 
     * @return a connection to the database.
     * @throws SQLException
     */
    protected Connection getConnection() throws SQLException {
        if (submitServerDatabaseProperties == null)
            throw new UnsupportedOperationException("No submitServerDatabaseProperties");
        
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
        if (submitServerDatabaseProperties == null)
            throw new UnsupportedOperationException("No submitServerDatabaseProperties");
       
        try {
            submitServerDatabaseProperties.releaseConnection(conn);
        } catch (SQLException e) {
            getSubmitServerFilterLog().warn(e.getMessage(), e);
        }
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
                getSubmitServerFilterLog().warn(
                        "Unable to rollback connection: " + reqStr);
                conn.rollback();
            }
        } catch (SQLException ignore) {
            getSubmitServerFilterLog().warn("Unable to rollback connection",
                    ignore);
            // ignore
        }
        releaseConnection(conn);
    }


    protected void handleSQLException(SQLException e) {
        // log SQLException
        getSubmitServerFilterLog().info(e.getMessage(), e);
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
    public static ChosenSubmissionPolicy getBestSubmissionPolicy(String className) throws ServletException {
        if (className == null)
            className = DEFAULT_BEST_SUBMISSION_POLICY;
        if (bestSubmissionPolicyMap.containsKey(className))
            return bestSubmissionPolicyMap.get(className);
        ChosenSubmissionPolicy bestSubmissionPolicy = (ChosenSubmissionPolicy) SubmitServerUtilities.createNewInstance(className);
        bestSubmissionPolicyMap.put(className, bestSubmissionPolicy);
        return bestSubmissionPolicy;
    }
}

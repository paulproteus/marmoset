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
 * Created on Jan 24, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.ServerError;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.UserSession;

/**
 * Catches the ServletExceptions and prints them to the response as text.
 * 
 * @author jspacco
 */
public class MonitorSlowTransactionsFilter extends SubmitServerFilter {
	int lowerBound = 7000;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
		String bound = filterConfig
				.getInitParameter("logTransactionsLongerThanThisManyMilliseconds");
		if (bound != null)
			lowerBound = Integer.parseInt(bound);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;

		long start = System.currentTimeMillis();
		try {
			chain.doFilter(req, resp);
		} finally {
			long end = System.currentTimeMillis();
			long duration = end - start;
			if (duration > lowerBound) {
				String url = request.getRequestURI();
				if (url.endsWith("buildServer/RequestSubmission"))
				  return;
				if (request.getQueryString() != null)
					url += "?" + request.getQueryString();
				HttpSession session = request.getSession(false);
				UserSession userSession = session == null ? null
						: (UserSession) session.getAttribute(USER_SESSION);
				if (userSession != null)
					url += " for student " + userSession.getStudentPK();
				long size = request.getContentLength();
				String msg;
				if (size > 0)
				    msg = "Slow: " + duration + " ms, size " + size  + ": " + url;
				else
				    msg = "Slow: " + duration + " ms: " + url;

				boolean isLong = false;
				String referer = request.getHeader("referer");
				if (referer != null) {
					// Ben: This is a hack to keep the logs from
					// getting too clogged up with irrelevant slow-
					// transaction warnings
					isLong = isLong
							|| referer.indexOf("RequestSubmission") != -1;
					isLong = isLong
							|| referer.indexOf("SubmitProjectViaEclipse") != -1;
					isLong = isLong
							|| referer.indexOf("ReportTestOutcomes") != -1;
					isLong = isLong || referer.indexOf("GetTestSetup") != -1;
					msg += " " + referer;
				}

				if (isLong) 
				  return;
				
				getSubmitServerFilterLog().info(msg);
				@Student.PK Integer userPK = userSession == null ? null : userSession
				    .getStudentPK();

				Course course = (Course) req.getAttribute(COURSE);
				Integer coursePK = null;
				if (course != null)
				  coursePK = course.getCoursePK();
				else if (userSession != null)
				  coursePK = userSession.getOnlyCoursePK();
				Student student = (Student) req.getAttribute(STUDENT);
				Project project = (Project) req.getAttribute(PROJECT);
				Submission submission = (Submission) req.getAttribute(SUBMISSION);
				Connection conn = null;
				try {
				  conn = getConnection();
				  insertServerError(conn, request, msg, userPK, coursePK, student, project, submission);
				} catch (SQLException e) {
				  getSubmitServerFilterLog().warn(e);
				}  finally {
				  releaseConnection(conn);
				}
				
			}
		}
	}


    public static void insertServerError(Connection conn, HttpServletRequest request, String msg,
            @Student.PK Integer userPK, Integer coursePK,
            Student student,  Project project, Submission submission) throws SQLException {
        String userAgent = request.getHeader("User-Agent");
        ServerError.insert(conn, ServerError.Kind.SLOW, 
                userPK, 
                student == null ? null : student.getStudentPK(),
                coursePK, 
                project == null ? null : project.getProjectPK(),
                        submission == null ? null : submission.getSubmissionPK(), null, msg, "", 
                        "Slow", 
                        request.getRequestURI(),
                        request.getQueryString(),
                        SubmitServerFilter.getRemoteHost(request), request.getHeader("referer"), userAgent, null);
    }
    public static void insertServerError(Connection conn, HttpServletRequest request,
           String msg, String type, String servlet) throws SQLException {
        String userAgent = request.getHeader("User-Agent");
        
        ServerError.insert(conn, ServerError.Kind.SLOW, null, null, null, null,
                        null, null, msg, type, 
                        servlet, 
                        request.getRequestURI(),
                        request.getQueryString(),
                        SubmitServerFilter.getRemoteHost(request), request.getHeader("referer"), userAgent, null);
    }
}

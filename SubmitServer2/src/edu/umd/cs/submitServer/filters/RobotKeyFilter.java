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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentSubmitStatus;
import edu.umd.cs.submitServer.SubmitServerConstants;

/**
 * @author jspacco
 * 
 */
public class RobotKeyFilter extends SubmitServerFilter {
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
		HttpServletResponse response = (HttpServletResponse) resp;

		Project project = (Project) request.getAttribute("project");
		Course course = (Course) request.getAttribute("course");
		StudentSubmitStatus status;
		try {
			status = StudentSubmitStatus
					.lookupByStudentRegistrationPKAndProjectPK(
							project.getCanonicalStudentRegistrationPK(),
							project.getProjectPK(), getConnection());
		} catch (SQLException e) {
			throw new ServletException(
					"Unable to find status of cannonical submission", e);
		}

		String robotKey = getRobotKey(course, project, status);
		String robotKeyParameter = request.getParameter("key");
		if (robotKeyParameter != null && robotKeyParameter.equals(robotKey))
		  request.setAttribute(SubmitServerConstants.INSTRUCTOR_CAPABILITY, true);
    
		request.setAttribute("robotKey", robotKey);

		chain.doFilter(req, resp);
	}

	public static String getRobotKey(Course course, Project project,
			StudentSubmitStatus status) throws ServletException {
		if (status == null) {
			return null;
		}
		String oneTimePassword = status.getOneTimePassword();
		String hashKey = course.toString() + "-" + project.getProjectNumber()
				+ "-" + oneTimePassword;
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("md5");

			byte hash[] = md5.digest(hashKey.getBytes("UTF-8"));
			BigInteger i = new BigInteger(1, hash);
			String key = i.toString(16);
			return key;
		} catch (NoSuchAlgorithmException e) {
			throw new ServletException(
					"Could not get message digest algorithm", e);
		} catch (UnsupportedEncodingException e) {
			throw new ServletException(
					"Could not find UTF-8 character encoding", e);
		}
	}
}

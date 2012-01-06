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
 * Created on Jun 7, 2005
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.utilities.XSSScrubber;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.RequestParser;

/**
 * @author jspacco
 * 
 */
public class PerformDemoLogin extends SubmitServerServlet {

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to
	 * post.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Connection conn = null;
		try {
			// Check that this server is configured for demo logins.
			String demoServer = getServletContext().getInitParameter(
					"demo.server");
			if (!"true".equals(demoServer))
				throw new ServletException(
						"Demo Server features (such as registering "
								+ " an email account) are disabled for this server");
			conn = getConnection();

			RequestParser parser = new RequestParser(request,
					getSubmitServerServletLog(), strictParameterChecking());
			String emailAddress = parser.getCheckedParameter("emailAddress");
			String password = parser.getPasswordParameter();

			Student student = Student.lookupByEmailAddress(emailAddress, conn);
			if (student == null) {
				request.setAttribute("canNotFindEmailAddress", Boolean.TRUE);
				request.setAttribute("emailAddress", emailAddress);
				request.getRequestDispatcher("/").forward(request, response);
				return;
			}
			if (!password.equals(student.getCampusUID())) {
				request.setAttribute("badPassword", Boolean.TRUE);
				request.setAttribute("emailAddress", emailAddress);
				request.getRequestDispatcher("/").forward(request, response);
				return;
			}

			// Set required user session information.
			HttpSession session = request.getSession();
			PerformLogin.setUserSession(session, student, conn);

			// check to see if user tried to view a page before logging in
			String target = parser.getOptionalCheckedParameter("target");

			if (target != null && !target.equals("")) {
				// This XSS scrub is redundant with the check that occurs in
				// getOptionalCheckedParameter, but Fortify complains unless
				// it happens here too
				response.sendRedirect(XSSScrubber.scrubbedStr(target));
				return;
			}

			// otherwise redirect to the main view page
			// System.out.println("otherwise redirect to the main view page");
			response.sendRedirect(request.getContextPath() + "/view/index.jsp");
		} catch (InvalidRequiredParameterException e) {
			request.setAttribute("otherError", Boolean.TRUE);
			request.getRequestDispatcher("/").forward(request, response);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

}

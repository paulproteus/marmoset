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
 * Created on Jul 6, 2005
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration.Capability;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.RequestParser;
import edu.umd.cs.submitServer.UserSession;

/**
 * @author jspacco
 *
 */
public class ChangePermission extends SubmitServerServlet {
	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
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
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		UserSession userSession = (UserSession) session
				.getAttribute(USER_SESSION);
		Connection conn = null;
		try {
			conn = getConnection();

			RequestParser parser = new RequestParser(request,
					getSubmitServerServletLog(), strictParameterChecking());
			// Using request parameter 'targetStudentRegistrationPK' rather than
			// 'studentRegistrationPK' because the ExtractParametersFilter
			// fetches
			// the studentRegistration record and then the
			// InstructorActionFilter
			// assumes that is who is making the request
			@StudentRegistration.PK int studentRegistrationPK = parser
					.getIntParameter("targetStudentRegistrationPK");
			String action = parser.getCheckedParameter("action");
			@Capability String permissionType = StudentRegistration.asCapability(parser
					.getCheckedParameter("permissionType"));

			if (!action.equals(StudentRegistration.ADD_PERMISSION)
					&& !action.equals(StudentRegistration.REMOVE_PERMISSION))
				throw new ServletException(
						"You must either 'add' or 'remove' a permission; '"
								+ action + "' is not a valid action");

			if ((!permissionType
					.equals(StudentRegistration.READ_ONLY_CAPABILITY))
					&& (!permissionType
							.equals(StudentRegistration.MODIFY_CAPABILITY))) {
				response.sendError(
						HttpServletResponse.SC_BAD_REQUEST,
						"The system only supports adding/removing read-only (TA) privileges and setting modify (Instructor) privileges to TAs");
				return;
			}

			StudentRegistration studentRegistration = StudentRegistration
					.lookupByStudentRegistrationPK(studentRegistrationPK, conn);

			// Only Super users can revoke instructor privileges
			if ((StudentRegistration.MODIFY_CAPABILITY
					.equals(studentRegistration.getInstructorCapability()))
					&& (!userSession.isSuperUser())) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"You can't change the permissions of instructors");
				return;
			}

			if (userSession.getStudentPK().equals(
					studentRegistration.getStudentPK())) {
				response.sendError(
						HttpServletResponse.SC_BAD_REQUEST,
						"You can't remove your own privileges because then you wouldn't be able to give yourself back the lost permission");
				return;
			}

			// TODO Handle adding/removing modify privilege as well.
			if (action.equals(StudentRegistration.ADD_PERMISSION))
				studentRegistration.setInstructorCapability(permissionType);
			else if (action.equals(StudentRegistration.REMOVE_PERMISSION))
				studentRegistration.setInstructorCapability("");

			// Update the record in the database.
			studentRegistration.update(conn);

			String target = request.getContextPath()
					+ "/view/instructor/course.jsp?coursePK="
					+ studentRegistration.getCoursePK();
			response.sendRedirect(target);

		} catch (InvalidRequiredParameterException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
	}
}

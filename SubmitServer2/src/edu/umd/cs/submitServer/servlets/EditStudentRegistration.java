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

package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.submitServer.ClientRequestException;
import edu.umd.cs.submitServer.RequestParser;
import edu.umd.cs.submitServer.StudentForUpload;

/**
 * EditStudentRegistration <br>
 * TODO Handle users who are not bound to particular courses (i.e. super-users,
 * research users)
 *
 * @author jspacco
 */
public class EditStudentRegistration extends SubmitServerServlet {

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
		boolean transactionSuccess = false;
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

			RequestParser parser = new RequestParser(request,
					getSubmitServerServletLog(), strictParameterChecking());
			StudentForUpload studentForUpload = new StudentForUpload(parser);

			@StudentRegistration.PK int studentRegistrationPK = parser
					.getIntParameter("studentRegistrationPK");
			StudentRegistration studentRegistration = StudentRegistration
					.lookupByStudentRegistrationPK(studentRegistrationPK, conn);
			if (studentRegistration == null)
				throw new SQLException(
						"Potential database corruption: cannot find studentRegistration with PK "
								+ studentRegistrationPK);
			Student student = studentRegistration.getCorrespondingStudent(conn);

			student.setLoginName(studentForUpload.loginName);
			student.setCampusUID(studentForUpload.campusUID);
			student.setFirstname(studentForUpload.firstname);
			student.setLastname(studentForUpload.lastname);

		
			student.update(conn);

			studentRegistration.setFirstname(studentForUpload.firstname);
			studentRegistration.setLastname(studentForUpload.lastname);
			studentRegistration.setClassAccount(studentForUpload.classAccount);
			studentRegistration.update(conn);
			conn.commit();
			transactionSuccess = true;

			String redirectURL = request.getContextPath()
					+ "/view/instructor/editStudentRegistration.jsp?studentRegistrationPK="
					+ studentRegistrationPK
					+ "&editStudentRegistrationMessage=Successful!";
			response.sendRedirect(redirectURL);
		} catch (ClientRequestException e) {
			response.sendError(e.getErrorCode(), e.getMessage());
			return;
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			rollbackIfUnsuccessfulAndAlwaysReleaseConnection(
					transactionSuccess, request, conn);
		}
	}

}

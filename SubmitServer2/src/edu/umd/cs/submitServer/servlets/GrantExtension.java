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
 * Created on Feb 8, 2005
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.StudentSubmitStatus;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.RequestParser;

/**
 * @author jspacco
 *
 */
public class GrantExtension extends SubmitServerServlet {


	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Connection conn = null;
		boolean transactionSuccess = false;
		try {
			conn = getConnection();
			// MultipartRequest multipartRequest =
			// (MultipartRequest)request.getAttribute(MULTIPART_REQUEST);
			RequestParser parser = new RequestParser(request,
					getSubmitServerServletLog(), strictParameterChecking());

			@StudentRegistration.PK int studentRegistrationPK = parser
					.getIntParameter("studentRegistrationPK");
			int projectPK = parser.getIntParameter("projectPK");
			int extension = parser.getIntParameter("extension");

			// low amount of transaction isolation required here since this
			// record is read/modified infrequently
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

			StudentSubmitStatus studentSubmitStatus
				= StudentSubmitStatus.createOrInsert(
						projectPK, studentRegistrationPK, conn);

			studentSubmitStatus.setExtension(extension);

			studentSubmitStatus.update(conn);
			conn.commit();
			transactionSuccess = true;

			// redirect to project page
			String url = request.getContextPath();
			url += "/view/instructor/project.jsp?projectPK=" + projectPK;
			response.sendRedirect(url);
			return;
		} catch (SQLException e) {
			handleSQLException(e);
			throw new ServletException(e);
		} catch (InvalidRequiredParameterException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					e.getMessage());
			return;
		} finally {
			rollbackIfUnsuccessfulAndAlwaysReleaseConnection(
					transactionSuccess, request, conn);
		}
	}

}

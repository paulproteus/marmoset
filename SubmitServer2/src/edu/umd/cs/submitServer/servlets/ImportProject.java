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

import org.apache.commons.fileupload.FileItem;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.submitServer.MultipartRequest;

public class ImportProject extends SubmitServerServlet {

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
		FileItem fileItem = null;
		boolean transactionSuccess = false;
		try {
			conn = getConnection();

			// MultipartRequestFilter is required
			MultipartRequest multipartRequest = (MultipartRequest) request
					.getAttribute(MULTIPART_REQUEST);
			Course course = (Course) request.getAttribute(COURSE);
			StudentRegistration canonicalStudentRegistration = StudentRegistration
					.lookupByStudentRegistrationPK(multipartRequest
							.getIntParameter("canonicalStudentRegistrationPK",
									0), conn);

			fileItem = multipartRequest.getFileItem();

			conn.setAutoCommit(false);
			/*
			 * 20090608: changed TRANSACTION_READ_COMMITTED to
			 * TRANSACTION_REPEATABLE_READ to make transaction compatible with
			 * innodb in MySQL 5.1, which defines READ_COMMITTED as unsafe for
			 * use with standard binary logging. For more information, see:
			 * <http
			 * ://dev.mysql.com/doc/refman/5.1/en/set-transaction.html#isolevel_read
			 * -committed>
			 */
			conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

			Project project = Project.importProject(fileItem.getInputStream(),
					course, canonicalStudentRegistration, conn);

			conn.commit();
			transactionSuccess = true;

			String redirectUrl = request.getContextPath()
					+ "/view/instructor/projectUtilities.jsp?projectPK="
					+ project.getProjectPK();
			response.sendRedirect(redirectUrl);

		} catch (ClassNotFoundException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			rollbackIfUnsuccessfulAndAlwaysReleaseConnection(
					transactionSuccess, request, conn);
		}
	}
}

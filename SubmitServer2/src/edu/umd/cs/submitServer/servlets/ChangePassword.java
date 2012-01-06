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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.submitServer.RequestParser;

public class ChangePassword extends SubmitServerServlet {

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
		RequestParser parser = new RequestParser(request,
				getSubmitServerServletLog(), strictParameterChecking());
		Connection conn = null;
		boolean transactionSuccess = false;
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

			Student student = (Student) request.getAttribute(STUDENT);
			String currentPassword = parser
					.getOptionalPasswordParameter("currentPassword");
			String newPassword = parser
					.getOptionalPasswordParameter("newPassword");
			String confirmPassword = parser
					.getOptionalPasswordParameter("confirmNewPassword");
			String message;
			String title;
			String link = "<a href=\"" + request.getContextPath()
					+ "/view/changePassword.jsp?studentPK="
					+ student.getStudentPK() + "\"> Try again! </a>";
			if (currentPassword == null
					|| !currentPassword.equals(student.getPassword())) {
				message = "The current password does not match the password for this account!<br> "
						+ "Your password has <b>NOT</b> been changed";
				title = "Password update not successful";
			} else if (newPassword == null
					|| !newPassword.equals(confirmPassword)) {
				message = "The two new passwords entered don't match!<br> "
						+ "Your password has <b>NOT</b> been changed";
				title = "Password update not successful";
			} else {
				student.setPassword(newPassword);
				student.update(conn);
				conn.commit();
				transactionSuccess = true;

				message = "Your password has been successfully updated!";
				title = "Password update successful!";
				link = "<a href=\"" + request.getContextPath()
						+ "/view/index.jsp\"> Back to main page </a>";
			}
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
			out.println("<HTML>");
			out.println("  <HEAD><TITLE>" + title + "</TITLE></HEAD>");
			out.println("  <BODY>");
			out.println(message);
			out.println("<p>");
			out.println(link);
			out.println("");
			out.println("  </BODY>");
			out.println("</HTML>");
			out.flush();
			out.close();
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			rollbackIfUnsuccessfulAndAlwaysReleaseConnection(
					transactionSuccess, request, conn);
		}
	}

}

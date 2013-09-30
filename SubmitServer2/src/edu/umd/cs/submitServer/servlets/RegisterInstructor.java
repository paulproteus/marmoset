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

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.utilities.XSSScrubber;
import edu.umd.cs.submitServer.ClientRequestException;
import edu.umd.cs.submitServer.RequestParser;
import edu.umd.cs.submitServer.StudentForUpload;

public class RegisterInstructor extends SubmitServerServlet {

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
			Integer coursePK = Course.asPK(parser.getIntegerParameter("coursePK", null));

			String result = "";

			// Add/update instructor account
			Student student = new Student();
			student.setLoginName(studentForUpload.loginName);
			student.setCampusUID(studentForUpload.campusUID);
			student.setFirstname(studentForUpload.firstname);
			student.setLastname(studentForUpload.lastname);
			student = student.insertOrUpdateCheckingLoginNameAndCampusUID(conn);

			StudentRegistration studentRegistration = StudentRegistration
					.lookupByCvsAccountAndCoursePK(studentForUpload.classAccount,
							coursePK, conn);
			if (studentRegistration == null) {
				studentRegistration = new StudentRegistration();
				studentRegistration.setFirstname(studentForUpload.firstname);
				studentRegistration.setLastname(studentForUpload.lastname);
				studentRegistration.setClassAccount(studentForUpload.classAccount);
				studentRegistration.setCoursePK(coursePK);
				studentRegistration.setStudentPK(student.getStudentPK());
				studentRegistration
						.setInstructorCapability(StudentRegistration.MODIFY_CAPABILITY);
				studentRegistration.insert(conn);
				result += "<br>Inserted new student registration for "
						+ studentForUpload.classAccount + " for coursePK "
						+ coursePK;
			} else {
				// User already exists, give them modify capability
				studentRegistration
						.setInstructorCapability(StudentRegistration.MODIFY_CAPABILITY);
				studentRegistration.update(conn);
				result += "<br>Set as Instructor student registration for "
						+ studentForUpload.classAccount + " for coursePK "
						+ coursePK;
			}

			conn.commit();
			transactionSuccess = true;

			String link = "<a href=\"" + request.getContextPath()
					+ "/view/instructor/course.jsp?coursePK=" + coursePK
					+ "\">Back to the main page for this course</a>";

			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<!DOCTYPE HTML>");
			out.println("<HTML>");
			out.println("  <HEAD><TITLE>Register an Instructor</TITLE></HEAD>");
			out.println("  <BODY>");
			out.println(XSSScrubber.scrubbedStr(result) + "<p>");

			// [NAT P003] Inform user of generated password
			
			out.println("<br>" + link);
			out.println("</body>");
			out.println("</html>");
			out.flush();
			out.close();

		} catch (ClientRequestException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			rollbackIfUnsuccessfulAndAlwaysReleaseConnection(
					transactionSuccess, request, conn);
		}
	}
}

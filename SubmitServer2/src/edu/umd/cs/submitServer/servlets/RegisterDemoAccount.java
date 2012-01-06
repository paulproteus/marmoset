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

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.utilities.JavaMail;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.RequestParser;

/**
 * @author jspacco
 *
 */
/**
 * @author jspacco
 *
 */
public class RegisterDemoAccount extends SubmitServerServlet {
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
			// Check that this server is configured for demo logins.
			String demoServer = getServletContext().getInitParameter(
					"demo.server");
			if (!"true".equals(demoServer))
				throw new ServletException(
						"Demo Server features (such as registering "
								+ " an email account) are disabled for this server");

			String adminEmail = getServletContext().getInitParameter(
					"admin.email");
			if (adminEmail == null)
				throw new ServletException(
						"Demo server is not configured correctly: missing admin email address");
			String adminSMTP = getServletContext().getInitParameter(
					"admin.smtp");
			if (adminSMTP == null)
				throw new ServletException(
						"Demo server is not configured correctly: missing admin smtp");

			conn = getConnection();

			RequestParser parser = new RequestParser(request,
					getSubmitServerServletLog(), strictParameterChecking());
			String emailAddress = parser.getCheckedParameter("emailAddress");
			int coursePK = parser.getIntParameter("coursePK");
			Course course = Course.getByCoursePK(coursePK, conn);

			// start a transaction, with a weak isolation
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

			// Look for a demo account with the given username and password
			// lookupByEmailAddress() will NOT find non-demo account
			Student student = Student.lookupByEmailAddress(emailAddress, conn);
			if (student != null) {
				// Is this student record already registered for the requested
				// course?
				if (StudentRegistration.lookupByStudentPKAndCoursePK(
						student.getStudentPK(), coursePK, conn) != null) {
					// forward back to the /demo.jsp welcome page, setting
					// request attributes
					// so that the /demo.jsp welcome page can format an
					// appropriate error message.
					request.setAttribute(
							"emailAddressAlreadyRegisteredForCourse",
							Boolean.TRUE);
					request.setAttribute("emailAddress", emailAddress);
					request.setAttribute("course", course);
					request.getRequestDispatcher("/")
							.forward(request, response);
					return;
				}
			} else {
				// TODO Make a separate page for creating a new user account
				String firstname = parser.getCheckedParameter("firstname");
				String lastname = parser.getCheckedParameter("lastname");
				student = Student.createNewDemoAccount(emailAddress, firstname,
						lastname, conn);
			}

			// at this point we know we need to create a new student
			// registration record
			StudentRegistration studentRegistration = new StudentRegistration();
			studentRegistration.setStudentPK(student.getStudentPK());
			studentRegistration.setFirstname(student.getFirstname());
			studentRegistration.setLastname(student.getLastname());
			studentRegistration.setCoursePK(coursePK);
			studentRegistration
					.setInstructorCapability(StudentRegistration.READ_ONLY_CAPABILITY);
			// XXX setting the cvs account to the email address (overloaded with
			// the campusUID)
			// I'm not sure what else makes sense to put here
			// TODO fix the bug where CVS accounts must be unique
			studentRegistration.setClassAccount(student.getFirstname()
					+ student.getLastname());
			studentRegistration.insert(conn);

			conn.commit();
			transactionSuccess = true;

			// TODO factor out these hard-coded constants
			String to = student.getLoginName();
			String from = adminEmail;
			String host = adminSMTP;
			String subject = "Password for " + emailAddress;
			String messageText = "Your password is " + student.getPassword();
			try {
				JavaMail.sendMessage(to, from, host, subject, messageText);
			} catch (MessagingException e) {
				// TODO Write new getEmailAddress() and getPassword() methods
				// that
				// return the overloaded loginName and campusUID fields?
				throw new ServletException("Unable to send an email to "
						+ student.getLoginName()
						+ "; PLEASE NOTE that your username is "
						+ student.getLoginName()
						+ " (same as your email address) and your password is "
						+ student.getPassword());
			}
			/*
			 * try { JavaMail.sendMessage(<YOUR EMAIL ADDRESS HERE>, from, host,
			 * student.getCampusUID()
			 * +" has just created a new demo account on marmoset-demo",
			 * student.getCampusUID()
			 * +" has just created a new demo account on marmoset-demo"); }
			 * catch (MessagingException e) { getSubmitServerServletLog().error(
			 * "Unable to send message about a new demo account to myself"); }
			 */

			request.setAttribute("passwordSent", Boolean.TRUE);
			request.setAttribute("emailAddress", emailAddress);
			request.setAttribute("course", course);
			request.getRequestDispatcher("/").forward(request, response);
		} catch (InvalidRequiredParameterException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			rollbackIfUnsuccessfulAndAlwaysReleaseConnection(
					transactionSuccess, request, conn);
		}
	}
}

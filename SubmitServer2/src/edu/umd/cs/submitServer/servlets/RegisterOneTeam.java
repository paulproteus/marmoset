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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.StudentSubmitStatus;
import edu.umd.cs.marmoset.utilities.XSSScrubber;
import edu.umd.cs.submitServer.ClientRequestException;
import edu.umd.cs.submitServer.RequestParser;
import edu.umd.cs.submitServer.StudentForUpload;

public class RegisterOneTeam extends SubmitServerServlet {


	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Connection conn = null;
		boolean transactionSuccess = false;
		if (true) 
		    throw new UnsupportedOperationException("team projects not supported");
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

			RequestParser parser = new RequestParser(request,
					getSubmitServerServletLog(), strictParameterChecking());
			StudentForUpload studentForUpload = new StudentForUpload(parser);
			Course course = (Course) request.getAttribute("course");

			// check for existing student
			Student student = Student.lookupByLoginName(
					studentForUpload.loginName, conn);
			if (student != null) {
				String redirectURL = request.getContextPath()
						+ "/view/instructor/registerOneTeam.jsp?coursePK="
						+ course.getCoursePK() + "&errorMessage=Error: "
						+ studentForUpload.loginName + " is already "
						+ "registered for a course";
				response.sendRedirect(XSSScrubber.scrubbedStr(redirectURL));
				return;
			}
			// assert student == null;

			int projectPK = parser.getIntParameter("projectPK");
			Project project = Project.lookupByProjectPK(projectPK, conn);

			// compute password - assume exactly 2 team members
			List<Integer> studentPKs = new ArrayList<Integer>();
			studentPKs.add(parser.getIntParameter("studentPK1"));
			studentPKs.add(parser.getIntParameter("studentPK2"));
			String password = Student.createTeamPassword(studentPKs
					.toArray(new String[2]));

			// register student with modified information
			student = new Student();
			student.setLoginName(studentForUpload.loginName);
			student.setCampusUID(studentForUpload.campusUID);
			student.setFirstname(studentForUpload.firstname);
			student.setLastname("Project " + project.getProjectNumber());
			student.setAccountType(Student.TEAM_ACCOUNT);
			// student.insert(conn);

			// add team registration
			StudentRegistration registration = new StudentRegistration();
			registration.setCoursePK(course.getCoursePK());
			registration.setClassAccount(studentForUpload.classAccount);
			registration.setStudentPK(student.getStudentPK()); // student PK was
																// set by
																// student.insert
			registration.setInstructorCapability(null);
			registration.setFirstname(student.getFirstname());
			registration.setLastname(student.getLastname());
			registration.insert(conn);

			// create output buffer
			StringBuffer result = new StringBuffer();
			result.append("<p>Successful Team Creation! </p>");
			result.append("<p><b>Course:</b> " + course.getCourseName()
					+ "</p>");
			result.append("<p><b>Team ID:</b> " + student.getLoginName()
					+ "</p>");
			result.append("<p><b>Team Name:</b> " + student.getFirstname()
					+ "</p>");
			result.append("<p><b>Class Account:</b> "
					+ registration.getClassAccount() + "</p>");
			result.append("<p><b>Project:</b> " + project.getProjectNumber()
					+ "</p>");
			result.append("<p><b>Team Members:</b>  ---  ");

			// Get student registrations
			List<Integer> studentRegistrationPKs = new ArrayList<Integer>();
			for (@Student.PK Integer studentPK : studentPKs) {
				StudentRegistration s = StudentRegistration
						.lookupByStudentPKAndCoursePK(Student.asPK(studentPK),
								course.getCoursePK(), conn);
				result.append(s.getFirstname() + " " + s.getLastname()
						+ "  ---  ");
				studentRegistrationPKs.add(s.getStudentRegistrationPK());
			}
			result.append("</p>");
			result.append("<p><b>Team Password:</b> Space seperated list of team members' "
					+ "passwords in the order given above.</p>");

			// prevent students in team from release testing
			StudentSubmitStatus.banStudentsFromProject(conn, projectPK,
					studentRegistrationPKs);

			// prevent team from release testing on other projects
			StudentSubmitStatus.banStudentFromExistingProjects(conn,
					course.getCoursePK(),
					registration.getStudentRegistrationPK(), projectPK);

			// commit insert/update transactions
			conn.commit();
			transactionSuccess = true;

			String link = "<a href=\"" + request.getContextPath()
					+ "/view/instructor/course.jsp?coursePK="
					+ course.getCoursePK()
					+ "\">Back to the main page for this course</a>";

			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
			out.println("<HTML>");
			out.println("  <HEAD><TITLE>Team Registration Successful</TITLE></HEAD>");
			out.println("  <BODY>");
			out.println("  <div style=\"color:#CC0000; font-family:arial, helvetica, sans-serif;\">");
			out.println(result.toString());
			out.println("  </div>");

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

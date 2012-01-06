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
 * Created on Jan 20, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.IO;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.UserSession;

/**
 * @author jspacco
 * 
 */
public class DownloadSubmission extends SubmitServerServlet {

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

		Submission submission = (Submission) request.getAttribute("submission");
		Project project = (Project) request.getAttribute("project");
		StudentRegistration studentRegistration = (StudentRegistration) request
				.getAttribute("studentRegistration");

		boolean instructorViewOfStudent = ((Boolean) request
				.getAttribute("instructorViewOfStudent")).booleanValue();
		// SimpleDateFormat RFC822DATEFORMAT = new
		// SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'ZZZZ",
		// Locale.US);
		boolean instructorCapability = userSession
				.hasInstructorCapability(project.getCoursePK());
		if (!instructorCapability
				&& studentRegistration.getStudentPK() != userSession
						.getStudentPK()) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
					"You don't have permission to do that");
			return;
		}

		Connection conn = null;

		try {
			conn = getConnection();

			byte[] bytes = submission.downloadArchive(conn);
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

			// Inform client of content type and length
			response.setContentLength(bytes.length);
			response.setContentType("application/zip");

			String filename;
			if (instructorViewOfStudent)
				filename = studentRegistration.getClassAccount() + "-"
						+ project.getProjectNumber() + "-"
						+ submission.getSubmissionPK() + ".zip";
			else
				filename = project.getProjectNumber() + ".zip";

			Util.setAttachmentHeaders(response, filename);
			;

			OutputStream out = response.getOutputStream();
			IO.copyStream(bais, out);
			out.flush();
			out.close();
			bais.close();
		} catch (SQLException e) {
			handleSQLException(e);
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
	}

}

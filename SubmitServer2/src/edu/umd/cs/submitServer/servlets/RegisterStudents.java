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
 * Created on Jan 21, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.submitServer.MultipartRequest;
import edu.umd.cs.submitServer.StudentForUpload;

/**
 * @author jspacco
 * 
 */
public class RegisterStudents extends SubmitServerServlet {

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Connection conn = null;
		BufferedReader reader = null;
		FileItem fileItem = null;
		try {
			conn = getConnection();

			// MultipartRequestFilter is required
			MultipartRequest multipartRequest = (MultipartRequest) request
					.getAttribute(MULTIPART_REQUEST);

			Course course = (Course) request.getAttribute("course");
	
			// open the uploaded file
			fileItem = multipartRequest.getFileItem();
			reader = new BufferedReader(new InputStreamReader(
					fileItem.getInputStream()));

			// [NAT P003] Init storage for display of generated passwords and
			// output
			StringBuilder result = new StringBuilder();
			StringBuilder passwordList = new StringBuilder(
					"Some passwords were "
							+ "automatically generated. Please save this information:\n");
			boolean printPasswords = false;
			// [end NAT P003]

			int lineNumber = 1;

			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;

				// hard-coded skip of first two lines for Maryland-specific
				// format
				if (line.startsWith("Last,First,UID,section,ClassAcct,DirectoryID"))
					continue;
				if (line.startsWith(",,,,,"))
					continue;

				lineNumber++;

				// strip out comments starting with #
				line = line.split("#")[0];

				// skip blank lines
				if (line.trim().equals("")) // [NAT] more robust, trim off
											// spaces
					continue;

				try {
					StudentForUpload s = new StudentForUpload(line, delimiter
							);

					result.append("Registered " + s.lastname + "\n");


				} catch (IllegalStateException e) {
					result.append("[ERROR] " + e.getMessage() + "\n");
					// if (false) e.printStackTrace(out);
					// [NAT] We could also use the debug in Web.xml
					// if
					// ("true".equals(getServletContext().getInitParameter("DEBUG")))
					// e.printStackTrace(out);
				} catch (Exception e1) {
					result.append("[ERROR] Problem processing line: '" + line
							+ "' at line number: " + lineNumber + "\n");
					// if (false) e1.printStackTrace(out);
				}
			}

			String link = "<a href=\"" + request.getContextPath()
					+ "/view/instructor/course.jsp?coursePK="
					+ course.getCoursePK()
					+ "\">Back to the main page for this course</a>";

			// prepare a response
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
			out.println("<HTML>");
			out.println("  <HEAD><TITLE>Register a Student</TITLE></HEAD>");
			out.println("  <BODY>");
			out.println("<pre>");
			out.println(result.toString());
			out.println("</pre>");
			out.println("<p>");

			// [NAT P003] Inform user of generated passwords
			if (printPasswords) {
				out.println("<pre>");
				out.println(passwordList.toString());
				out.println("</pre>");
			}
			// [end NAT P003]

			out.println("<br>" + link);
			out.println("</body>");
			out.println("</html>");
			out.flush();
			out.close();

		} catch (SQLException e) {
			handleSQLException(e);
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
			if (reader != null)
				reader.close();
			if (fileItem != null)
				fileItem.delete();
		}
	}

	/**
	 * The delimiter used in the uploaded files of students for registration.
	 */
	String delimiter = ",";

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cs.submitServer.servlets.SubmitServerServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		super.init();
		String delimiter = getServletContext().getInitParameter(
				"register.students.delimiter");
		if (delimiter != null)
			this.delimiter = delimiter;
	}

}

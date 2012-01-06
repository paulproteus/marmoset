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
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.LogEntry;
import edu.umd.cs.submitServer.RequestParser;

public class CourseRSS extends SubmitServerServlet {

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
		RequestParser parser = new RequestParser(request,
				getSubmitServerServletLog(), strictParameterChecking());
		Course course = (Course) request.getAttribute(COURSE);
		LogEntry.Priority priority = parser.getOptionalEnumParameter("priority", LogEntry.Priority.class, LogEntry.Priority.MEDIUM);
		ServletOutputStream out = response.getOutputStream();
		response.setContentType("application/rss+xml");

		String link = request.getRequestURL().toString();

		Connection conn = null;
		try {
			conn = getConnection();

			Collection<LogEntry> logEntries
			 = LogEntry.studentLogEntriesForCourse(course, priority, conn);

			LogEntry.write(out, logEntries,  course.getCourseName(), link, "Student RSS feed for " + course.getCourseName(),
					request.getContextPath());
			LogEntry.write(System.out, logEntries,  course.getCourseName(), link, "Student RSS feed for " + course.getCourseName(),
					request.getContextPath());
			System.out.println("Done");

		} catch (SQLException e) {
			throw new ServletException(e);
		} catch (XMLStreamException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
		out.close();

	}

}

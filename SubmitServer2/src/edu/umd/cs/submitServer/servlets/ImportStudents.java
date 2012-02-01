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
import edu.umd.cs.submitServer.WebConfigProperties;

@Deprecated
public class ImportStudents extends GradeServerInterfaceServlet {
	private static final WebConfigProperties webProperties = WebConfigProperties.get();


	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		Connection gradesConn = null;
		Connection conn = null;
		boolean transactionSuccess = false;
		String term = request.getParameter("term");
		if (term == null)
			term = webProperties.getProperty("semester");
		Course course = (Course) request.getAttribute(COURSE);
		String [] courseIDs = request.getParameterValues("courseID");
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		try {
			gradesConn = getGradesConnection();
			conn = getConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

			for(String c : courseIDs) {
				int courseID = Integer.parseInt(c);
				ImportCourse.importStudents(out, term, course, courseID, false, gradesConn, conn);
			}

			transactionSuccess = true;
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			rollbackIfUnsuccessfulAndAlwaysReleaseConnection(
					transactionSuccess, request, conn);
			releaseGradesConnection(gradesConn);
		}

	}




}

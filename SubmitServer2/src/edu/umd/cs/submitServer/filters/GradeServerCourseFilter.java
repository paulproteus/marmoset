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

package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.submitServer.WebConfigProperties;

public class GradeServerCourseFilter extends GradeServerInterfaceFilter {
	private static final WebConfigProperties webProperties = WebConfigProperties.get();

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		Connection gradesConn = null;
		try {
			gradesConn = getGradesConnection();

			Student user = (Student) request.getAttribute(USER);
			String term = request.getParameter("semester");
			if (term == null)
				term = webProperties.getProperty("semester");

			request.setAttribute("semester", term);
			String query  = "SELECT DISTINCT course, gradesSection, courseID"
				+ " FROM submitexportstaff "
				+ " WHERE term = ?"
				+ " AND ( role = ? OR role = ?)"
						+ " AND uid = ?";
			PreparedStatement stmt = gradesConn.prepareStatement(query);
			stmt.setString(1, term);
			stmt.setString(2, "Instructor");
			stmt.setString(3, "View Only");
			stmt.setString(4, user.getCampusUID());
			ResultSet rs = stmt.executeQuery();
			ArrayList<String[]> result = new ArrayList<String[]>();
			while(rs.next()) {
				String course = rs.getString(1);
				String section = rs.getString(2);
				String courseID = rs.getString(3);
				String combined = course;
				if (section != null && section.length() > 0)
					combined += " " + section;
				String[] v = new String[] { course, combined, courseID };
				result.add(v);
			}
			request.setAttribute("gradeCourses", result);
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseGradesConnection(gradesConn);
		}

		chain.doFilter(req, resp);
	}

}

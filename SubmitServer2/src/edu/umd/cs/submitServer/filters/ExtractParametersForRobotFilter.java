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
 * Created on Jan 19, 2005
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.submitServer.RequestParser;

/**
 * Requires a projectPK and optionally a studentRegistrationPK.
 * 
 * This filter stores studentRegistration, student, course, project,
 * studentSubmitStatus and submissionList attributes in the request.
 */
public class ExtractParametersForRobotFilter extends SubmitServerFilter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		RequestParser parser = new RequestParser(request,
				getSubmitServerFilterLog(), strictParameterChecking());
		Integer projectPK = parser.getIntegerParameter("projectPK", null);

		Connection conn = null;
		try {
			conn = getConnection();

			Project project = Project.getByProjectPK(projectPK, conn);
			request.setAttribute(PROJECT, project);

			Integer coursePK = project.getCoursePK();

			Course course = Course.lookupByCoursePK(coursePK, conn);
			request.setAttribute(COURSE, course);

			List<StudentRegistration> studentRegistrationCollection = StudentRegistration
					.lookupAllWithAtLeastOneSubmissionByProjectPK(projectPK,
							conn);

			TreeSet<StudentRegistration> studentRegistrationSet = new TreeSet<StudentRegistration>(
					StudentRegistration.classAccountComparator);
			studentRegistrationSet.addAll(studentRegistrationCollection);

			request.setAttribute(STUDENT_REGISTRATION_SET,
					studentRegistrationSet);
      request.setAttribute("allStudents", studentRegistrationSet);

		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}

		chain.doFilter(request, response);
	}

}

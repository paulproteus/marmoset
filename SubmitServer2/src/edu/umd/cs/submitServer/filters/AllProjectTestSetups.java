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
import java.util.Collection;
import java.util.TreeMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestSetup;

/**
 * Requires a projectPK.
 * 
 * Stores a list of the test-setups into the request. Also stores a list of the
 * canonical submissions into the request.
 */
public class AllProjectTestSetups extends SubmitServerFilter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		Project project = (Project) request.getAttribute(PROJECT);
		Course course = (Course) request.getAttribute(COURSE);
		StudentRegistration canonicalAccount = (StudentRegistration)
				request.getAttribute("canonicalAccount");

		Connection conn = null;
		try {
			conn = getConnection();
			Collection<TestSetup> allTestSetups = TestSetup
					.lookupAllByProjectPK(project.getProjectPK(), conn);
			request.setAttribute("allTestSetups", allTestSetups);

			Collection<Submission> canonicalSubmissions = Submission
					.lookupAllByStudentRegistrationPKAndProjectPK(
							project.getCanonicalStudentRegistrationPK(),
							project.getProjectPK(), conn);
			request.setAttribute("canonicalSubmissions", canonicalSubmissions);
			
			TreeMap<Submission,StudentRegistration> baselineSubmissionCandidates
			= new TreeMap<Submission,StudentRegistration>();

			addSubmissions(baselineSubmissionCandidates, canonicalSubmissions, canonicalAccount);
			for(StudentRegistration sr : 
				StudentRegistration.lookupAllByCourseAndCapability(course, 
						StudentRegistration.PSEUDO_STUDENT_CAPABILITY, conn)) {
				addSubmissions(baselineSubmissionCandidates, 
						Submission.lookupAllByStudentRegistrationPKAndProjectPK(sr.getStudentRegistrationPK(), 
								project.getProjectPK(), conn), sr);
			}
			request.setAttribute("candidateBaselines", baselineSubmissionCandidates.entrySet());
			
			

		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
		chain.doFilter(request, response);
	}

	private void addSubmissions(
			TreeMap<Submission, StudentRegistration> baselineSubmissionCandidates,
			Collection<Submission> canonicalSubmissions,
			StudentRegistration sr) {
		for(Submission s : canonicalSubmissions) 
			switch(s.getBuildStatus()) {
			case COMPLETE:
			case NEW:
			case ACCEPTED:
				baselineSubmissionCandidates.put(s,sr);
			}
		}

}

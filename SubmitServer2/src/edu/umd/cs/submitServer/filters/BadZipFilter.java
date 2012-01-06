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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Archive;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.utilities.FixZip;

/**
 * Requires a projectPK.
 * 
 * Stores a list of the test-setups into the request. Also stores a list of the
 * canonical submissions into the request.
 */
public class BadZipFilter extends SubmitServerFilter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {

		System.out.println("Running bad zip filter");
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		Collection<Submission> badSubmissions = new TreeSet<Submission>();
		Map<Submission,Project> badSubmissionProject = new HashMap<Submission,Project>();
		Map<Submission,StudentRegistration> badSubmissionStudentRegistration 
		= new HashMap<Submission,StudentRegistration>();
		
		Connection conn = null;
		try {
			conn = getConnection();
			for(Archive a : Archive.getAll(Submission.SUBMISSION_ARCHIVES, conn)) {
				if (FixZip.hasProblem(a.getArchive())) {
					System.out.printf("%d is bad%n", a.getArchivePK());
						badSubmissions.addAll(Submission.lookupAllByArchivePK(a.getArchivePK(), conn));
				} else {
					System.out.printf("%d is good%n", a.getArchivePK());
				}
			}
			
			System.out.printf("%d bad submissions%n", badSubmissions.size());
			for(Submission s : badSubmissions) {
				Project p = Project.getByProjectPK(s.getProjectPK(), conn);
				StudentRegistration sr = StudentRegistration.lookupByStudentRegistrationPK(
						s.getStudentRegistrationPK(), conn);
				badSubmissionProject.put(s, p);
				badSubmissionStudentRegistration.put(s, sr);
				
				
			}
			request.setAttribute("badSubmissions", badSubmissions);
			request.setAttribute("badSubmissionsProject", badSubmissionProject);
			request.setAttribute("badSubmissionStudentRegistration", badSubmissionStudentRegistration);
			
			
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
		chain.doFilter(request, response);
	}

}

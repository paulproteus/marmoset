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
 * Created on Apr 8, 2005
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.CodeReviewSummary;
import edu.umd.cs.marmoset.modelClasses.CodeReviewer;
import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.UserSession;

/**
 * @author jspacco
 *
 */
public class MyCodeReviewsFilter extends SubmitServerFilter {


	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		HttpSession session = request.getSession();
		UserSession userSession = (UserSession) session
				.getAttribute(USER_SESSION);
		Project project = (Project) request.getAttribute(PROJECT);
		Course course = (Course) request.getAttribute(COURSE);
        Submission submission = (Submission) request.getAttribute(SUBMISSION);
        boolean isInstructor = (Boolean) request.getAttribute(
                SubmitServerConstants.INSTRUCTOR_CAPABILITY);
		List<Project> myProjects = (List<Project>) request.getAttribute(PROJECT_LIST);
		HashSet<Integer> myProjectPks = null;
		if (myProjects != null) {
			myProjectPks = new HashSet<Integer>();
			for(Project p : myProjects) {
				myProjectPks.add(p.getProjectPK());
			}
		}
			
		Connection conn = null;
		try {
			conn = getConnection();
			
			if (isInstructor) {
			  Map<Submission, Timestamp> requestsForHelp = Submission.lookupAllActiveHelpRequests(course.getCoursePK(), conn);
			  request.setAttribute("requestsForHelp", requestsForHelp.keySet());
			  request.setAttribute("requestsForHelpTimestamp", requestsForHelp);
		     }
			Collection<CodeReviewer> myReviews;
			if (submission != null)
			  myReviews = Collections.singleton(CodeReviewer.lookupBySubmissionAndStudentPK(submission.getSubmissionPK(), 
			      userSession.getStudentPK(), conn));
			      else
			        myReviews = CodeReviewer.lookupByStudentPK(userSession.getStudentPK(), conn);
			
			Collection<CodeReviewSummary> reviewsOfMyCode = new TreeSet<CodeReviewSummary>();
			Collection<CodeReviewSummary> myAssignments = new TreeSet<CodeReviewSummary>();
			Collection<CodeReviewSummary> adHocReviews = new TreeSet<CodeReviewSummary>();


			boolean allCodeReviews = request.getServletPath().endsWith("codeReviews.jsp");
			boolean anyCodeReviews = false;
			for(CodeReviewer r : myReviews) {
				CodeReviewSummary s =  new CodeReviewSummary(conn, r);
				if (project != null && !project.equals(s.getProject()))
					continue;
				if (myProjectPks != null && !myProjectPks.contains(s.getProject().getProjectPK()))
					continue;
				if (submission != null && !submission.equals(s.getSubmission())) {
					continue;
				}
				boolean show = s.isTimely() || allCodeReviews;
				if (project != null && s.getAssignment() != null)
				    show = true;
				if (!show)
				    continue;

				anyCodeReviews = true;
				CodeReviewer author = s.getAuthor();
				if (author != null && userSession.getStudentPK() == author.getStudentPK())
					reviewsOfMyCode.add(s);
				else if (r.isAssignment())
					myAssignments.add(s);
				else
					adHocReviews.add(s);
			}
			request.setAttribute("reviewsOfMyCode", reviewsOfMyCode);
			request.setAttribute("myAssignments", myAssignments);
			request.setAttribute("adHocReviews", adHocReviews);
			request.setAttribute("anyCodeReviews", anyCodeReviews);


		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
		chain.doFilter(request, response);
	}


}

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
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Queries;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.StudentSubmitStatus;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.RequestParser;
import edu.umd.cs.submitServer.policy.ChooseLastSubmissionPolicy;
import edu.umd.cs.submitServer.policy.ChosenSubmissionPolicy;

/**
 * @author jspacco
 *
 *         Expects a studentRegistrationSet to be set as a request attribute.
 */
public class AllStudentsSummaryFilter extends SubmitServerFilter {
	/*
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		Connection conn = null;
		try {
			conn = getConnection();

			Project project = (Project) request.getAttribute(PROJECT);

			Set<StudentRegistration> registrationSet = (Set<StudentRegistration>) request
					.getAttribute(STUDENT_REGISTRATION_SET);
			
			
			@CheckForNull StudentRegistration instructor = (StudentRegistration) request.getAttribute(STUDENT_REGISTRATION);
			if (!instructorCapability(request))
                throw new ServletException("Not instructor ");
          
	        
			boolean useDefault = false;
			RequestParser parser = new RequestParser(request,
					getSubmitServerFilterLog(), strictParameterChecking());
			useDefault = parser.getBooleanParameter("useDefault", false);
			String section = (String) request.getAttribute(SECTION);
			
			if (section != null) {
			    for(Iterator<StudentRegistration> i = registrationSet.iterator(); i.hasNext(); ) {
			        StudentRegistration sr = i.next();
			        if (!section.equals(sr.getSection()))
			            i.remove();
			    }
			}
			ChosenSubmissionPolicy bestSubmissionPolicy = null;

			if (!project.isTested())
				bestSubmissionPolicy = new ChooseLastSubmissionPolicy();
			else if (useDefault)
				bestSubmissionPolicy = getBestSubmissionPolicy(DEFAULT_BEST_SUBMISSION_POLICY);
			else
				bestSubmissionPolicy = getBestSubmissionPolicy(project
						.getBestSubmissionPolicy()); // null should return the
														// DefaultBestSubmissionPolicy.

			Map<Integer, Submission> lastSubmission = bestSubmissionPolicy
					.lookupLastSubmissionMap(project, conn);
			Map<Integer, Submission> lastOnTime = bestSubmissionPolicy
					.lookupChosenOntimeSubmissionMap(project, conn);
			Map<Integer, Submission> lastLate = bestSubmissionPolicy
					.lookupChosenLateSubmissionMap(project, conn);
			Map<Integer, Submission> lastVeryLate = bestSubmissionPolicy
					.lookupChosenVeryLateSubmissionMap(project, conn);
			
			Collection<Submission> submissionsUnderReview 
			= (Collection<Submission>) request.getAttribute("submissionsUnderReview");

			Map<Integer, Submission> submissionsThatNeedReview 
			= new HashMap<Integer, Submission>(lastSubmission);

			if (submissionsUnderReview != null)
			    for(Submission s : submissionsUnderReview) {
			        @StudentRegistration.PK int author = s.getStudentRegistrationPK();
			        Submission reviewed = submissionsThatNeedReview.get(author);
			        if (s.equals(reviewed))
			            submissionsThatNeedReview.remove(author);
			    }

			if (section != null) {
			    Set<Integer> registrationPKSet = new HashSet<Integer>();
			    for(StudentRegistration sr : registrationSet)
			        registrationPKSet.add(sr.getStudentRegistrationPK());
			    lastSubmission.keySet().retainAll(registrationPKSet);
			    lastOnTime.keySet().retainAll(registrationPKSet);
			    lastLate.keySet().retainAll(registrationPKSet);
			    lastVeryLate.keySet().retainAll(registrationPKSet);
			    submissionsThatNeedReview.keySet().retainAll(registrationPKSet);
			}

			Set<Integer> submissionsWithOutdatedTestResults
			= Queries.lookupSubmissionsWithOutdatedTestResults(conn, project);
			request.setAttribute("submissionsWithOutdatedTestResults", 
			        submissionsWithOutdatedTestResults);
			request.setAttribute("lastSubmission", lastSubmission);
			request.setAttribute("submissionsThatNeedReview", submissionsThatNeedReview);
			request.setAttribute("lastOnTime", lastOnTime);
			request.setAttribute(LAST_LATE, lastLate);
			request.setAttribute("lastVeryLate", lastVeryLate);
			
			Map<Integer, Submission> bestSubmissionMap = bestSubmissionPolicy
					.getChosenSubmissionMap(registrationSet, lastOnTime, lastLate);
			request.setAttribute("bestSubmissionMap", bestSubmissionMap);

			String sortKey = parser.getOptionalCheckedParameter("sortKey");

			if ("time".equals(sortKey)) {
				TreeSet<StudentRegistration> sortedByTime = new TreeSet<StudentRegistration>(
						StudentRegistration
								.getSubmissionViaTimestampComparator(lastSubmission));
				sortedByTime.addAll(registrationSet);
				request.setAttribute("studentRegistrationSet", sortedByTime);
			} else if ("score".equals(sortKey)) {
				TreeSet<StudentRegistration> sortedByScore = new TreeSet<StudentRegistration>(
						StudentRegistration
								.getSubmissionViaMappedValuesComparator(lastSubmission));
				sortedByScore.addAll(registrationSet);
				request.setAttribute("studentRegistrationSet", sortedByScore);
			}

			TreeSet<StudentRegistration> justStudentSubmissions = new TreeSet<StudentRegistration>();
			TreeSet<StudentRegistration> staffStudentSubmissions = new TreeSet<StudentRegistration>();
			boolean hasOtherStaffSubmissions = false;
            for(StudentRegistration sr : registrationSet)
			    if (sr.isNormalStudent()) {
			        if (section == null || section.equals(sr.getSection())
			                || section.equals("none") && sr.getSection() == null)
			                justStudentSubmissions.add(sr);
			    }
            else {
                staffStudentSubmissions.add(sr);
                if (!sr.equals(instructor))
                    hasOtherStaffSubmissions = true;
            }
			        
          
			 request.setAttribute("justStudentSubmissions", justStudentSubmissions);   
			 request.setAttribute("staffStudentSubmissions", staffStudentSubmissions);   
			 request.setAttribute("hasOtherStaffSubmissions", hasOtherStaffSubmissions);   
             Map<Integer, StudentSubmitStatus> submitStatusMap = StudentSubmitStatus.lookupAllByProjectPK(project.getProjectPK(), conn);
             request.setAttribute("submitStatusMap", submitStatusMap);
             
			if (project.isPair()) {
				Map<StudentRegistration,StudentRegistration> partner
				= new HashMap<StudentRegistration,StudentRegistration>();
				Map<Integer, StudentRegistration> studentRegistrationMap =
					new HashMap<Integer, StudentRegistration>();
				Collection<StudentRegistration>  allStudents =
					(Collection<StudentRegistration>) request.getAttribute("allStudents");
				for(StudentRegistration s : allStudents) {
					studentRegistrationMap.put(s.getStudentRegistrationPK(), s);
				}
				TreeSet<StudentRegistration>  studentsWithoutSubmissions =
				(TreeSet<StudentRegistration>) request.getAttribute("studentsWithoutSubmissions");

				for(StudentSubmitStatus status : submitStatusMap.values()) {
					Integer partnerPK = status.getPartnerPK();
					if (partnerPK != null && partnerPK > 0) {
						StudentRegistration from = studentRegistrationMap.get(status.getStudentRegistrationPK());
						StudentRegistration to = studentRegistrationMap.get(partnerPK);
						if (from == null || to == null)
							continue;
						partner.put(from, to);
						request.setAttribute("partnerMap", partner);
						if (!studentsWithoutSubmissions.contains(from))
							studentsWithoutSubmissions.remove(to);

					}

				}
				request.setAttribute("partnerMap", partner);
				request.setAttribute("studentsWithoutSubmissions", studentsWithoutSubmissions);

			}
		} catch (SQLException e) {
			handleSQLException(e);
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
		chain.doFilter(request, response);
	}
}

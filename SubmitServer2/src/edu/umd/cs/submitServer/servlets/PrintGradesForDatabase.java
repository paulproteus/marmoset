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
 * Created on Feb 12, 2005
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.StudentSubmitStatus;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

/**
 * @author jspacco
 * 
 */
public class PrintGradesForDatabase extends SubmitServerServlet {


	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Connection conn = null;
		try {
			conn = getConnection();

			// get the project and all the student registrations
			Map<Integer, Submission> bestSubmissionMap = (Map<Integer, Submission>) request
					.getAttribute("bestSubmissionMap");

			Map<Integer, StudentSubmitStatus> submitStatusMap 
			= (Map<Integer, StudentSubmitStatus>) request.getAttribute("submitStatusMap");
          
			Project project = (Project) request.getAttribute("project");
			Course course = (Course) request.getAttribute("course");
            
			Set<StudentRegistration> registrationSet = (Set<StudentRegistration>) request
					.getAttribute("studentRegistrationSet");

			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();

			// get the outcome from the canonical run; we'll use this to
			// retrieve the names of the test cases
			TestOutcomeCollection canonicalCollection = TestOutcomeCollection
					.lookupCanonicalOutcomesByProjectPK(project.getProjectPK(),
							conn);
			
			Timestamp ontime = project.getOntime();
			out.printf("## grades for project %s in %s%n",
			            project.getProjectNumber(), course.getCourseName());
			out.printf("## Generated at %tc%n", new Date());
			out.printf("##### Format is ### (extra ## on each of the following lines%n");
			out.printf("### acct, # submissions, minutesLate, time submitted, last name, first name%n");
			out.printf("## acct, test, score, comment%n");
            
                    
			
			for (StudentRegistration registration : registrationSet) {
				if (registration.getInstructorLevel() > StudentRegistration.STUDENT_CAPABILITY_LEVEL)
					continue;
				Submission submission = bestSubmissionMap.get(registration
						.getStudentRegistrationPK());
				
				if (submission != null) {
				    StudentSubmitStatus status = submitStatusMap.get(registration.getStudentRegistrationPK());
	                
					String classAccount = registration.getClassAccount();
					Timestamp submitted = submission.getSubmissionTimestamp();
					int lateInMinutes = 
					    (int)  TimeUnit.MINUTES.convert(submitted.getTime() - ontime.getTime(),
					              TimeUnit.MILLISECONDS);
					lateInMinutes -= status.getExtension() * 60;

					
					out.printf("# %s,%d,%d,%tc,%s,%s%n",
					        classAccount, 
					        status.getNumberSubmissions(),
					        lateInMinutes,
					        submitted,
					        registration.getLastname(),
					        registration.getFirstname());
					        
                    if (submission.getStatus().equals(Submission.LATE)) {
						if (project.getKindOfLatePenalty().equals(
								Project.CONSTANT))
							out.println(classAccount + ",*,-"
									+ project.getLateConstant() + ",Late");
						else
							out.println(classAccount + ",*,*"
									+ project.getLateMultiplier() + ",Late");
					} else if (submission.getStatus().equals(
							Submission.VERY_LATE))
						out.println(classAccount
								+ ",*,*0.0,Very Late");

					TestOutcomeCollection outcomeCollection = TestOutcomeCollection
							.lookupByTestRunPK(
									submission.getCurrentTestRunPK(), conn);

					for (TestOutcome canonicalOutcome : canonicalCollection) {
						// Skip anything that isn't a cardinal test type
						// (public,release,secret)
						if (!canonicalOutcome.isCardinalTestType())
							continue;

						TestOutcome outcome = outcomeCollection
								.getTest(canonicalOutcome.getTestName());
						StringBuffer result = new StringBuffer(
								classAccount);
						result.append(",");
						result.append(canonicalOutcome.getTestName());
						if (outcome == null) {
							result.append(",0,Could not run");
						} else if (outcome.getOutcome().equals(
								TestOutcome.PASSED)) {
							result.append(",");
							result.append(canonicalOutcome.getPointValue());
						} else {
							result.append(",0,");
							result.append(outcome.getOutcome());
						}
						out.println(result);
					}
				}
			}

			out.flush();
			out.close();
		} catch (SQLException e) {
			handleSQLException(e);
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
	}
}

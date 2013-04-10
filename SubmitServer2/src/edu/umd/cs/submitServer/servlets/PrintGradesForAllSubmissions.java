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
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.StudentSubmitStatus;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

public class PrintGradesForAllSubmissions extends SubmitServerServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Connection conn = null;
		try {
			conn = getConnection();

			Project project = (Project) request.getAttribute("project");
			Map<Integer, StudentRegistration> registrationMap = (Map<Integer, StudentRegistration>) request
					.getAttribute("studentRegistrationMap");
			Timestamp ontime = project.getOntime();
			Map<Integer, StudentSubmitStatus> submitStatusMap 
      = (Map<Integer, StudentSubmitStatus>) request.getAttribute("submitStatusMap");
     
			response.setContentType("text/plain");
	    response.setCharacterEncoding("UTF-8");
			String filename = "project-" + project.getProjectNumber()
					+ "-grades.csv";
			Util.setAttachmentHeaders(response, filename);

			PrintWriter out = response.getWriter();

			// get the outcome from the canonical run; we'll use this to
			// retrieve the names of the test cases
			TestOutcomeCollection canonicalCollection = TestOutcomeCollection
					.lookupCanonicalOutcomesByProjectPK(project.getProjectPK(),
							conn);

			// format and print the header
			StringBuilder header = new StringBuilder("classAccount,timestamp,UTC,hoursLate,total");
			for (TestOutcome outcome : canonicalCollection) {
				if (outcome.getTestType().equals(TestOutcome.TestType.BUILD))
					continue;
				header.append( "," + outcome.getTestType() + "_"
						+ outcome.getTestName());
			}
			out.println(header);

			// Look up all submissions for this project
			List<Submission> allSubmissions = Submission.lookupAllByProjectPK(
					project.getProjectPK(), conn);

			for (Submission submission : allSubmissions) {
				// Get the studentRegistration associated with this submission
				StudentRegistration registration = registrationMap
						.get(submission.getStudentRegistrationPK());
				StudentSubmitStatus status = submitStatusMap.get(registration.getStudentRegistrationPK());
        
				// Only interested in student submissions
				if (registration != null
						&& registration.getInstructorLevel() == StudentRegistration.STUDENT_CAPABILITY_LEVEL) {
					// Now find the test outcome collection
					TestOutcomeCollection testOutcomeCollection = TestOutcomeCollection
							.lookupByTestRunPK(
									submission.getCurrentTestRunPK(), conn);
					Timestamp submitted = submission.getSubmissionTimestamp();
          int lateInHours = 
              (int)  TimeUnit.HOURS.convert(submitted.getTime() - ontime.getTime(),
                        TimeUnit.MILLISECONDS);
          lateInHours -= status.getExtension();

					String result = registration.getClassAccount() + ","
							+ submission.getSubmissionTimestamp() + ","
							+ submission.getSubmissionTimestamp().getTime()
							+ "," 
							+ "," + lateInHours +","+ submission.getValuePassedOverall();
					for (TestOutcome outcome : testOutcomeCollection) {
						// Skip anything that is not a cardinal test type
						// (public,release,secret)
						if (!outcome.isCardinalTestType())
							continue;

						if (outcome.getOutcome().equals(TestOutcome.PASSED)) {
							result += "," + outcome.getPointValue();
						} else {
							result += ",0";
						}
					}
					out.println(result);
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

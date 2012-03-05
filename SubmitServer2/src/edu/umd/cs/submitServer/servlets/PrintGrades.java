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
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

/**
 * @author jspacco
 *
 */
public class PrintGrades extends SubmitServerServlet {


	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Connection conn = null;
		try {
			conn = getConnection();

			// get the project and all the student registrations
			Map<Integer, Submission> bestSubmissionMap = (Map<Integer, Submission>) request
					.getAttribute("bestSubmissionMap");

			Project project = (Project) request.getAttribute("project");
			Set<StudentRegistration> registrationSet = (Set<StudentRegistration>) request
					.getAttribute("studentRegistrationSet");

			response.setContentType("text/plain");
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
			StringBuilder header = new StringBuilder("classAccount,total,status");
			for (TestOutcome outcome : canonicalCollection) {
				if (outcome.getTestType().equals(TestOutcome.TestType.BUILD))
					continue;
				header.append("," + outcome.getTestType() + "_"
						+ outcome.getTestName());
			}
			out.println(header);

			for (StudentRegistration registration : registrationSet) {
				Submission submission = bestSubmissionMap.get(registration
						.getStudentRegistrationPK());

				if (submission != null) {
					String result = registration.getClassAccount() + ","
							+ submission.getAdjustedScore() + ","
							+ submission.getStatus();

					TestOutcomeCollection outcomeCollection = TestOutcomeCollection
							.lookupByTestRunPK(
									submission.getCurrentTestRunPK(), conn);

					for (TestOutcome outcome : outcomeCollection) {
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

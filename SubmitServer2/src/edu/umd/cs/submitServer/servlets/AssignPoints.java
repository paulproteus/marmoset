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
 * Created on Jan 25, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.LogEntry;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestRun;
import edu.umd.cs.marmoset.modelClasses.TestSetup;
import edu.umd.cs.submitServer.RequestParser;

/**
 * @author jspacco
 *
 */
public class AssignPoints extends SubmitServerServlet {


	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		RequestParser parser = new RequestParser(request,
				getSubmitServerServletLog(), strictParameterChecking());
		TestOutcomeCollection testOutcomeCollection = (TestOutcomeCollection) request
				.getAttribute("testOutcomeCollection");
		TestSetup testSetup = (TestSetup) request.getAttribute("testSetup");
		Project project = (Project) request.getAttribute("project");
		Course course = (Course) request.getAttribute("course");
		String studentLink = "/view/project?projectPK="
			+ project.getProjectPK();
		TestRun testRun = (TestRun) request.getAttribute("testRun");
		String comment = parser.getOptionalCheckedParameter("comment");

		Submission submission = (Submission) request.getAttribute("submission");

		// Create a map of testNames to corresponding testOutcomes
		Map<String, TestOutcome> testOutcomes = new HashMap<String, TestOutcome>();
		for (TestOutcome testOutcome : testOutcomeCollection
				.getAllTestOutcomes()) {
			testOutcomes.put(testOutcome.getTestName(), testOutcome);
		}

		Connection conn = null;
		boolean transactionSuccess = false;

		try {
			conn = getConnection();

			TestSetup currentTestingSetup = TestSetup.lookupByTestSetupPK(
					project.getTestSetupPK(), conn);

			// Update point values for cardinal test types (PUBLIC, RELEASE,
			// SECRET) in the canonical submission
			for (Enumeration<String> e = parser.getParameterNames(); e
					.hasMoreElements();) {
				String pName = e.nextElement();
				TestOutcome testOutcome = testOutcomes.get(pName);
				if (testOutcome != null) {
				    Integer value = parser.getIntegerParameter(pName, null);
					if (value != null) 
					    testOutcome.setPointValue(value);
				}
			}

			// Now perform the batch-update
			testOutcomeCollection.batchUpdatePointValues(conn);

			int valuePassedOverall = testOutcomeCollection.getValuePassedOverall();
			int valuePublicTests = testOutcomeCollection.getValuePublicTests();
			int valueReleaseTests = testOutcomeCollection.getValueReleaseTests();
			int valueSecretTests = testOutcomeCollection.getValueSecretTests();

			testSetup.setValuePublicTests(valuePublicTests);
			testSetup.setValueReleaseTests(valueReleaseTests);
			testSetup.setValueSecretTests(valueSecretTests);
			testSetup.setValueTotalTests(valuePassedOverall);
			testSetup.update(conn);

			testRun.setValuePassedOverall(valuePassedOverall);
			testRun.setValuePublicTestsPassed(valuePublicTests);
			testRun.setValueReleaseTestsPassed(valueReleaseTests);
			testRun.setValueSecretTestsPassed(valueSecretTests);
			testRun.update(conn);

			if (submission != null) {
				submission.setValuePublicTestsPassed(valuePublicTests);
				submission.setValueReleaseTestsPassed(valueReleaseTests);
				submission.setValueSecretTestsPassed(valueSecretTests);
				submission.setValuePassedOverall(valuePassedOverall);
				submission.update(conn);
			}

			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

			if (currentTestingSetup == null) {
				testSetup.setVersion(1);
				LogEntry.activeNewTestSetup(conn, course, project, comment == null ? project.getTitle() + " testing activated" : comment,
						studentLink);
			} else {

				if (testSetup.getTestSetupPK().equals(
						currentTestingSetup.getTestSetupPK())) {
					getSubmitServerServletLog().debug("Huh???");
					getSubmitServerServletLog().debug(
							"Assigning points to project "
									+ project.getProjectNumber()
									+ ", testing setup PK "
									+ testSetup.getTestSetupPK());
					getSubmitServerServletLog().debug(
							"Test run " + testRun.getTestRunPK());
					getSubmitServerServletLog().debug(
							"old active setup is testing setup PK "
									+ currentTestingSetup.getTestSetupPK());
				}

				testSetup.setVersion(currentTestingSetup.getVersion() + 1);
				currentTestingSetup.setStatus(TestSetup.Status.INACTIVE);
				currentTestingSetup.update(conn);
				LogEntry.activeNewTestSetup(conn, course, project, comment == null ? project.getTitle() + " testing updated" : comment,
						studentLink);
			}
			testSetup.setComment(comment);
			testSetup.setStatus(TestSetup.Status.ACTIVE);
			project.setTestSetupPK(testSetup.getTestSetupPK());
			testSetup.update(conn);
			project.update(conn);
			conn.commit();
			transactionSuccess = true;

		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			rollbackIfUnsuccessfulAndAlwaysReleaseConnection(
					transactionSuccess, request, conn);
		}
		String redirectUrl = request.getContextPath()
				+ "/view/instructor/projectUtilities.jsp?projectPK="
				+ project.getProjectPK();
		response.sendRedirect(redirectUrl);
	}

}

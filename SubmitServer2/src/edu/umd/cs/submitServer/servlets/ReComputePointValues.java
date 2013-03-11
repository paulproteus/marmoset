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
 * Created on Feb 9, 2005
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.ITestSummary;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestRun;

/**
 * @author jspacco
 * 
 */
public class ReComputePointValues extends SubmitServerServlet {


	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Connection conn = null;
		boolean transactionSuccess = false;
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

			Project project = (Project) request.getAttribute("project");

			// find all the test runs for this project
			List<TestRun> testRunList = TestRun.lookupAllByProjectPK(
					project.getProjectPK(), conn);

			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();

			out.println("we've found " + testRunList.size() + " test runs");

			for (TestRun testRun : testRunList) {

				out.println("testRunPK: " + testRun.getTestRunPK());

				// fetch the corresponding testOutcomeCollection
				ITestSummary<?> outcomes = TestOutcomeCollection
						.lookupByTestRunPK(testRun.getTestRunPK(), conn);

				// update the testRun's information
				testRun.setCompileSuccessful(outcomes.isCompileSuccessful());
				testRun.setValuePublicTestsPassed(outcomes
						.getValuePublicTestsPassed());
				testRun.setValueReleaseTestsPassed(outcomes
						.getValueReleaseTestsPassed());
				testRun.setValueSecretTestsPassed(outcomes
						.getValueSecretTestsPassed());
				testRun.setValuePassedOverall(outcomes.getValuePassedOverall());
				testRun.update(conn);

				out.println("outcomes.getValuePassedOverall: "
						+ outcomes.getValuePassedOverall());

				// if it exists, get the submission that has this testRun as its
				// currentTestRun
				Submission submission = Submission.lookupByTestRunPK(
						testRun.getTestRunPK(), conn);

				// set the cached results for the current test run of this
				// submission
				if (submission != null) {
					out.println("recomputing for submissionPK: "
							+ submission.getSubmissionPK()
							+ ", getValuePassedOverall: "
							+ outcomes.getValuePassedOverall());
					submission.setCompileSuccessful(outcomes
							.isCompileSuccessful());
					submission.setValuePublicTestsPassed(outcomes
							.getValuePublicTestsPassed());
					submission.setValueReleaseTestsPassed(outcomes
							.getValueReleaseTestsPassed());
					submission.setValueSecretTestsPassed(outcomes
							.getValueSecretTestsPassed());
					submission.setValuePassedOverall(outcomes
							.getValuePassedOverall());
					submission.update(conn);
				}
			}

			conn.commit();
			transactionSuccess = true;

			out.flush();
			out.close();
		} catch (SQLException e) {
			handleSQLException(e);
			throw new ServletException(e);
		} finally {
			rollbackIfUnsuccessfulAndAlwaysReleaseConnection(
					transactionSuccess, request, conn);
		}
	}
}

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
 * Created on Jan 16, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

import edu.umd.cs.marmoset.modelClasses.BuildServer;
import edu.umd.cs.marmoset.modelClasses.CodeMetrics;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentSubmitStatus;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestRun;
import edu.umd.cs.marmoset.modelClasses.TestSetup;
import edu.umd.cs.marmoset.utilities.JavaMail;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.MultipartRequest;
import edu.umd.cs.submitServer.WebConfigProperties;

/**
 * @author jspacco Called (usually by the Build Server) to report the outcomes
 *         of a test run
 */
public class ReportTestOutcomes extends SubmitServerServlet {
	private static final WebConfigProperties webProperties = WebConfigProperties.get();
	private static Logger failedLog;

	private static Logger getFailedBackgroundRetestLog() {
		if (failedLog == null) {
			failedLog = Logger
					.getLogger("edu.umd.cs.submitServer.servlets.failedBackgroundRetestLog");
		}
		return failedLog;
	}

	private static Logger successfulLog;

	private static Logger getSuccessfulBackgroundRetestLog() {
		if (successfulLog == null) {
			successfulLog = Logger
					.getLogger("edu.umd.cs.submitServer.servlets.successfulBackgroundRetestLog");
		}
		return successfulLog;
	}


	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// will be set by MultipartRequestFilter
		MultipartRequest multipartRequest = (MultipartRequest) request
				.getAttribute(MULTIPART_REQUEST);

		Timestamp now = new Timestamp(System.currentTimeMillis());

	
		// Insert test outcomes into database,
		// create new TestRun
		// and update submission as having been tested.
		Connection conn = null;
		FileItem fileItem = null;
		boolean transactionSuccess = false;
		try {
			// Get submission pk and the submission
			@Submission.PK
			int submissionPK = Submission.asPK(multipartRequest.getIntParameter("submissionPK"));

			// Get the testSetupPK
			int testSetupPK = multipartRequest.getIntParameter("testSetupPK");
			boolean newTestSetup = multipartRequest
					.getBooleanParameter("newTestSetup");
			boolean isBackgroundRetest = multipartRequest
					.getBooleanParameter("isBackgroundRetest");
			String load = multipartRequest.getOptionalStringParameter("load");
			if (load == null)
				load = "unknown";
			String remoteHost = request.getRemoteHost();
			// Get test machine (if specified)
			String testMachine = multipartRequest
					.getOptionalStringParameter("testMachine");
			if (testMachine == null)
				testMachine = "unknown";

			// Get md5sum of classfiles (if specified)
			CodeMetrics codeMetrics = new CodeMetrics();
			codeMetrics.setMd5sumSourcefiles(multipartRequest
					.getOptionalStringParameter("md5sumClassfiles"));
			codeMetrics.setMd5sumClassfiles(multipartRequest
					.getOptionalStringParameter("md5sumSourcefiles"));
			if (multipartRequest.hasKey("codeSegmentSize")) {
				codeMetrics.setCodeSegmentSize(multipartRequest
						.getIntParameter("codeSegmentSize"));
			}

			// Get the fileItem
			fileItem = multipartRequest.getFileItem();

			// Read into TestOutcomeCollection in memory
			TestOutcomeCollection testOutcomeCollection = new TestOutcomeCollection();

			ObjectInputStream in = null;
			try {
				byte[] data = fileItem.get();
				in = new ObjectInputStream(new ByteArrayInputStream(data));
				testOutcomeCollection.read(in);
			} catch (IOException e) {
				getSubmitServerServletLog().error(
						"Could not read test outcomes from build server");
				throw new ServletException(e);
			} finally {
				if (in != null)
					in.close();
			}

			// Make sure test outcome collection is not empty
			if (testOutcomeCollection.isEmpty()) {
				String msg = "No test outcomes received; ";
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
				return;
			}

			logHotspotErrors(submissionPK, testSetupPK, testMachine,
					testOutcomeCollection);

			conn = getConnection();

			Submission submission = Submission.lookupBySubmissionPK(
					submissionPK, conn);

			if (submission == null) {
				throw new ServletException("submissionPK " + submissionPK
						+ " does not refer to a submission in the database");
			}
	

			Project project = Project.getByProjectPK(submission.getProjectPK(),
					conn);
			TestSetup testSetup = TestSetup.lookupByTestSetupPK(testSetupPK,
					conn);
			BuildServer.insertOrUpdateSuccess(conn, testMachine, remoteHost, now, load, submission);

			
			conn.close();
			// Begin a transaction.
			// We can set this to a low isolation level because
			// - We don't read anything
			// - The inserts/updates we perform should not affect
			// rows visible to any other transaction
			conn = getConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

			//
			// * Create new TestRun row
			// * increment numTestOutcomes in submissions table
			// * set currentTestRunPK in submissions table
			// * set testRunPK in all the testOutcomes
			// * write the testoutcomes to the disk

			// TODO Handle partial credit for grades here?
			if (!newTestSetup) {
				// Set point totals
				TestRun canonicalTestRun = TestRun.lookupByTestRunPK(
						testSetup.getTestRunPK(), conn);
				TestOutcomeCollection canonicalTestOutcomeCollection = TestOutcomeCollection
						.lookupByTestRunPK(canonicalTestRun.getTestRunPK(),
								conn);
				Map<String, TestOutcome> canonicalTestOutcomeMap = new HashMap<String, TestOutcome>();
				for (TestOutcome testOutcome : canonicalTestOutcomeCollection
						.getAllOutcomes()) {
					canonicalTestOutcomeMap.put(testOutcome.getTestName(),
							testOutcome);
				}
				for (TestOutcome testOutcome : testOutcomeCollection
						.getAllOutcomes()) {
					// TODO should this check isTestType()???
					if (!testOutcome.getTestType().equals(
							TestOutcome.FINDBUGS_TEST)
							&& !testOutcome.getTestType().equals(
									TestOutcome.UNCOVERED_METHOD)
							&& canonicalTestOutcomeMap.containsKey(testOutcome
									.getTestName())) {
						TestOutcome canonicalTestOutcome = canonicalTestOutcomeMap
								.get(testOutcome.getTestName());
						testOutcome.setPointValue(canonicalTestOutcome
								.getPointValue());
					}
				}
			} else {
				// set all point values to 1
				for (TestOutcome testOutcome : testOutcomeCollection) {
					testOutcome.setPointValue(1);
				}
			}

			// Background Retests:
			// If this was a background re-test, then we need to take special
			// steps.
			// This was a background-retest if:
			// 1) The BuildServer says that this was a background retest
			// 2) or the status has been left as "complete" or has been
			// explicitly marked "background"
			if (isBackgroundRetest
					|| !newTestSetup
					&& submission.getBuildStatus().equals(
							Submission.BuildStatus.COMPLETE)) {

				// Look up current testOutcomeCollection
				TestOutcomeCollection currentTestOutcomeCollection = TestOutcomeCollection
						.lookupByTestRunPK(submission.getCurrentTestRunPK(),
								conn);

				// Look up the testRun for the current TestOutcomeCollection
				TestRun currentTestRun = TestRun.lookupByTestRunPK(
						submission.getCurrentTestRunPK(), conn);
				if (currentTestRun.getTestSetupPK() != testSetupPK) {
					// Retest was against a different jarfile than the current
					// one;
					// for now just ignore this run.
					transactionSuccess = true;
					conn.commit();
					return;
				}

				// Compare the cardinal test outcomes from the two
				// outcomeCollections
				String differences = compareCardinalOutcomes(
						currentTestOutcomeCollection, testOutcomeCollection,
						submissionPK, testSetupPK, testMachine);

				if (differences == null) {
					// If the results are the same, great! We have more
					// confidence that this result is correct.
					submission.incrementNumSuccessfulBackgroundRetests();

					getSuccessfulBackgroundRetestLog().info(
							"Corroborating run for submissionPK = "
									+ submissionPK + ", testSetupPK = "
									+ testSetupPK + " performed by "
									+ testMachine);
				} else if (differences.equals("skip")) {
					// There may have been differences but we don't care
					// We don't re-test "could_not_run" results
					conn.commit();
					transactionSuccess = true;
					return;
				} else {
					// If the results differ, log which test cases were
					// different
					submission.incrementNumFailedBackgroundRetests();
					// TODO: RSS feed
					getFailedBackgroundRetestLog().warn(differences);
					// XXX Should I insert differing outcomes into the database
					// as re-tests?
					if (currentTestOutcomeCollection.getValuePassedOverall() < testOutcomeCollection.getValuePassedOverall())
						isBackgroundRetest = false;

				}
				submission.update(conn);

				// If there were no differences, commit what we've done and
				// exit.
				if (differences == null) {
					conn.commit();
					transactionSuccess = true;
					return;
				}
			}

			// Create new TestRun row.
			TestRun testRun = new TestRun();
			testRun.setSubmissionPK(submissionPK);
			testRun.setTestSetupPK(testSetupPK);
			testRun.setTestMachine(testMachine);
			testRun.setTestTimestamp(now);
			testRun.setValuePassedOverall(testOutcomeCollection
					.getValuePassedOverall());
			testRun.setCompileSuccessful(testOutcomeCollection
					.isCompileSuccessful());
			testRun.setValuePublicTestsPassed(testOutcomeCollection
					.getValuePublicTestsPassed());
			testRun.setValueReleaseTestsPassed(testOutcomeCollection
					.getValueReleaseTestsPassed());
			testRun.setValueSecretTestsPassed(testOutcomeCollection
					.getValueSecretTestsPassed());
			testRun.setNumFindBugsWarnings(testOutcomeCollection
					.getNumFindBugsWarnings());
			// set the md5sum for this testRun
			// XXX currently the md5sums are stored in both testRuns and
			// codeMetrics tables
			if (codeMetrics.getMd5sumClassfiles() != null
					&& codeMetrics.getMd5sumSourcefiles() != null) {
				testRun.setMd5sumClassfiles(codeMetrics.getMd5sumClassfiles());
				testRun.setMd5sumSourcefiles(codeMetrics.getMd5sumSourcefiles());
			}

			// perform insert
			testRun.insert(conn);
			BuildServer.updateLastTestRun(conn, testMachine, testRun);


			// Insert a new codeMetrics row if we have codeMetrics data to
			// insert
			// codeMetrics data is keyed to the testRunPK
			if (codeMetrics.getCodeSegmentSize() > 0) {
				codeMetrics.setTestRunPK(testRun.getTestRunPK());
				codeMetrics.insert(conn);
			}

			// update the testRunPK of the testOutcomes we've been sent from the
			// BuildServer
			// with the testRunPK of the row we just inserted
			testOutcomeCollection.updateTestRunPK(testRun.getTestRunPK());

			// increment the number of test outcomes
			submission.setNumTestRuns(submission.getNumTestRuns() + 1);

			// Next, insert the test outcomes
			testOutcomeCollection.insert(conn);

			// if this was a new project jarfile being tested against the
			// canonical account
			if (newTestSetup) {
				// lookup the pending project

				testSetup.setValueTotalTests(testOutcomeCollection
						.getValuePassedOverall());
				testSetup.setValuePublicTests(testOutcomeCollection
						.getValuePublicTests());
				testSetup.setValueReleaseTests(testOutcomeCollection
						.getValueReleaseTests());
				testSetup.setValueSecretTests(testOutcomeCollection
						.getValueSecretTests());

				testSetup.setTestRunPK(testRun.getTestRunPK());

				if (!testOutcomeCollection.isCompileSuccessful()
						|| testOutcomeCollection.getNumFailedOverall() > 0
						|| testOutcomeCollection
								.getCouldNotRunAnyCardinalTests()) {
					// If any tests have failed, then set status to failed
					testSetup.setJarfileStatus(TestSetup.FAILED);
				} else {
					// TODO count num tests passed and set build/public/release
					// stats
					// set status to OK for this jarfile
					testSetup.setJarfileStatus(TestSetup.TESTED);

				}
				// update pending project_jarfile to reflect the changes just
				// made
				testSetup.update(conn);
			}

			// Only change information about the current test_run if this 
			// run used the most recent testSetup
			if (!isBackgroundRetest
					&& (!submission.isComplete() || testRun.getTestSetupPK() == project
							.getTestSetupPK())) {
				// update the pass/fail/warning stats
				submission.setCurrentTestRunPK(testRun.getTestRunPK());

				// [NAT]
				// Ensure that submission is not release eligible if the
				// student_submit_status.can_release_test is false
				StudentSubmitStatus submitStatus = StudentSubmitStatus
						.lookupByStudentRegistrationPKAndProjectPK(
								submission.getStudentRegistrationPK(),
								submission.getProjectPK(), conn);
				boolean canReleaseTest = (submitStatus == null)
						|| submitStatus.getCanReleaseTest();
				submission.setReleaseEligible(testOutcomeCollection
						.isReleaseEligible() && canReleaseTest);

				// if project's release policy is anytime, then make it release
				// eligible
				if (Project.ANYTIME.equals(project.getReleasePolicy()))
					submission.setReleaseEligible(canReleaseTest);

				submission.setValuePassedOverall(testOutcomeCollection
						.getValuePassedOverall());
				submission.setCompileSuccessful(testOutcomeCollection
						.isCompileSuccessful());
				submission.setValuePublicTestsPassed(testOutcomeCollection
						.getValuePublicTestsPassed());
				submission.setValueReleaseTestsPassed(testOutcomeCollection
						.getValueReleaseTestsPassed());
				submission.setValueSecretTestsPassed(testOutcomeCollection
						.getValueSecretTestsPassed());
				submission.setNumFindBugsWarnings(testOutcomeCollection
						.getNumFindBugsWarnings());

				if (!isBackgroundRetest) {
					// If we're re-setting the currentTestRunPK, then find any
					// existing
					// backgroundRetests and clear them. Background re-tests are
					// always compared
					// to the current set of testOutcomes.
					submission.setNumFailedBackgroundRetests(0);
					submission.setNumSuccessfulBackgroundRetests(0);
					submission.setNumPendingBuildRequests(0);
				}
			}
			// perform update
			// Update the status of the submission
			submission.setBuildStatus(Submission.BuildStatus.COMPLETE);
			submission.update(conn);

			conn.commit();
			transactionSuccess = true;
		} catch (InvalidRequiredParameterException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			System.out.println(e);
			e.printStackTrace();
			throw new ServletException(e);
		} finally {
			rollbackIfUnsuccessfulAndAlwaysReleaseConnection(
					transactionSuccess, request, conn);
			if (fileItem != null)
				fileItem.delete();
		}
	}

	/**
	 * @param submissionPK
	 * @param testSetupPK
	 * @param testMachine
	 * @param testOutcomeCollection
	 */
	private void logHotspotErrors(@Submission.PK int submissionPK, int testSetupPK,
			String testMachine, TestOutcomeCollection testOutcomeCollection) {
		String adminEmail = webProperties.getProperty("admin.email");
		if (adminEmail == null || adminEmail.equals("")) {
			getSubmitServerServletLog().error(
					"Can't find admin.email in web.properties");
		}
		String adminSMTP = webProperties.getProperty("admin.smtp");
		if (adminSMTP == null || adminSMTP.equals("")) {
			getSubmitServerServletLog().error(
					"Can't find admin.smtp in web.properties");
		}

		for (TestOutcome outcome : testOutcomeCollection) {
			if (outcome
					.getLongTestResult()
					.contains(
							"An unexpected error has been detected by HotSpot Virtual Machine")) {
				String errorMsg = "SubmissionPK " + submissionPK
						+ " for test-setup " + testSetupPK
						+ " from buildServer " + testMachine
						+ " had a HotSpot exception: " + outcome;
				getSubmitServerServletLog().error(errorMsg);
				try {
					JavaMail.sendMessage(adminEmail, adminEmail, adminSMTP,
							"HotSpot error!", outcome.toString());
				} catch (MessagingException e) {
					getSubmitServerServletLog()
							.error("Unable to email "
									+ adminEmail
									+ " from smtp server "
									+ adminSMTP
									+ " a warning about a HotSpot exception on one of the buildServers",
									e);
				}
			}
		}
	}

	/**
	 * Compares two testOutcomeCollections that were run against the same
	 * testSetupPK and produces a String suitable for entry in a log4j log
	 * summarizing the differences between the two collections. Used by the
	 * background retesting mechanism. TODO It's ugly that this returns a
	 * String!
	 *
	 * @param oldCollection
	 *            The collection currently in the database.
	 * @param newCollection
	 *            The collection returned after a background re-test.
	 * @param submissionPK
	 *            The submissionPK of the submission being retested.
	 * @param testSetupPK
	 *            The testSetupPK of the testSetup used for the re-test.
	 * @return null if the two testOutcomeCollections are the same; a String
	 *         suitable for writing in a log if there are differences
	 */
	private static String compareCardinalOutcomes(
			TestOutcomeCollection oldCollection,
			TestOutcomeCollection newCollection,
			 @Submission.PK Integer submissionPK,
			Integer testSetupPK, String testMachine) {
		for (TestOutcome newOutcome : newCollection) {
			// If any of the tests were marked "COULD_NOT_RUN" then don't record
			// anything
			// into the DB
			if (newOutcome.getOutcome().equals(TestOutcome.COULD_NOT_RUN)) {
				getFailedBackgroundRetestLog().warn(
						newOutcome.getOutcome() + " result for submissionPK "
								+ submissionPK + " and testSetupPK "
								+ testSetupPK);
				return null;
			}
		}
		// TODO Handle when one compiles and the next doesn't compile.
		// NOTE: We're ignoring a lot of other info here like md5sums, clover
		// info, etc.
		StringBuffer buf = new StringBuffer();
		for (TestOutcome oldOutcome : oldCollection
				.getIterableForCardinalTestTypes()) {
			TestOutcome newOutcome = newCollection.getTest(oldOutcome
					.getTestName());

			if (oldOutcome.getOutcome().equals(TestOutcome.COULD_NOT_RUN)) {
				getFailedBackgroundRetestLog().warn(
						"Not currently able to compare could not runs");
				return "skip";
			}
			if (newOutcome == null) {
				// We're in trouble here
				throw new IllegalStateException(
						"Can't find "
								+ oldOutcome.getTestName()
								+ " in new testOutcomeCollection after background retest for "
								+ " submissionPK " + submissionPK
								+ " and testSetupPK " + testSetupPK);
			}
			if (!oldOutcome.getOutcome().equals(newOutcome.getOutcome())) {
				buf.append("\t" + oldOutcome.getTestName()
						+ ": outcome in database is " + oldOutcome.getOutcome()
						+ "; background retest produced "
						+ newOutcome.getOutcome() + "\n");
			}
		}
		if (buf.length() > 0) {
			buf.insert(0, "submissionPK = " + submissionPK + ", testSetupPK = "
					+ testSetupPK + " on " + testMachine + ":\n");
			// Strip off last newline
			buf.delete(buf.length() - 1, buf.length());
			return buf.toString();
		}
		return null;
	}
}

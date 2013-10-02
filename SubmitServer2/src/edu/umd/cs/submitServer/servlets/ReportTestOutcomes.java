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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
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
import edu.umd.cs.marmoset.modelClasses.ServerError;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.StudentSubmitStatus;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestRun;
import edu.umd.cs.marmoset.modelClasses.TestRun.Kind;
import edu.umd.cs.marmoset.modelClasses.TestSetup;
import edu.umd.cs.marmoset.utilities.JavaMail;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.MultipartRequest;
import edu.umd.cs.submitServer.WebConfigProperties;
import edu.umd.cs.submitServer.filters.SubmitServerFilter;

/**
 * @author jspacco Called (usually by the Build Server) to report the outcomes
 *         of a test run
 */
public class ReportTestOutcomes extends SubmitServerServlet {
  private static final WebConfigProperties webProperties = WebConfigProperties.get();
  private static Logger failedLog;

  private static Logger getFailedBackgroundRetestLog() {
    if (failedLog == null) {
      failedLog = Logger.getLogger("edu.umd.cs.submitServer.servlets.failedBackgroundRetestLog");
    }
    return failedLog;
  }

  private static Logger successfulLog;

  private static Logger getSuccessfulBackgroundRetestLog() {
    if (successfulLog == null) {
      successfulLog = Logger.getLogger("edu.umd.cs.submitServer.servlets.successfulBackgroundRetestLog");
    }
    return successfulLog;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // will be set by MultipartRequestFilter
    MultipartRequest multipartRequest = (MultipartRequest) request.getAttribute(MULTIPART_REQUEST);

    Timestamp now = new Timestamp(System.currentTimeMillis());

    // Insert test outcomes into database,
    // create new TestRun
    // and update submission as having been tested.
    Connection conn = null;
    boolean transactionSuccess = false;
    try {
      // Get submission pk and the submission
      @Submission.PK
      int submissionPK = Submission.asPK(multipartRequest.getIntParameter("submissionPK"));

      String courses = multipartRequest.getStringParameter("courses");

      // Get the testSetupPK
      int testSetupPK = multipartRequest.getIntParameter("testSetupPK");
      boolean newTestSetup = multipartRequest.getBooleanParameter("newTestSetup");
      boolean isBackgroundRetest = multipartRequest.getBooleanParameter("isBackgroundRetest");
      String load = multipartRequest.getOptionalStringParameter("load");
      
      if (load == null)
        load = "unknown";
      String kindString = multipartRequest.getOptionalStringParameter("kind");
      Kind kind = Kind.UNKNOWN;
      if (kindString != null) {
        try {
        kind = Kind.valueOf(kindString);
        } catch (RuntimeException e) {
          // ignore
        }
        
      }
      
      String remoteHost = SubmitServerFilter.getRemoteHost(request);
      // Get test machine (if specified)
      String testMachine = multipartRequest.getOptionalStringParameter("hostname");
      if (testMachine == null)
        testMachine = multipartRequest.getOptionalStringParameter("testMachine");
      if (testMachine == null)
        testMachine = "unknown";

      CodeMetrics codeMetrics = getCodeMetrics(multipartRequest);
      int testDurationsMillis = multipartRequest.getIntegerParameter("testDurationsMillis", 0);

      // Read into TestOutcomeCollection in memory
      TestOutcomeCollection testOutcomeCollection = TestOutcomeCollection.deserialize(
          getfileItemDataAndDelete(multipartRequest));

      logHotspotErrors(submissionPK, testSetupPK, testMachine, testOutcomeCollection);

      conn = getConnection();

      Submission submission = Submission.lookupBySubmissionPK(submissionPK, conn);

      if (submission == null) {
        throw new ServletException("submissionPK " + submissionPK + " does not refer to a submission in the database");
      }

      StudentRegistration studentRegistration = StudentRegistration.lookupByStudentRegistrationPK(
          submission.getStudentRegistrationPK(), conn);
      @Student.PK
      Integer studentPK = studentRegistration.getStudentPK();
      Project project = Project.getByProjectPK(submission.getProjectPK(), conn);

      TestSetup testSetup = TestSetup.lookupByTestSetupPK(testSetupPK, conn);
      Integer canonicalTestRunPK = testSetup.getTestRunPK();
      if (newTestSetup
          && (testSetup.getStatus() == TestSetup.Status.TESTED || testSetup.getStatus() == TestSetup.Status.ACTIVE)) {
        newTestSetup = false;
      }
      BuildServer.insertOrUpdateSuccess(conn, testMachine, remoteHost, now, load, submission);

      // Validate buildserver
      Collection<Integer> allowedCourses = RequestSubmission.getCourses(conn, courses);

      if (allowedCourses.isEmpty()) {
        ServerError.insert(conn, ServerError.Kind.BAD_AUTHENTICATION, studentPK, studentPK, project.getCoursePK(),
            project.getProjectPK(), submission.getSubmissionPK(), "", "Build server " + testMachine
                + " reporting outcome but does not provide any valid credentials", "", this.getClass().getSimpleName(),
            "", "", remoteHost, "", "", null);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authorized");
        return;
      }

      if (!allowedCourses.contains(project.getCoursePK())) {
        ServerError.insert(conn, ServerError.Kind.BAD_AUTHENTICATION, studentPK, studentPK, project.getCoursePK(),
            project.getProjectPK(), submission.getSubmissionPK(), "", "Build server " + testMachine
                + " reporting outcome for course it is not authorized to do so", "", this.getClass().getSimpleName(),
            "", "", remoteHost, "", "", null);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authorized");
        return;
      }
      
      
      
      TestRun canonicalTestRun = TestRun.lookupByTestRunPK(canonicalTestRunPK, conn);
      if (!newTestSetup) {
        if (canonicalTestRun == null || canonicalTestRun.getTestSetupPK() != testSetupPK) {
          ServerError.insert(conn, ServerError.Kind.UNKNOWN, studentPK, studentPK, project.getCoursePK(),
              project.getProjectPK(), submission.getSubmissionPK(), "", "Discarding stale build server result", "",
              this.getClass().getSimpleName(), "", "", remoteHost, "", "", null);
          response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "not current test setup");
          return;
        }

        // Set point totals

        TestOutcomeCollection canonicalTestOutcomeCollection = TestOutcomeCollection.lookupByTestRunPK(
            canonicalTestRun.getTestRunPK(), conn);
        Map<String, TestOutcome> canonicalTestOutcomeMap = new HashMap<String, TestOutcome>();
        for (TestOutcome testOutcome : canonicalTestOutcomeCollection.getAllOutcomes()) {
          if (testOutcome.isCardinalTestType())
            canonicalTestOutcomeMap.put(testOutcome.getTestName(), testOutcome);
        }
        
        for (TestOutcome testOutcome : testOutcomeCollection.getAllOutcomes())
          if (testOutcome.isCardinalTestType() && !testOutcome.getOutcome().equals(TestOutcome.COULD_NOT_RUN)) {
            TestOutcome canonicalTestOutcome = canonicalTestOutcomeMap.get(testOutcome.getTestName());
            if (canonicalTestOutcome == null || !canonicalTestOutcome.getTestType().equals(testOutcome.getTestType())) {
              String message = "Did not find matching canonical test outcome for " + testOutcome.getTestName()
                  + " with outcome " + testOutcome.getOutcome();
              ServerError.insert(conn, ServerError.Kind.UNKNOWN, studentPK, studentPK, project.getCoursePK(), project
                  .getProjectPK(), submission.getSubmissionPK(), "", message, "", this.getClass().getSimpleName(), "",
                  "", remoteHost, "", "", null);
              response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, message);
              return;
            }

            testOutcome.setPointValue(canonicalTestOutcome.getPointValue());

          }
      } else {
        
        // new test setup
        
        // set all point values to 1
        for (TestOutcome testOutcome : testOutcomeCollection) {
          testOutcome.setPointValue(1);
        }
      }

      getSubmitServerServletLog().info(
          "Reporting test outcome for submissionPK = " + submissionPK + ", testSetupPK = " + testSetupPK
              + " performed by " + testMachine + ", kind = " + kind);

      // Background Retests:
      // If this was a background re-test, then we need to take special
      // steps.
      // This was a background-retest if:
      // 1) The BuildServer says that this was a background retest
      // 2) or the status has been left as "complete" or has been
      // explicitly marked "background"
      if (isBackgroundRetest || !newTestSetup && submission.getBuildStatus().equals(Submission.BuildStatus.COMPLETE)) {

        // Look up current testOutcomeCollection
        TestOutcomeCollection currentTestOutcomeCollection = TestOutcomeCollection.lookupByTestRunPK(
            submission.getCurrentTestRunPK(), conn);

        // Look up the testRun for the current TestOutcomeCollection
        TestRun currentTestRun = TestRun.lookupByTestRunPK(submission.getCurrentTestRunPK(), conn);
        if (currentTestRun == null)
          getSubmitServerServletLog().warn("Unable to find test run " + submission.getCurrentTestRunPK());
        if (currentTestRun == null || currentTestRun.getTestSetupPK() != testSetupPK) {
          // Retest was against a different jarfile than the current
          // one;
          // for now just ignore this run.

          return;
        }

        // Compare the cardinal test outcomes from the two
        // outcomeCollections
        String differences = compareCardinalOutcomes(currentTestOutcomeCollection, testOutcomeCollection, submissionPK,
            testSetupPK, testMachine);

        if (differences == null) {
          // If the results are the same, great! We have more
          // confidence that this result is correct.
          submission.incrementNumSuccessfulBackgroundRetests();

          getSuccessfulBackgroundRetestLog().info(
              "Corroborating run for submissionPK = " + submissionPK + ", testSetupPK = " + testSetupPK
                  + " performed by " + testMachine);
        } else if (differences.equals("skip")) {
          // There may have been differences but we don't care
          // We don't re-test "could_not_run" results
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
        if (differences == null)
          return;
      } // isBackgroundRetest

      conn = switchToTransaction(conn);

      // * Create new TestRun row
      // * increment numTestOutcomes in submissions table
      // * set currentTestRunPK in submissions table
      // * set testRunPK in all the testOutcomes
      // * write the testoutcomes to the disk

      // Create new TestRun row.
      TestRun testRun = createTestRun(submissionPK, testSetupPK, testMachine, testOutcomeCollection, codeMetrics, now,
          testDurationsMillis);

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
        getSubmitServerServletLog().info(
            "Ran new test setup, submissionPK = " + submissionPK + ", testSetupPK = " + testSetupPK
                + " performed by " + testMachine + ", kind = " + kind);
        
        // lookup the pending project

        testSetup.setValueTotalTests(testOutcomeCollection.getValuePassedOverall());
        testSetup.setValuePublicTests(testOutcomeCollection.getValuePublicTests());
        testSetup.setValueReleaseTests(testOutcomeCollection.getValueReleaseTests());
        testSetup.setValueSecretTests(testOutcomeCollection.getValueSecretTests());

        testSetup.setTestRunPK(testRun.getTestRunPK());

        if (!testOutcomeCollection.isCompileSuccessful() || testOutcomeCollection.getNumFailedOverall() > 0
            || testOutcomeCollection.getCouldNotRunAnyCardinalTests()) {
          // If any tests have failed, then set status to failed
          testSetup.setStatus(TestSetup.Status.FAILED);
        } else {
          testSetup.setStatus(TestSetup.Status.TESTED);
        }
        getSubmitServerServletLog().info(
            "Ran new test setup, submissionPK = " + submissionPK + ", testSetupPK = " + testSetupPK
                + " performed by " + testMachine + ", kind = " + kind + ", status = " + testSetup.getStatus());
      
        // update pending testSetup to reflect the changes just
        // made
        testSetup.update(conn);
      }

      // Only change information about the current test_run if this
      // run used the most recent testSetup
      if (!isBackgroundRetest && (newTestSetup || testRun.getTestSetupPK() == project.getTestSetupPK())) {
        // update the pass/fail/warning stats
        submission.setCurrentTestRunPK(testRun.getTestRunPK());

        // Ensure that submission is not release eligible if the
        // student_submit_status.can_release_test is false
        StudentSubmitStatus submitStatus = StudentSubmitStatus.lookupByStudentRegistrationPKAndProjectPK(
            submission.getStudentRegistrationPK(), submission.getProjectPK(), conn);
        boolean canReleaseTest = (submitStatus == null) || submitStatus.getCanReleaseTest();
        submission.setReleaseEligible(testOutcomeCollection.isReleaseEligible() && canReleaseTest);

        // if project's release policy is anytime, then make it release
        // eligible
        if (Project.ANYTIME.equals(project.getReleasePolicy()))
          submission.setReleaseEligible(canReleaseTest);

        submission.setValuePassedOverall(testOutcomeCollection.getValuePassedOverall());
        submission.setCompileSuccessful(testOutcomeCollection.isCompileSuccessful());
        submission.setValuePublicTestsPassed(testOutcomeCollection.getValuePublicTestsPassed());
        submission.setValueReleaseTestsPassed(testOutcomeCollection.getValueReleaseTestsPassed());
        submission.setValueSecretTestsPassed(testOutcomeCollection.getValueSecretTestsPassed());
        submission.setNumFindBugsWarnings(testOutcomeCollection.getNumFindBugsWarnings());

        // If we're re-setting the currentTestRunPK, then find any
        // existing
        // backgroundRetests and clear them. Background re-tests are
        // always compared
        // to the current set of testOutcomes.
        submission.setNumFailedBackgroundRetests(0);
        submission.setNumSuccessfulBackgroundRetests(0);
        submission.setNumPendingBuildRequests(0);

      }
       // perform update
      // Update the status of the submission
      submission.setBuildStatus(Submission.BuildStatus.COMPLETE);
      submission.update(conn);
      getSubmitServerServletLog().info(
          "Completed testing submissionPK = " + submissionPK + ", testSetupPK = " + testSetupPK
              + " performed by " + testMachine + ", kind = " + kind + ", status = " + testSetup.getStatus());
    
      conn.commit();
      transactionSuccess = true;
    } catch (InvalidRequiredParameterException e) {
      throw new ServletException(e);
    } catch (SQLException e) {
      throw new ServletException(e);
    } finally {
      rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess, request, conn);
    }
  }

  /**
   * @param multipartRequest
   * @return
   */
  private byte[] getfileItemDataAndDelete(MultipartRequest multipartRequest) {
    // Get the fileItem
    FileItem fileItem = multipartRequest.getFileItem();
    byte[] data = fileItem.get();
    fileItem.delete();
    return data;
  }

  /**
   * @param multipartRequest
   * @return
   * @throws InvalidRequiredParameterException
   */
  private CodeMetrics getCodeMetrics(MultipartRequest multipartRequest) throws InvalidRequiredParameterException {
    CodeMetrics codeMetrics = new CodeMetrics();
    codeMetrics.setMd5sumSourcefiles(multipartRequest.getOptionalStringParameter("md5sumClassfiles"));
    codeMetrics.setMd5sumClassfiles(multipartRequest.getOptionalStringParameter("md5sumSourcefiles"));
    if (multipartRequest.hasKey("codeSegmentSize")) {
      codeMetrics.setCodeSegmentSize(multipartRequest.getIntParameter("codeSegmentSize"));
    }
    return codeMetrics;
  }

  /**
   * @param submissionPK
   * @param testSetupPK
   * @param testMachine
   * @param testOutcomeCollection
   * @param codeMetrics
   * @param now
   * @param testDurationsMillis
   * @return
   */
  private TestRun createTestRun(@Submission.PK int submissionPK, int testSetupPK, String testMachine,
      TestOutcomeCollection testOutcomeCollection, CodeMetrics codeMetrics, Timestamp now, int testDurationsMillis) {
    TestRun testRun = new TestRun();
    testRun.setSubmissionPK(submissionPK);
    testRun.setTestSetupPK(testSetupPK);
    testRun.setTestMachine(testMachine);
    testRun.setTestTimestamp(now);
    testRun.setValuePassedOverall(testOutcomeCollection.getValuePassedOverall());
    testRun.setCompileSuccessful(testOutcomeCollection.isCompileSuccessful());
    testRun.setValuePublicTestsPassed(testOutcomeCollection.getValuePublicTestsPassed());
    testRun.setValueReleaseTestsPassed(testOutcomeCollection.getValueReleaseTestsPassed());
    testRun.setValueSecretTestsPassed(testOutcomeCollection.getValueSecretTestsPassed());
    testRun.setNumFindBugsWarnings(testOutcomeCollection.getNumFindBugsWarnings());
    // set the md5sum for this testRun
    // XXX currently the md5sums are stored in both testRuns and
    // codeMetrics tables
    if (codeMetrics.getMd5sumClassfiles() != null && codeMetrics.getMd5sumSourcefiles() != null) {
      testRun.setMd5sumClassfiles(codeMetrics.getMd5sumClassfiles());
      testRun.setMd5sumSourcefiles(codeMetrics.getMd5sumSourcefiles());
    }
    testRun.setTestDurationMillis(testDurationsMillis);
    return testRun;
  }

  /**
   * @param conn
   * @return
   * @throws SQLException
   */
  private Connection switchToTransaction(Connection conn) throws SQLException {
    conn.close();
    // Begin a transaction.
    // We can set this to a low isolation level because
    // - We don't read anything
    // - The inserts/updates we perform should not affect
    // rows visible to any other transaction
    conn = getConnection();
    conn.setAutoCommit(false);
    conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
    return conn;
  }

  /**
   * @param submissionPK
   * @param testSetupPK
   * @param testMachine
   * @param testOutcomeCollection
   */
  private void logHotspotErrors(@Submission.PK int submissionPK, int testSetupPK, String testMachine,
      TestOutcomeCollection testOutcomeCollection) {
    String adminEmail = webProperties.getProperty("admin.email");
    if (adminEmail == null || adminEmail.equals("")) {
      getSubmitServerServletLog().error("Can't find admin.email in web.properties");
    }
    String adminSMTP = webProperties.getProperty("admin.smtp");
    if (adminSMTP == null || adminSMTP.equals("")) {
      getSubmitServerServletLog().error("Can't find admin.smtp in web.properties");
    }

    for (TestOutcome outcome : testOutcomeCollection) {
      if (outcome.getLongTestResult().contains("An unexpected error has been detected by HotSpot Virtual Machine")) {
        String errorMsg = "SubmissionPK " + submissionPK + " for test-setup " + testSetupPK + " from buildServer "
            + testMachine + " had a HotSpot exception: " + outcome;
        getSubmitServerServletLog().error(errorMsg);
        try {
          JavaMail.sendMessage(adminEmail, adminEmail, adminSMTP, "HotSpot error!", outcome.toString());
        } catch (MessagingException e) {
          getSubmitServerServletLog().error(
              "Unable to email " + adminEmail + " from smtp server " + adminSMTP
                  + " a warning about a HotSpot exception on one of the buildServers", e);
        }
      }
    }
  }

  /**
   * Compares two testOutcomeCollections that were run against the same
   * testSetupPK and produces a String suitable for entry in a log4j log
   * summarizing the differences between the two collections. Used by the
   * background retesting mechanism. TODO It's ugly that this returns a String!
   * 
   * @param oldCollection
   *          The collection currently in the database.
   * @param newCollection
   *          The collection returned after a background re-test.
   * @param submissionPK
   *          The submissionPK of the submission being retested.
   * @param testSetupPK
   *          The testSetupPK of the testSetup used for the re-test.
   * @return null if the two testOutcomeCollections are the same; a String
   *         suitable for writing in a log if there are differences
   */
  private static String compareCardinalOutcomes(TestOutcomeCollection oldCollection,
      TestOutcomeCollection newCollection, @Submission.PK Integer submissionPK, Integer testSetupPK, String testMachine) {
    for (TestOutcome newOutcome : newCollection) {
      // If any of the tests were marked "COULD_NOT_RUN" then don't record
      // anything
      // into the DB
      if (newOutcome.getOutcome().equals(TestOutcome.COULD_NOT_RUN)) {
        getFailedBackgroundRetestLog().warn(
            newOutcome.getOutcome() + " result for submissionPK " + submissionPK + ", testSetupPK " + testSetupPK
                + " and test " + newOutcome.getTestType() + newOutcome.getTestNumber() + " : "
                + newOutcome.getTestName());
        return null;
      }
    }
    // TODO Handle when one compiles and the next doesn't compile.
    // NOTE: We're ignoring a lot of other info here like md5sums, clover
    // info, etc.
    StringBuffer buf = new StringBuffer();
    for (TestOutcome oldOutcome : oldCollection.getIterableForCardinalTestTypes()) {
      TestOutcome newOutcome = newCollection.getTest(oldOutcome.getTestName());

      if (oldOutcome.getOutcome().equals(TestOutcome.COULD_NOT_RUN)) {
        getFailedBackgroundRetestLog().warn("Not currently able to compare could not runs");
        return "skip";
      }
      if (newOutcome == null) {
        // We're in trouble here
        throw new IllegalStateException("Can't find " + oldOutcome.getTestName()
            + " in new testOutcomeCollection after background retest for " + " submissionPK " + submissionPK
            + " and testSetupPK " + testSetupPK);
      }
      if (!oldOutcome.getOutcome().equals(newOutcome.getOutcome())) {
        buf.append("\t" + oldOutcome.getTestName() + ": outcome in database is " + oldOutcome.getOutcome()
            + "; background retest produced " + newOutcome.getOutcome() + "\n");
      }
    }
    if (buf.length() > 0) {
      buf.insert(0, "submissionPK = " + submissionPK + ", testSetupPK = " + testSetupPK + " on " + testMachine + ":\n");
      // Strip off last newline
      buf.delete(buf.length() - 1, buf.length());
      return buf.toString();
    }
    return null;
  }
}

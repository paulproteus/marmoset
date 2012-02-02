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
 * Created on Jan 15, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.BuildServer;
import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.HttpHeaders;
import edu.umd.cs.marmoset.modelClasses.IO;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Queries;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.Submission.BuildStatus;
import edu.umd.cs.marmoset.modelClasses.TestSetup;
import edu.umd.cs.submitServer.MultipartRequest;
import edu.umd.cs.submitServer.WebConfigProperties;
import edu.umd.cs.submitServer.filters.MonitorSlowTransactionsFilter;

/**
 * @author jspacco
 * 
 */
public class RequestSubmission extends SubmitServerServlet {
	private static final WebConfigProperties webProperties = WebConfigProperties.get();
    private static final int LOG_REQUESTS_MORE_THAN_MS = 9000;

    static HashSet<String> complainedAbout = new HashSet<String>();

    private static final int MAX_BUILD_DURATION_MINUTES = 3;

    private static Lock lock = new java.util.concurrent.locks.ReentrantLock();

    enum Kind {
        UNKNOWN, SPECIFIC_REQUEST, SPECIFIC_REQUEST_NEW_TESTUP, SPECIFIC_REQUEST_NO_TESTSETUP, NEW_TEST_SETUP, BUILD_STATUS_NEW, EXPLICIT_RETESTS, OUT_OF_DATE_TEST_SETUPS;
    }

    public static void timeDump(StringBuffer buf, Timestamp started, String f, Object... args) {
        long now = System.currentTimeMillis();
        long delay = now - started.getTime();
        buf.append(String.format("%4d ", delay));
        buf.append(String.format(f, args));

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new ServletException();

    }
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Connection conn = null;
        // Create model objects to represent a submission and
        // auxiliary info
        Submission submission = new Submission();
        TestSetup testSetup = new TestSetup();
        MultipartRequest multipartRequest = (MultipartRequest) request.getAttribute(MULTIPART_REQUEST);
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String submissionPK = multipartRequest.getOptionalCheckedParameter("submissionPK");
        String testSetupPK = multipartRequest.getOptionalCheckedParameter("testSetupPK");

        String hostname = multipartRequest.getOptionalStringParameter("hostname");
        String load = multipartRequest.getOptionalStringParameter("load");
        if (load == null)
            load = "unknown";
        Kind kind = Kind.UNKNOWN;
        Queries.RetestPriority foundPriority = null;
        String courses = multipartRequest.getStringParameter("courses");
        String remoteHost = request.getRemoteHost();

        try {
            boolean foundNewTestSetup = false;
            boolean foundSubmissionForBackgroundRetesting = false;

            /**
             * We are going to use Java static locking as well as database
             * locking. If requests from two build servers come in at the same
             * time, we'd just wind up trying to assign them the same submission
             * and having to roll back one. Better to just do them one at a
             * time.
             */
            StringBuffer buf = new StringBuffer();

            boolean locked = lock.tryLock(100, TimeUnit.MILLISECONDS);
            if (!locked) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, SUBMIT_SERVER_BUSY);
                return;
            }
            try {
                conn = getConnection();
                 Collection<Integer> allowedCourses = Course.lookupAllPKByBuildserverKey(conn, courses);
               

                if (submissionPK != null) {
                    submission = Submission.lookupBySubmissionPK(Submission.asPK(Integer.valueOf(submissionPK)), conn);
                    Project project = Project.lookupByProjectPK(submission.getProjectPK(), conn);
                    if (testSetupPK != null) {
                        testSetup = TestSetup.lookupByTestSetupPK(Integer.valueOf(testSetupPK), conn);
                        if (testSetup !=null && testSetup.getProjectPK() != submission.getProjectPK())
                            throw new ServletException("Submission " + submissionPK + " and test setup " + testSetupPK 
                                    + " are for different projects");
                    } else
                        testSetup = TestSetup.lookupByTestSetupPK(project.getTestSetupPK(), conn);
                    if (testSetup != null) {
                        if (project.getTestSetupPK() == 0)
                            kind = Kind.SPECIFIC_REQUEST_NEW_TESTUP;
                        else
                            kind = Kind.SPECIFIC_REQUEST;
                    } else {
                        kind = Kind.SPECIFIC_REQUEST_NO_TESTSETUP;
                    }
                } else {

                    findSubmission: {

                        foundNewTestSetup = Queries.lookupNewTestSetup(conn, submission, testSetup, allowedCourses,
                                MAX_BUILD_DURATION_MINUTES);
                        kind = Kind.NEW_TEST_SETUP;
                        if (foundNewTestSetup)
                            break findSubmission;

                        for (Queries.RetestPriority priority : Queries.RetestPriority.values()) {

                            timeDump(buf, now, "%s%n", priority);
                            foundPriority = priority;
                            // most_recent submission without any test run,
                            // oldest first
                            kind = Kind.BUILD_STATUS_NEW;
                            if (Queries.lookupSubmission(conn, submission, testSetup, allowedCourses, BuildStatus.NEW, priority))
                                break findSubmission;

                            kind = Kind.EXPLICIT_RETESTS;
                            // look for explicit retests
                            if (Queries.lookupSubmission(conn, submission, testSetup, allowedCourses, BuildStatus.RETEST,
                                    priority))
                                break findSubmission;

                            kind = Kind.OUT_OF_DATE_TEST_SETUPS;
                            // look for out of date test setups
                            if (Queries.lookupSubmissionWithOutdatedTestResults(conn, submission, testSetup, allowedCourses,
                                    priority))
                                break findSubmission;

                            kind = Kind.BUILD_STATUS_NEW;

                            // now look for pending submissions
                            if (Queries.lookupPendingSubmission(conn, submission, testSetup, allowedCourses,
                                    MAX_BUILD_DURATION_MINUTES, priority)) {
                                if (submission.getNumPendingBuildRequests() <= 3)
                                    break findSubmission;

                                // TODO: RSS Entry:
                                submission.setBuildStatus(Submission.BuildStatus.BROKEN);
                                submission.update(conn);
                            }

                        }

                        foundSubmissionForBackgroundRetesting = true;
                        // *) look for ambient background retest
                        if (isBackgroundRetestingEnabled()
                                && Queries.lookupSubmissionForBackgroundRetest(conn, submission, testSetup, allowedCourses))
                            break findSubmission;
                        BuildServer.submissionRequestedNoneAvailable(conn, hostname, remoteHost, courses, now, load);

                        long delay = System.currentTimeMillis() - now.getTime();
                        if (delay > LOG_REQUESTS_MORE_THAN_MS) {
                            String msg = "Took " + delay + "ms to find nothing :: " + buf;
                            MonitorSlowTransactionsFilter.insertServerError(conn, request, msg, "Slow", "RequestSubmission");
                        }
                        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, NO_SUBMISSIONS_AVAILABLE_MESSASGE);
                        return;

                    }

                }

                timeDump(buf, now, "%s %s%n", kind, foundPriority);
                getSubmitServerServletLog().trace(
                        "RequestSubmission: submission pk = " + submission.getSubmissionPK() + ", projectPK =  "
                                + submission.getProjectPK());
                long delay = System.currentTimeMillis() - now.getTime();
                if (delay > LOG_REQUESTS_MORE_THAN_MS) {
                    String msg = "Took " + delay + "ms to find submission " + kind + " " + foundPriority + " :: " + buf;
                    MonitorSlowTransactionsFilter.insertServerError(conn, request, msg, "Slow", "RequestSubmission");
                }

                // at this point, either submission and testSetup are
                // non-null or we've already sent an error

                // Tell client not to cache this object.
                // See http://www.jguru.com/faq/view.jsp?EID=377
                Util.setNoCache(response);

                // Send submission PK in an HTTP header.
                response.setHeader(HttpHeaders.HTTP_SUBMISSION_PK_HEADER, submission.getSubmissionPK().toString());

                if (kind != null)
                    response.setHeader(HttpHeaders.HTTP_KIND_HEADER, kind.name());
                if (foundPriority != null)
                    response.setHeader(HttpHeaders.HTTP_PRIORITY_HEADER, foundPriority.name());
                response.setHeader(HttpHeaders.HTTP_PREVIOUS_BUILD_STATUS_HEADER, submission.getBuildStatus().name());

                // Send project PK in an HTTP header.
                boolean hasTestSetup = testSetup != null && testSetup.getTestSetupPK() != null;
                if (hasTestSetup)
                    response.setHeader(HttpHeaders.HTTP_TEST_SETUP_PK_HEADER, testSetup.getTestSetupPK().toString());
                else
                    response.setHeader(
                            HttpHeaders.HTTP_HUH_HEADER,
                            String.format("submission %d, project %d, no test setup", submission.getSubmissionPK(),
                                    submission.getProjectPK()));

                // if we found a new test setup, let the buildserver know
                if (hasTestSetup && (foundNewTestSetup || kind == Kind.SPECIFIC_REQUEST_NEW_TESTUP)) {
                    response.setHeader(HttpHeaders.HTTP_NEW_TEST_SETUP, "yes");
                    // set the status to 'pending' and set the current time for
                    // datePosted
                    // date_posted is a horrible name, this is the field I'm
                    // using for timeouts
                    testSetup.setJarfileStatus(TestSetup.PENDING);
                    testSetup.setDatePosted(new Timestamp(System.currentTimeMillis()));
                    testSetup.update(conn);
                } else
                    response.setHeader(HttpHeaders.HTTP_NEW_TEST_SETUP, "no");

                if (foundSubmissionForBackgroundRetesting)
                    response.setHeader(HttpHeaders.HTTP_BACKGROUND_RETEST, "yes");
                else
                    response.setHeader(HttpHeaders.HTTP_BACKGROUND_RETEST, "no");

                // If this is *NOT* a background re-test then
                // update build_status to pending, and build_request_timestamp
                // to current time.
                if (!foundSubmissionForBackgroundRetesting) {
                    submission.setBuildStatus(Submission.BuildStatus.PENDING);
                    submission.setBuildRequestTimestamp(new Timestamp(System.currentTimeMillis()));
                    submission.incrementNumPendingBuildRequests();
                    submission.update(conn);
                }
            } finally {
                lock.unlock();
            }
            // if communication with the buildserver fails after this
            // then we'll wait for things to time out
            // prepare byte array input stream of the submission
            byte[] bytes = submission.downloadArchive(conn);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

            // Inform client of file length
            response.setContentLength(bytes.length);

            // Inform client of content type
            response.setContentType("application/x-zip");

            // TODO: maybe we should send the md5sum?

            OutputStream out = response.getOutputStream();
            IO.copyStream(bais, out);
            BuildServer.submissionRequestedAndProvided(conn, hostname, remoteHost, courses, now, load, submission);
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } catch (InterruptedException e) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "request interrupted");
        } finally {
            releaseConnection(conn);
        }
    }

    static final String NO_SUBMISSIONS_AVAILABLE_MESSASGE = "No submissions available";
    static final String SUBMIT_SERVER_BUSY = "Submit server busy with other build server";

    private boolean performBackgroundRetesting = false;

    /*
     * (non-Javadoc)
     * 
     * @see edu.umd.cs.submitServer.servlets.SubmitServerServlet#init()
     */
    @Override
    public void init() throws ServletException {
        // TODO Auto-generated method stub
        super.init();
        String performBackgroundRetestingStr = webProperties.getProperty("perform.background.retesting");
        if ("true".equals(performBackgroundRetestingStr))
            performBackgroundRetesting = true;
    }

    private boolean isBackgroundRetestingEnabled() {
        return performBackgroundRetesting;
    }
}

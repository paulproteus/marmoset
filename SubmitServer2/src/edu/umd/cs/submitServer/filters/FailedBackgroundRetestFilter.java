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

/**
 * Created on Nov 5, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.ServerError;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestRun;
import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.UserSession;

/**
 * FailedBackgroundRetestFilter
 *
 * @author jspacco
 */
public class FailedBackgroundRetestFilter extends SubmitServerFilter {


    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;

        Project project = (Project) req.getAttribute(PROJECT);
        
        if (project == null)
            throw new ServletException("No project");

        StudentRegistration studentRegistration = (StudentRegistration) req.getAttribute(STUDENT_REGISTRATION);
        
        if (studentRegistration == null)
            throw new ServletException("No StudentRegistration");
        
        Boolean instructor = (Boolean) request.getAttribute(SubmitServerConstants.INSTRUCTOR_CAPABILITY);
        if (instructor == null)
            instructor = false;
        List<Submission> failedBackgroundRetestSubmissionList = new LinkedList<Submission>();

        Connection conn = null;
        try {
            conn = getConnection();

            if (studentRegistration == null)
                Submission.lookupAllWithFailedBackgroundRetestsByProjectPK(
                        project.getProjectPK(),
                        failedBackgroundRetestSubmissionList,
                        conn);
            else {
                Submission.lookupAllWithFailedBackgroundRetestsByProjectPK(
                        project.getProjectPK(),
                        studentRegistration.getStudentRegistrationPK(),
                        failedBackgroundRetestSubmissionList,
                        conn);


            }

            try {
                // XXX NAT Check to see if there are inconsistencies in public,
                // release, or secret tests
                Map<Integer, Boolean> publicInconsistencies = new HashMap<Integer, Boolean>();
                Map<Integer, Boolean> releaseInconsistencies = new HashMap<Integer, Boolean>();
                Map<Integer, Boolean> secretInconsistencies = new HashMap<Integer, Boolean>();

                for (Iterator<Submission> i =  failedBackgroundRetestSubmissionList.iterator(); i.hasNext(); ) {
                    Submission s = i.next();
                    @Submission.PK Integer submissionPK = s.getSubmissionPK();
                    if (s.getNumFailedBackgroundRetests() == 0) {
                        ServletExceptionFilter.logError(conn, ServerError.Kind.EXCEPTION, request, 
                                "submission " + submissionPK + " is and isn't inconsistent"
                                , "assertion", null);
                        continue;
                    }

                    List<TestRun> testRunList = TestRun
                            .lookupAllBySubmissionPK(submissionPK, conn);
                    int runs = testRunList == null ? 0 : testRunList.size();
                    if (runs <= 1) {
                        ServletExceptionFilter.logError(conn, ServerError.Kind.EXCEPTION, request, 
                                "submission " + submissionPK + " has failed backround tests but " + runs + " test runs"                                    
                                , "assertion", null);
                        continue;
                    }


                    boolean inconsistentPublic = false;
                    boolean inconsistentRelease = false;
                    boolean inconsistentSecret = false;

                    TestRun tr = testRunList.get(0);
                    int pTemp = tr.getValuePublicTestsPassed();
                    int rTemp = tr.getValueReleaseTestsPassed();
                    int sTemp = tr.getValueSecretTestsPassed();

                    for (TestRun r : testRunList) {
                        if (pTemp != r.getValuePublicTestsPassed())
                            inconsistentPublic = true;

                        if (rTemp != r.getValueReleaseTestsPassed())
                            inconsistentRelease = true;

                        if (sTemp != r.getValueSecretTestsPassed())
                            inconsistentSecret = true;

                    }
                    boolean visible = instructor || inconsistentPublic
                            || inconsistentRelease && s.isReleaseTestingRequested();

                    if (visible) {
                        publicInconsistencies.put(submissionPK, inconsistentPublic);
                        releaseInconsistencies.put(submissionPK, inconsistentRelease);
                        secretInconsistencies.put(submissionPK, inconsistentSecret);
                    } else
                        i.remove();

                }
                req.setAttribute("failedBackgroundRetestSubmissionList",
                        failedBackgroundRetestSubmissionList);

                req.setAttribute("publicInconsistencies", publicInconsistencies);
                req.setAttribute("releaseInconsistencies",
                        releaseInconsistencies);
                req.setAttribute("secretInconsistencies", secretInconsistencies);
                // END NAT
            } catch (RuntimeException e) {
                ServletExceptionFilter.logError(conn, ServerError.Kind.EXCEPTION, request, 
                       "error in " + FailedBackgroundRetestFilter.class.getSimpleName(),
                        null, e);
            } 

        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
        chain.doFilter(req, resp);
    }

}

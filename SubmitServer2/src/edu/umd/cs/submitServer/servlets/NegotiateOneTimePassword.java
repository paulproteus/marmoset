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
 * Created on Jan 14, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.StudentSubmitStatus;
import edu.umd.cs.submitServer.ClientRequestException;
import edu.umd.cs.submitServer.RequestParser;
import edu.umd.cs.submitServer.SubmitServerUtilities;
import edu.umd.cs.submitServer.filters.AccessLogFilter;
import edu.umd.cs.submitServer.filters.ServletExceptionFilter;

/**
 * @author jspacco
 * 
 */
public class NegotiateOneTimePassword extends SubmitServerServlet {
    private static Logger accessLog;

    private Logger getAccessLog() {
        if (accessLog == null) {
            accessLog = Logger.getLogger(AccessLogFilter.class);
        }
        return accessLog;
    }

    /**
     * The doPost method of the servlet. <br>
     * 
     * This method is called when a form has its tag value method equals to
     * post.
     * 
     * @param request
     *            the request send by the client to the server
     * @param response
     *            the response send by the server to the client
     * @throws ServletException
     *             if an error occurred
     * @throws IOException
     *             if an error occurred
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean transactionSuccess = false;
        Connection conn = null;
        String courseName = null;
        String section = null;
        String projectNumber = null;
        String semester = null;
        String campusUID = null;
        String uidPassword = null;
        Project project = null;
        StudentRegistration studentRegistration = null;
        
        try {
            conn = getConnection();
            RequestParser parser = new RequestParser(request, getSubmitServerServletLog(), strictParameterChecking());
            courseName = parser.getCheckedParameter("courseName");
            section = parser.getOptionalCheckedParameter("section");
            projectNumber = parser.getCheckedParameter("projectNumber");
            semester = parser.getCheckedParameter("semester");
            campusUID = parser.getCheckedParameter("campusUID");
            uidPassword = parser.getPasswordParameter("uidPassword");

            getAccessLog().info(
                    "NegotiateOneTimePassword attempt:\t" + campusUID + "\t" + semester + "\t" + courseName + "\t"
                            + projectNumber);



            // fetch course and project records
            // This cannot be done in a filter because this is the only place it
            // happens
            project = Project.lookupByCourseProjectSemester(courseName, section, projectNumber, semester, conn);
            if (project == null) {
                String msg = "Could not find record for project " + projectNumber + " in " + courseName + ", " + semester
                        + "; you likely have an out of date .submit file";
                ServletExceptionFilter.logErrorAndSendServerError(conn, request, response, msg, "login name: " + campusUID, null);
                return;
            }
            // authenticate the student and find their Student record

            // [NAT P001] temporary, refactor this to do authentication in one
            // place
            Student student = PerformLogin.authenticateStudent(conn, campusUID, uidPassword, skipLDAP, getIAuthenticationService());
            // [end NAT P001]

            // I need to do my own logging here-- AccessLogFilter cannot be
            // applied
            // to this servlet!
            getAccessLog().info(
                    "studentPK " + student.getStudentPK() + " requesting " + SubmitServerUtilities.extractURL(request));

            studentRegistration = StudentRegistration.lookupByStudentPKAndCoursePK(student.getStudentPK(), project.getCoursePK(),
                    conn);

            if (studentRegistration == null) {

                String msg = student.getFirstname() + " " + student.getLastname() + " is not registered for this " + courseName
                        + " in semester " + semester + ",  "
                        + " If you changed your DirectoryID, please notify your instructor so that we can update the system.";
                ServletExceptionFilter.logErrorAndSendServerError(conn, request, response, msg, "login name: " + campusUID, null);
                return;
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (NamingException e) {
            throw new ServletException(e);
        } catch (ClientRequestException e) {
            String msg = String.format("failed to negotiate oneTime password for %s in %s %s: %s", campusUID, courseName, semester,
                    e.getMessage());
           
            ServletExceptionFilter.logErrorAndSendServerError(conn, request, response, msg, "login name: " + campusUID, e);
           
            return;

        } finally {
            releaseConnection(conn);
        }
        conn = null;
        try {

            conn = getConnection();

            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

            StudentSubmitStatus submitStatus = StudentSubmitStatus.createOrInsert(project.getProjectPK(),
                    studentRegistration.getStudentRegistrationPK(), conn);

            String classAccount = studentRegistration.getClassAccount();
            String oneTimePassword = submitStatus.getOneTimePassword();

            conn.commit();
            transactionSuccess = true;

            // write out the classAccount/oneTimePassword pair
            response.setContentType("text/plain");
            PrintWriter out = response.getWriter();
            out.println("cvsAccount=" + classAccount);
            out.println("classAccount=" + classAccount);
            out.println("oneTimePassword=" + oneTimePassword);
            out.flush();
            out.close();

            getAccessLog().info(
                    "studentPK " + studentRegistration.getStudentPK() + " successful "
                            + SubmitServerUtilities.extractURL(request));
            getAuthenticationLog().info(
                    "NegotiateOneTimePassword success:\t" + campusUID + "\t" + semester + "\t" + courseName + "\t"
                            + projectNumber);

        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess, request, conn);
        }
    }

    boolean skipLDAP;
}

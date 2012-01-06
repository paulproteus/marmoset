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
import java.util.Collection;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Rubric;
import edu.umd.cs.marmoset.modelClasses.RubricEvaluation;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;

/**
 * @author jspacco
 * 
 */
public class PrintRubricEvaluationsForDatabase extends SubmitServerServlet {

    /**
     * The doGet method of the servlet. <br>
     * 
     * This method is called when a form has its tag value method equals to get.
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
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        Collection<Submission> submissionsUnderReview = (Collection<Submission>) request.getAttribute("submissionsUnderReview");
        Map<Integer, StudentRegistration> studentRegistrationMap = (Map<Integer, StudentRegistration>) request
                .getAttribute("studentRegistrationMap");
        Map<Integer, Rubric> rubricMap = (Map<Integer, Rubric>) request.getAttribute("rubricMap");

        try {
            conn = getConnection();
            for (Submission s : submissionsUnderReview) {

                String acct = studentRegistrationMap.get(s.getStudentRegistrationPK()).getClassAccount();
                for (RubricEvaluation e : RubricEvaluation.lookupBySubmissionPK(s.getSubmissionPK(), conn)) {
                    Rubric r = rubricMap.get(e.getRubricPK());
                    out.printf("%s,%s,%s,%s%n", acct, r.getName(), e.getPoints(), e.getValueAndExplanation(r));
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

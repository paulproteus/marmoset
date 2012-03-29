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
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.CodeReviewAssignment;
import edu.umd.cs.marmoset.modelClasses.CodeReviewer;
import edu.umd.cs.marmoset.modelClasses.Submission;

public class RemoveCodeReviewers extends SubmitServerServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
      
        CodeReviewAssignment assignment = (CodeReviewAssignment) request
                .getAttribute(CODE_REVIEW_ASSIGNMENT);

        Submission submission = (Submission) request.getAttribute(SUBMISSION);
        String redirectUrl;
         Connection conn = null;
        try {
            conn = getConnection();
            if (submission != null) {
                CodeReviewer.deleteInactiveReviewers(conn, assignment, submission);
                redirectUrl = request.getContextPath()
                        + "/view/instructor/submission.jsp?submissionPK="
                        + submission.getSubmissionPK();
            }
            else {
                CodeReviewer.deleteInactiveReviewers(conn, assignment);
                redirectUrl = request.getContextPath()
                        + "/view/instructor/codeReviewAssignment.jsp?codeReviewAssignmentPK="
                        + assignment.getCodeReviewAssignmentPK();
            }
           
          
            response.sendRedirect(redirectUrl);
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
        	releaseConnection(conn);
        }
    }

}

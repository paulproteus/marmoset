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
 * Created on Jan 13, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.CodeReviewAssignment;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.RequestParser;

public class AddSubmissionsToCodeReviewAssignment extends SubmitServerServlet {

    static boolean nullSafeEquals(Object x, Object y) {
        if (x == y)
            return true;
        if (x == null || y== null)
            return false;
        return x.equals(y);
    }
    static void add(LinkedHashMap<String, Integer> map, String name, @CheckForNull Integer value) {
        if (value == null) 
            return;
        map.put(name,value);
    }

    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleRequest(request, response, false);
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleRequest(request, response, true);
    }


    private void handleRequest(HttpServletRequest request, HttpServletResponse response, boolean post)
            throws ServletException, IOException {
	    
        CodeReviewAssignment assignment = (CodeReviewAssignment) request.getAttribute("codeReviewAssignment");
        Collection<Submission> submissionsUnderReview = (Collection<Submission>) request.getAttribute("submissionsUnderReview");
        Collection<StudentRegistration> students = (Collection<StudentRegistration>) request
                .getAttribute(JUST_STUDENT_REGISTRATION_SET);
        Map<Integer, Submission> lastSubmissionMap = (Map<Integer, Submission>) request.getAttribute("lastSubmission");
        int totalStudents = students.size();
        PrintWriter out = null;
        if (!post) {
            response.setContentType("text/plain");
            out = response.getWriter();
        }
        Map<Integer, StudentRegistration> studentRegistrationMap = 
                (Map<Integer, StudentRegistration>) request
        .getAttribute("studentRegistrationMap");
        for(Submission submission : submissionsUnderReview) {
            Integer submissionPK = submission.getSubmissionPK();
            if (submission.getProjectPK() != assignment.getProjectPK()) {
                if (!post)
                out.printf("skipping removal of submission pk %d%n", submissionPK);
                continue;
            }
            StudentRegistration sr = studentRegistrationMap.get(submission.getStudentRegistrationPK());

            if (sr != null) {
                students.remove(sr);
                if (!post)
                out.printf("removing %s; his submission %d is already under review %n", sr.getFullname(),
                        submissionPK) ;
            }
        }
        int withoutReview = students.size();
        
        RequestParser parser = new RequestParser(request,
                getSubmitServerServletLog(), strictParameterChecking());

        int numReviewersPerSubmission = parser.getIntParameter("numReviewers",1);
        ArrayList<Integer> codeReviewers = CreateCodeReviewAssignment.instructorReviewers(request);
    
        if (!post) {
            out.printf("add reviews for %d/%d students%n", withoutReview, totalStudents);
            out.printf("%d reviewers, %d reviews per submission%n", codeReviewers.size(), numReviewersPerSubmission);
            for(StudentRegistration sr : students) {
                out.printf(" adding review for %s%n", sr);
                Submission s = lastSubmissionMap.get(sr.getStudentRegistrationPK());
                if (s == null) {
                    out.printf("    no submission found%n");
                    
                } else
                    out.printf("    submission %d at %tc%n", s.getSubmissionNumber(), s.getSubmissionTimestamp());
            }
            out.close();
            return;
        }
		Connection conn = null;
		try {
			conn = getConnection();
				

            CreateCodeReviewAssignment.assignReviewersOfStudentCode(assignment, lastSubmissionMap, students, 1, codeReviewers,
                    true, conn);
		
			String redirectUrl = request.getContextPath()
					+ "/view/instructor/codeReviewAssignment.jsp?codeReviewAssignmentPK="
					+ assignment.getCodeReviewAssignmentPK();
			response.sendRedirect(redirectUrl);
		} catch (SQLException e) {
			handleSQLException(e);
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
	}
   

 
}

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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.CodeReviewAssignment;
import edu.umd.cs.marmoset.modelClasses.CodeReviewer;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Rubric;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.RequestParser;


public class UpdateCodeReviewAssignment extends SubmitServerServlet {

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
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Project project = (Project) request.getAttribute("project");
		RequestParser parser = new RequestParser(request,
                getSubmitServerServletLog(), strictParameterChecking());

		boolean addReviews = parser.getCheckbox("addReviews");
         
		Connection conn = null;
		try {
			conn = getConnection();
			
			if (addReviews) {
	            
			 Map<Integer, Submission> submissionsThatNeedReview 
             =     (Map<Integer, Submission>) request.getAttribute("submissionsThatNeedReview");
			 for(Submission s :submissionsThatNeedReview.values() ) {
			 StudentRegistration sr = StudentRegistration.lookupByStudentRegistrationPK(s.getStudentRegistrationPK(), conn);
			 System.out.printf("Adding review for submission %s by %s : %s at %tc%n",
			         s.getSubmissionNumber(), sr.getClassAccount(), sr.getFullname(), s.getSubmissionTimestamp());
			 
			 }
			 
			}
			@CodeReviewAssignment.PK int codeReviewAssignmentPK = 
			        CodeReviewAssignment.asPK(parser.getIntParameter("codeReviewAssignmentPK"));
			CodeReviewAssignment assignment = CodeReviewAssignment.lookupByPK(codeReviewAssignmentPK, conn);
			
			if (assignment == null)
			    throw new  ServletException("could not find code review assignment ");
			if (assignment.getProjectPK() != project.getProjectPK())
			    throw new ServletException("project vs code review assignment mismatch");
			
			String description = parser.getStringParameter("description");
			Timestamp deadline = parser.getTimestampParameter("deadline");
			boolean anonymous = parser.getCheckbox("anonymous");
			boolean canSeeOthers = parser.getCheckbox("canSeeOthers");
			 
			assignment.setDescription(description);
			assignment.setDeadline(deadline);
			assignment.setAnonymous(anonymous);
			assignment.setOtherReviewsVisible(canSeeOthers);
			assignment.update(conn);

			if (addReviews) {
			    @Student.PK int reviewer =  Student.asPK(parser.getIntParameter("reviewer"));
			    Map<Integer, Submission> submissionsThatNeedReview 
	             =     (Map<Integer, Submission>) request.getAttribute("submissionsThatNeedReview");
			    for(Submission s : submissionsThatNeedReview.values()) {
			        CodeReviewer.lookupOrInsertAuthor(conn, s, assignment, "");
			        CodeReviewer.updateOrInsert(conn, 
			                assignment.getCodeReviewAssignmentPK(),  s.getSubmissionPK(),
			                reviewer, "",
	                        false, true);
			    }
			}
	        
			Integer rubricCount = parser.getOptionalInteger("rubric-count");
			if (rubricCount != null)
			    for(int i = 1; i <= rubricCount; i++) {
			        String base = "rubric"+i+"-";
			        String name = request.getParameter(base+"name");
			        String presentation = request.getParameter(base+"presentation");
			        String rubricDescription = request.getParameter(base+"description");
			        String data;
			        if (presentation.equals("NUMERIC")) {
			            Integer min = parser.getOptionalInteger(base+"min");
			            Integer max = parser.getOptionalInteger(base+"max");
			            Integer defaultValue = parser.getOptionalInteger(base+"default");
			            LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
			            String first;
			            if (defaultValue == null || nullSafeEquals(min, defaultValue)) {
			                add(map, "min", min);
			                add(map, "max", max);
			            } else if  (nullSafeEquals(max, defaultValue)) {
			                add(map, "max", max);
			                add(map, "min", min);
			            } else {
			                add(map, "default", defaultValue);
			                add(map, "max", max);
                            add(map, "max", max);
			            }
			            data = Rubric.serializeMapToData(map);		            
			        } else if (presentation.equals("CHECKBOX")) {
			            Integer unchecked = parser.getOptionalInteger(base+"false");
                        Integer checked = parser.getOptionalInteger(base+"true");
                        data = "false:" + unchecked + ", true:" + checked;
                       
			        } else {
			            data = request.getParameter(base+"value");
			        }
			        new Rubric(conn, assignment, name, rubricDescription, presentation, data);
		    }


		
			String redirectUrl = request.getContextPath()
					+ "/view/instructor/codeReviewAssignment.jsp?codeReviewAssignmentPK="
					+ assignment.getCodeReviewAssignmentPK();
			response.sendRedirect(redirectUrl);
		} catch (InvalidRequiredParameterException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			handleSQLException(e);
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	/**
	 * @param request
	 * @param deck
	 * @return
	 */
	private HashSet<Integer> instructorReviewers(HttpServletRequest request,
			ArrayList<Integer> deck) {
		HashSet<Integer> instructorsInDeck = new HashSet<Integer>();
		for(Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ) {
			String p = e.nextElement();
			if (p.startsWith("reviewer-")) {
				@Student.PK int studentPK = Integer.parseInt(p.substring(9));
				deck.add(studentPK);
				instructorsInDeck.add(studentPK);
			}
		}
		return instructorsInDeck;
	}

}

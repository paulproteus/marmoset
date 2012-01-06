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
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

/**
 * @author jspacco
 *
 */
public class CreateCodeReviewAssignment extends SubmitServerServlet {

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

		Collection<StudentRegistration> students = (Collection<StudentRegistration>) request.getAttribute(JUST_STUDENT_REGISTRATION_SET);
		Collection<StudentRegistration> studentsWithoutSubmissions = (Collection<StudentRegistration>) request.getAttribute("studentsWithoutSubmissions");

		Connection conn = null;
		try {
			conn = getConnection();
			RequestParser parser = new RequestParser(request,
					getSubmitServerServletLog(), strictParameterChecking());

			String description = parser.getStringParameter("description");
			Timestamp deadline = parser.getTimestampParameter("deadline");
			boolean anonymous = parser.getCheckbox("anonymous");
			boolean canSeeOthers = parser.getCheckbox("canSeeOthers");

			CodeReviewAssignment assignment =
				new CodeReviewAssignment(conn, project.getProjectPK(), description,
						deadline, canSeeOthers, anonymous);
			addRubrics(request, parser, assignment, conn);


			String of = parser.getStringParameter("of");
            int numReviewersPerSubmission = parser.getIntParameter("numReviewers");
			boolean studenrReviewers = parser.getCheckbox("studentReviewers");
            ArrayList<Integer> codeReviewers = instructorReviewers(request);
            HashSet<Integer> instructorsInDeck = new HashSet<Integer>(codeReviewers);

            if (studenrReviewers) {
                for (StudentRegistration studentRegistration : students)
                    codeReviewers.add(studentRegistration.getStudentPK());
            }

            if (of.equals("all")) {
                Map<Integer, Submission> lastSubmissionMap = (Map<Integer, Submission>) request.getAttribute("lastSubmission");

				assignReviewersOfStudentCode(assignment, lastSubmissionMap, students, numReviewersPerSubmission, codeReviewers,
                        instructorsInDeck, conn);
			} else {
				@Submission.PK int ofPK = Submission.asPK(Integer.parseInt(of));
				Submission submission = Submission.lookupBySubmissionPK(ofPK, conn);
		        
				reviewOneSubmission(assignment, submission, students, studentsWithoutSubmissions, studenrReviewers, codeReviewers, conn);
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
     * @param parser
     * @param assignment
     * @param conn
     * @throws InvalidRequiredParameterException
     * @throws SQLException
     */
    public void addRubrics(HttpServletRequest request, RequestParser parser, CodeReviewAssignment assignment, Connection conn)
            throws InvalidRequiredParameterException, SQLException {
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
                    int unchecked = parser.getIntParameter(base+"false");
                    int checked = parser.getIntParameter(base+"true");
                    data = "false:" + unchecked + ", true:" + checked;
                } else {
                    data = request.getParameter(base+"value");
                }
                new Rubric(conn, assignment, name, rubricDescription, presentation, data);
        }
    }

    public void reviewOneSubmission(CodeReviewAssignment assignment, Submission submission, 
            Collection<StudentRegistration> students,
            Collection<StudentRegistration> studentsWithoutSubmissions, 
            boolean studentReviewers,
            ArrayList<Integer> codeReviewers, Connection conn) throws SQLException {
         StudentRegistration author = StudentRegistration.lookupByStudentRegistrationPK(
        		submission.getStudentRegistrationPK(),
        		conn);
        @Student.PK int authorPK = author.getStudentPK();
        CodeReviewer.lookupOrInsertAuthor(conn, submission, assignment, "");

        for(@Student.PK int reviewerPK : codeReviewers) {
        	if ( authorPK != reviewerPK)
        		CodeReviewer.updateOrInsert(conn, assignment.getCodeReviewAssignmentPK(),  submission.getSubmissionPK(),
        				reviewerPK,"",
        				false, true);
        }

        if (studentReviewers) {
        	// Add student reviewers

        	students.addAll(studentsWithoutSubmissions);
        	if (assignment.isAnonymous()) {
        		List<StudentRegistration> shuffled = new ArrayList<StudentRegistration>(students);
        		Collections.shuffle(shuffled);
        		students = shuffled;
        	}

        	int reviewerNumber = 1;
        	for(StudentRegistration studentRegistration : students) {
        		String knownAs = "";
        		if (assignment.isAnonymous())
        			knownAs = "Reviewer " + reviewerNumber++;
        		@Student.PK int reviewerPK = studentRegistration.getStudentPK();
        		if ( authorPK != reviewerPK)
        			CodeReviewer.updateOrInsert(conn, assignment.getCodeReviewAssignmentPK(),  submission.getSubmissionPK(),
        					reviewerPK,knownAs,
        					false, false);
        	}

        }
    }

    public static void assignReviewersOfStudentCode( CodeReviewAssignment assignment, Map<Integer, Submission> submissionMap,
            Collection<StudentRegistration> students, int numReviewersPerSubmission, List<Integer> codeReviewers,
            Collection<Integer> instructorsInDeck, Connection conn) throws SQLException {
        // reviewing all student submissions               

        if (codeReviewers.size() == 0)
            throw new IllegalArgumentException("no reviewers");
        ArrayList<Integer> roster = new ArrayList<Integer>();
        Random r = new Random();
        ArrayList<StudentRegistration> toReview = new ArrayList<StudentRegistration>(students);
        Collections.shuffle(toReview, r);

        int authorNum = 1;
        for(StudentRegistration sr : toReview) {
            if (!sr.isNormalStudent())
                continue;
            @Student.PK int authorPK = sr.getStudentPK();
            Submission submission = submissionMap.get(sr.getStudentRegistrationPK());
            if (submission == null)
                continue;
            HashSet<Integer> reviewers = new HashSet<Integer>();
            reviewers.add(authorPK);
            String authorName = "";
            if (assignment.isAnonymous()) {
                authorName = "Author " + (authorNum++);
            }
            int pos = 0;
            if (codeReviewers.size() == 1 && codeReviewers.get(0).intValue() == authorPK)
                continue;

            // Ensure we have enough people in the roster
            while (roster.size()  < numReviewersPerSubmission + 2) {
                Collections.shuffle(codeReviewers, r);
                roster.addAll(codeReviewers);
            }
            CodeReviewer.lookupOrInsertAuthor(conn, submission, assignment, authorName);
            for(int i = 0; i  < numReviewersPerSubmission; i++) {
                while (pos < roster.size() && reviewers.contains(roster.get(pos)))
                    pos++;
                if (pos >= roster.size()) {
                    System.out.printf("Can only find %d of %d reviewers%n", reviewers.size(), numReviewersPerSubmission);
                    System.out.println("reviewers are: " + reviewers);
                    break;
                }
                @Student.PK int reviewerPK = roster.get(pos);
                reviewers.add(reviewerPK);
                roster.remove(pos);
                String knownAs = "";
                if (assignment.isAnonymous())
                    knownAs = "Reviewer " + (i+1);


                CodeReviewer.updateOrInsert(conn, assignment.getCodeReviewAssignmentPK(),  submission.getSubmissionPK(),
                        reviewerPK, knownAs,
                        false, instructorsInDeck.contains(reviewerPK));
            }
        }
    }


	static ArrayList<Integer> instructorReviewers(HttpServletRequest request) {
		ArrayList<Integer> deck = new ArrayList<Integer>();
		for(Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ) {
			String p = e.nextElement();
			if (p.startsWith("reviewer-")) {
				@Student.PK int studentPK = Integer.parseInt(p.substring(9));
				deck.add(studentPK);
				
			}
		}
		return deck;
	}

}

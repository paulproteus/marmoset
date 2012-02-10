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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.CodeReviewAssignment;
import edu.umd.cs.marmoset.modelClasses.CodeReviewAssignment.Kind;
import edu.umd.cs.marmoset.modelClasses.CodeReviewer;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Rubric;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.RequestParser;

public class CreateCodeReviewAssignment extends SubmitServerServlet {
    static boolean nullSafeEquals(Object x, Object y) {
        if (x == y)
            return true;
        if (x == null || y == null)
            return false;
        return x.equals(y);
    }

    static void add(LinkedHashMap<String, Integer> map, String name, @CheckForNull Integer value) {
        if (value == null)
            return;
        map.put(name, value);
    }

    List<Integer> getStudentPKs(Collection<StudentRegistration> students) {
        ArrayList<Integer> codeReviewers = new ArrayList<Integer>();
        for (StudentRegistration studentRegistration : students)
            codeReviewers.add(studentRegistration.getStudentPK());
        return codeReviewers;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Project project = (Project) request.getAttribute("project");

        Collection<StudentRegistration> students = (Collection<StudentRegistration>) request
                .getAttribute(JUST_STUDENT_REGISTRATION_SET);
       
        RequestParser parser = new RequestParser(request, getSubmitServerServletLog(), strictParameterChecking());

        Kind kind = Kind.getByParamValue(parser.getStringParameter("kind"));
        String description = parser.getStringParameter("description");
        Timestamp deadline = parser.getTimestampParameter("deadline");
        boolean anonymous = parser.getCheckbox("anonymous");
        boolean canSeeOthers = parser.getCheckbox("canSeeOthers");
       
        if (kind == Kind.PEER && parser.getBooleanParameter("peerBySection", false))
            kind = Kind.PEERBYSECTION;
        
        Connection conn = null;
        try {
            conn = getConnection();
             		
            CodeReviewAssignment assignment = new CodeReviewAssignment(conn, project.getProjectPK(), description, deadline,
                    canSeeOthers, anonymous, kind);
            addRubrics(request, parser, assignment, conn);

            switch (kind) {
            case INSTRUCTIONAL: {
                ArrayList<Integer> instructorReviewers = instructorReviewers(request);
                Map<Integer, Submission> lastSubmissionMap = (Map<Integer, Submission>) request.getAttribute("lastSubmission");

                assignReviewersOfStudentCode(assignment, lastSubmissionMap, students, 1, instructorReviewers,
                        true, conn);
                break;
            }
            case INSTRUCTIONALBYSECTION: {
                SortedSet<String> sections = (SortedSet<String>) request.getAttribute(SECTIONS);
                SortedMap<String, SortedSet<StudentRegistration>> sectionMap = (SortedMap<String, SortedSet<StudentRegistration>>) request
                        .getAttribute(SECTION_MAP);
                Map<Integer, Submission> lastSubmissionMap = (Map<Integer, Submission>) request.getAttribute("lastSubmission");
                for (String section : sections) {
                    @Student.PK int sectionReviewerPK = 
                            Student.asPK(parser.getIntParameter("section-reviewer-" + section));
                    System.out.println("Reviewer for section " + section + " is " + sectionReviewerPK);
                   assignReviewerOfStudentCode(assignment, lastSubmissionMap, sectionMap.get(section), 
                           sectionReviewerPK, conn);
                }
                break;
            }
            case PEERBYSECTION: {
                
                int numReviewersPerSubmission = parser.getIntParameter("numReviewers");
                Map<Integer, Submission> lastSubmissionMap = (Map<Integer, Submission>) request.getAttribute("lastSubmission");
                TreeMap<String, SortedSet<StudentRegistration>> sectionMap =
                       (TreeMap<String, SortedSet<StudentRegistration>>) request.getAttribute(SECTION_MAP);
                
                for(SortedSet<StudentRegistration> section : sectionMap.values()) {
                    List<Integer> codeReviewers = getStudentPKs(section);
                    assignReviewersOfStudentCode(assignment, lastSubmissionMap, students, numReviewersPerSubmission, codeReviewers,
                            false, conn);
                }
                break;
            }
                
            case PEER: {
                int numReviewersPerSubmission = parser.getIntParameter("numReviewers");
                Map<Integer, Submission> lastSubmissionMap = (Map<Integer, Submission>) request.getAttribute("lastSubmission");
                List<Integer> codeReviewers = getStudentPKs(students);
                assignReviewersOfStudentCode(assignment, lastSubmissionMap, students, numReviewersPerSubmission, codeReviewers,
                        false, conn);
                break;
            }
            case EXEMPLAR: {
                String of = parser.getStringParameter("of");
                @Submission.PK
                int ofPK = Submission.asPK(Integer.parseInt(of));
                Submission submission = Submission.lookupBySubmissionPK(ofPK, conn);
                Collection<StudentRegistration> studentsWithoutSubmissions = (Collection<StudentRegistration>) request
                        .getAttribute("studentsWithoutSubmissions");
                students.addAll(studentsWithoutSubmissions);
                reviewOneSubmission(assignment, submission, students, conn);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown code review kind: " + kind);
            }

            String redirectUrl = request.getContextPath() + "/view/instructor/codeReviewAssignment.jsp?codeReviewAssignmentPK="
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
            for (int i = 1; i <= rubricCount; i++) {
                String base = String.format("rubric-%d-", i);
                String name = request.getParameter(base + "name");
                String presentation = request.getParameter(base + "presentation");
                String rubricDescription = request.getParameter(base + "description");
                String data;
                if (presentation.equals("NUMERIC")) {
                    Integer min = parser.getOptionalInteger(base + "min");
                    Integer max = parser.getOptionalInteger(base + "max");
                    Integer defaultValue = parser.getOptionalInteger(base + "default");
                    LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
                    String first;
                    if (defaultValue == null || nullSafeEquals(min, defaultValue)) {
                        add(map, "min", min);
                        add(map, "max", max);
                    } else if (nullSafeEquals(max, defaultValue)) {
                        add(map, "max", max);
                        add(map, "min", min);
                    } else {
                        add(map, "default", defaultValue);
                        add(map, "max", max);
                        add(map, "max", max);
                    }
                    data = Rubric.serializeMapToData(map);
                } else if (presentation.equals("CHECKBOX")) {
                    int unchecked = parser.getIntParameter(base + "false");
                    int checked = parser.getIntParameter(base + "true");
                    data = "false:" + unchecked + ", true:" + checked;
                } else {
                    data = request.getParameter(base + "value");
                }
                new Rubric(conn, assignment, name, rubricDescription, presentation, data);
            }
    }

    public void reviewOneSubmission(CodeReviewAssignment assignment, Submission submission,
            Collection<StudentRegistration> students, Connection conn) throws SQLException {
        StudentRegistration author = StudentRegistration.lookupByStudentRegistrationPK(submission.getStudentRegistrationPK(),
                conn);
        @Student.PK
        int authorPK = author.getStudentPK();
        CodeReviewer.lookupOrInsertAuthor(conn, submission, assignment, "");

        if (assignment.isAnonymous()) {
            List<StudentRegistration> shuffled = new ArrayList<StudentRegistration>(students);
            Collections.shuffle(shuffled);
            students = shuffled;
        }

        int reviewerNumber = 1;
        for (StudentRegistration studentRegistration : students) {
            String knownAs = "";
            if (assignment.isAnonymous())
                knownAs = "Reviewer " + reviewerNumber++;
            @Student.PK
            int reviewerPK = studentRegistration.getStudentPK();
            if (authorPK != reviewerPK)
                CodeReviewer.updateOrInsert(conn, assignment.getCodeReviewAssignmentPK(), submission.getSubmissionPK(),
                        reviewerPK, knownAs, false, false);
        }
        
    }

    public static void assignReviewerOfStudentCode(CodeReviewAssignment assignment, Map<Integer, Submission> submissionMap,
            Collection<StudentRegistration> students, @Student.PK int reviewerStudentPK, Connection conn) throws SQLException {

       for (StudentRegistration sr : students) {
            if (!sr.isNormalStudent())
                continue;
            @Student.PK
            int authorPK = sr.getStudentPK();
            Submission submission = submissionMap.get(sr.getStudentRegistrationPK());
            if (submission == null)
                continue;

            CodeReviewer.lookupOrInsertAuthor(conn, submission, assignment, "");

            CodeReviewer.updateOrInsert(conn, assignment.getCodeReviewAssignmentPK(), submission.getSubmissionPK(), reviewerStudentPK,
                    "", false, true);
        }
    }

    public static void assignReviewersOfStudentCode(CodeReviewAssignment assignment, Map<Integer, Submission> submissionMap,
            Collection<StudentRegistration> students, int numReviewersPerSubmission, List<Integer> codeReviewers,
            boolean instructionalReview, Connection conn) throws SQLException {
        // reviewing all student submissions

        if (codeReviewers.size() == 0)
            throw new IllegalArgumentException("no reviewers");
        
        ArrayList<Integer> roster = new ArrayList<Integer>();
        Random r = new Random();
        ArrayList<StudentRegistration> toReview = new ArrayList<StudentRegistration>(students);
        Collections.shuffle(toReview, r);

        int authorNum = 1;
        for (StudentRegistration sr : toReview) {
            if (!sr.isNormalStudent())
                continue;
            @Student.PK
            int authorPK = sr.getStudentPK();
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
            while (roster.size() < numReviewersPerSubmission + 2) {
                Collections.shuffle(codeReviewers, r);
                roster.addAll(codeReviewers);
            }
            CodeReviewer.lookupOrInsertAuthor(conn, submission, assignment, authorName);
            for (int i = 0; i < numReviewersPerSubmission; i++) {
                while (pos < roster.size() && reviewers.contains(roster.get(pos)))
                    pos++;
                if (pos >= roster.size()) {
                    System.out.printf("Can only find %d of %d reviewers%n", reviewers.size(), numReviewersPerSubmission);
                    System.out.println("reviewers are: " + reviewers);
                    break;
                }
                @Student.PK
                int reviewerPK = Student.asPK(roster.get(pos));
                reviewers.add(reviewerPK);
                roster.remove(pos);
                String knownAs = "";
                if (assignment.isAnonymous())
                    knownAs = "Reviewer " + (i + 1);

                CodeReviewer.updateOrInsert(conn, assignment.getCodeReviewAssignmentPK(), submission.getSubmissionPK(),
                        reviewerPK, knownAs, false, instructionalReview);
            }
        }
    }

    static ArrayList<Integer> instructorReviewers(HttpServletRequest request) {
        ArrayList<Integer> deck = new ArrayList<Integer>();
        for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
            String p = e.nextElement();
            if (p.startsWith("reviewer-")) {
                @Student.PK
                int studentPK = Integer.parseInt(p.substring(9));
                deck.add(studentPK);

            }
        }
        return deck;
    }

}

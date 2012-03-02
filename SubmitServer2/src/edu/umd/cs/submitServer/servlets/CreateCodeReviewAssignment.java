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
import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedHashMap;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import edu.umd.cs.marmoset.modelClasses.CodeReviewAssignment;
import edu.umd.cs.marmoset.modelClasses.CodeReviewAssignment.Kind;
import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Rubric;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.RequestParser;

public class CreateCodeReviewAssignment extends SubmitServerServlet {
    private static boolean nullSafeEquals(Object x, Object y) {
        if (x == y)
            return true;
        if (x == null || y == null)
            return false;
        return x.equals(y);
    }

    private static void add(LinkedHashMap<String, Integer> map, String name,
            @CheckForNull Integer value) {
        if (value == null)
            return;
        map.put(name, value);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Project project = (Project) request.getAttribute(PROJECT);
        Course course = (Course) request.getAttribute(COURSE);

        RequestParser parser = new RequestParser(request,
                getSubmitServerServletLog(), strictParameterChecking());

        StudentRegistration instructor = (StudentRegistration) request
                .getAttribute(STUDENT_REGISTRATION);
        Student user = (Student) request.getAttribute(USER);
        if (user.getStudentPK() != user.getStudentPK())
            throw new IllegalArgumentException(
                    "Instructor and user don't match");
        if (project.getCoursePK() != instructor.getCoursePK())
            throw new IllegalArgumentException(
                    "Instructor and project don't match");
        if (!instructor.isInstructorModifiy())
            throw new IllegalArgumentException("not instructor");

        String description = parser.getStringParameter("description");
        Timestamp deadline = parser.getTimestampParameter("deadline");
        boolean anonymous = parser.getCheckbox("anonymous");
        boolean canSeeOthers = parser.getCheckbox("canSeeOthers");
       
        Connection conn = null;
        try {
            conn = getConnection();

            CodeReviewAssignment assignment
             = (CodeReviewAssignment) request.getAttribute(CODE_REVIEW_ASSIGNMENT);

             
            if (assignment != null) {
                 if (!assignment.isPrototype())
                    throw new IllegalArgumentException(
                            "Can only update prototype code reviews");

                assignment.setDescription(description);
                assignment.setDeadline(deadline);
                assignment.setAnonymous(anonymous);
                assignment.setOtherReviewsVisible(canSeeOthers);
                assignment.update(conn);
            } else {
                Kind kind = Kind.getByParamValue(parser.getStringParameter("kind"));
                if (!kind.isPrototype())
                    throw new IllegalArgumentException(
                            "Can only create prototype code reviews");
           
                assignment = new CodeReviewAssignment(conn,
                        project.getProjectPK(), description, deadline,
                        canSeeOthers, anonymous, kind);
                String of = parser.getStringParameter("of");
                @Submission.PK
                int ofPK = Submission.asPK(Integer.parseInt(of));
                Submission submission = Submission.lookupBySubmissionPK(ofPK,
                        conn);
                StudentRegistration reviewer;
                switch (kind) {
                case PEER_PROTOTYPE:
                    reviewer = StudentAccountForInstructor
                            .createOrFindPseudoStudentRegistration(conn,
                                    course, instructor, user);
                    break;
                case INSTRUCTIONAL_PROTOTYPE:
                    reviewer = instructor;
                    break;
                default:
                    throw new AssertionError();
                }
                AssignCodeReviews.reviewOneSubmission(assignment, submission,
                        Collections.singleton(reviewer), conn);
            }
            addRubrics(request, parser, assignment, conn);

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

    public void addRubrics(HttpServletRequest request, RequestParser parser,
            CodeReviewAssignment assignment, Connection conn)
            throws InvalidRequiredParameterException, SQLException {
        Integer rubricCount = parser.getOptionalInteger("rubric-count");
        if (rubricCount != null)
            for (int i = 1; i <= rubricCount; i++) {
                String base = String.format("rubric-%d-", i);
                String name = request.getParameter(base + "name");
                String pk = request.getParameter(base + "pk");
                String presentation = request.getParameter(base
                        + "presentation");
                String rubricDescription = request.getParameter(base
                        + "description");
                if (Strings.isNullOrEmpty(name)
                        || Strings.isNullOrEmpty(presentation))
                    continue;
                String data;
                if (presentation.equals("NUMERIC")) {
                    Integer min = parser.getOptionalInteger(base + "min");
                    Integer max = parser.getOptionalInteger(base + "max");
                    Integer defaultValue = parser.getOptionalInteger(base
                            + "default");
                    LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
                    String first;
                    if (defaultValue == null
                            || nullSafeEquals(min, defaultValue)) {
                        add(map, "min", min);
                        add(map, "max", max);
                    } else if (nullSafeEquals(max, defaultValue)) {
                        add(map, "max", max);
                        add(map, "min", min);
                    } else {
                        add(map, "default", defaultValue);
                        add(map, "max", max);
                        add(map, "min", min);
                    }
                    data = Rubric.serializeMapToData(map);
                } else if (presentation.equals("CHECKBOX")) {
                    int unchecked = parser.getIntParameter(base + "false");
                    int checked = parser.getIntParameter(base + "true");
                    data = "false:" + unchecked + ", true:" + checked;
                } else {
                    data = request.getParameter(base + "value");
                }
                if (Strings.isNullOrEmpty(pk)) {
                  new Rubric(conn, assignment, name, rubricDescription,
                          presentation, data);
                } else {
                int rubricPK = Integer.parseInt(pk);
                  Rubric rubric = Rubric.lookupByPK(Rubric.asPK(rubricPK), conn);
                  Preconditions.checkNotNull(rubric);
                  if ("true".equals(request.getParameter(base + "delete"))) {
                    rubric.setCodeReviewAssignmentPK(0);
                  }
                  rubric.setData(data);
                  rubric.setName(name);
                  rubric.setDescription(rubricDescription);
                  rubric.update(conn);
                }
            }
    }

}

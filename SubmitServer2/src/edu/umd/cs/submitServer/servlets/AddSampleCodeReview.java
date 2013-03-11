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
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.CodeReviewAssignment;
import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.RequestParser;

public class AddSampleCodeReview extends SubmitServerServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Project project = (Project) request.getAttribute(PROJECT);
    Course course = (Course) request.getAttribute(COURSE);

    RequestParser parser = new RequestParser(request, getSubmitServerServletLog(), strictParameterChecking());

    StudentRegistration instructor = (StudentRegistration) request.getAttribute(STUDENT_REGISTRATION);
    Student user = (Student) request.getAttribute(USER);
    if (user.getStudentPK() != user.getStudentPK())
      throw new IllegalArgumentException("Instructor and user don't match");
    if (project.getCoursePK() != instructor.getCoursePK())
      throw new IllegalArgumentException("Instructor and project don't match");
    if (!instructor.isInstructorModifiy())
      throw new IllegalArgumentException("not instructor");

    Connection conn = null;
    boolean transactionSuccess = false;
    try {
      conn = getConnection();
      conn.setAutoCommit(false);
      conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

      CodeReviewAssignment assignment = (CodeReviewAssignment) request.getAttribute(CODE_REVIEW_ASSIGNMENT);

      String of;
      StudentRegistration reviewer;

      switch (assignment.getKind()) {
      case PEER_PROTOTYPE:
        of = parser.getStringParameter("of-peer");

        reviewer = StudentAccountForInstructor.createOrFindPseudoStudentRegistration(conn, course, instructor, user);
        break;
      case INSTRUCTIONAL_PROTOTYPE:
        of = parser.getStringParameter("of-instructional");

        reviewer = instructor;
        break;
      default:
        throw new AssertionError();
      }

      @Submission.PK
      int ofPK = Submission.asPK(Integer.parseInt(of));
      Submission submission = Submission.lookupBySubmissionPK(ofPK, conn);
      StudentRegistration author = StudentRegistration.lookupBySubmissionPK(ofPK, conn);

      if (reviewer.getStudentPK() == author.getStudentPK())
        throw new IllegalArgumentException("Reviewer and author for prototype code review must be distinct");

      AssignCodeReviews.reviewOneSubmission(assignment, submission, Collections.singleton(reviewer), conn);
      conn.commit();
      transactionSuccess = true;
      
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
      rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess, request, conn);
    }
  }

}

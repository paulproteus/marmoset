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
import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.submitServer.RequestParser;

public class ChangeCodeReviewAssignmentVisibility extends SubmitServerServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Project project = (Project) request.getAttribute(PROJECT);
    Course course = (Course) request.getAttribute(COURSE);

    RequestParser parser = new RequestParser(request, getSubmitServerServletLog(), strictParameterChecking());

    StudentRegistration instructor = (StudentRegistration) request.getAttribute(STUDENT_REGISTRATION);
    CodeReviewAssignment assignment = (CodeReviewAssignment) request.getAttribute(CODE_REVIEW_ASSIGNMENT);
    
    if (!instructor.isInstructorModifiy()) throw new IllegalArgumentException();
    boolean visibleToStudents = parser.getBooleanParameter("visibleToStudents");
    Connection conn = null;
    try {
      conn = getConnection();
      assignment.setVisibleToStudents(visibleToStudents);
      assignment.update(conn);
           
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

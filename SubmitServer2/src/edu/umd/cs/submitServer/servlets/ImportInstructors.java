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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.submitServer.WebConfigProperties;

public class ImportInstructors extends GradeServerInterfaceServlet {
  private static final WebConfigProperties webProperties = WebConfigProperties.get();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    Connection gradesConn = null;
    Connection conn = null;
    boolean transactionSuccess = false;
    String term = request.getParameter("semester");
    if (term == null)
      term = webProperties.getRequiredProperty("semester");
    response.setContentType("text/plain");
    try {
      gradesConn = getGradesConnection();
      conn = getConnection();
      HashSet<String> allUIDs = new HashSet<String>();
      for (Student instructor : Student.lookupAllInstructors(conn).values()) {
        allUIDs.add(instructor.getCampusUID());
      }

      {
        String query = "SELECT DISTINCT lastName, firstName, nickname, uid, directoryID" + " FROM submitexportstaff "
            + " WHERE term = ? AND role = ? ";
        PreparedStatement stmt = gradesConn.prepareStatement(query);
        stmt.setString(1, term);
        stmt.setString(2, "Instructor");
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
          Student s = new Student();
          int col = 1;
          s.setLastname(rs.getString(col++));
          String firstname = rs.getString(col++);
          String nickname = rs.getString(col++);
          firstname = ImportCourse.getEffectiveFirstname(firstname, nickname);
          s.setFirstname(firstname);
          String uid = rs.getString(col++);
          allUIDs.add(uid);
          s.setCampusUID(uid);
          s.setLoginName(rs.getString(col++));
          s.setCanImportCourses(true);
          boolean added = s == s.insertOrUpdate(conn);
          if (added)
            response.getWriter().printf("Added %s %s%n", s.getFirstname(), s.getLastname());
          else
            response.getWriter().printf("Updated %s %s%n", s.getFirstname(), s.getLastname());
        }
        rs.close();
        stmt.close();
      }

      {
        String query = "SELECT DISTINCT uid, lastName, firstName, nickname, directoryID" + " FROM submitexportcourses "
            + " WHERE term = ?";
        PreparedStatement stmt = gradesConn.prepareStatement(query);
        stmt.setString(1, term);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
          int col = 1;
          String uid = rs.getString(col++);
          if (!allUIDs.add(uid))
            continue;
          String lastName = rs.getString(col++);
          String firstname = rs.getString(col++);
          String nickname = rs.getString(col++);
          String loginName = rs.getString(col++);
          if (firstname == null || lastName == null || loginName == null)
            continue;
          Student s = new Student();

          s.setCampusUID(uid);
          s.setLastname(lastName);

          firstname = ImportCourse.getEffectiveFirstname(firstname, nickname);
          s.setFirstname(firstname);
          s.setLoginName(loginName);

          s.setCanImportCourses(true);

          boolean added = s == s.insertOrUpdate(conn);
          if (added)
            response.getWriter().printf("Added %s %s (not in grades server)%n", s.getFirstname(), s.getLastname());
          else
            response.getWriter()
                .printf("Updated %s %s (should not have happened)%n", s.getFirstname(), s.getLastname());
        }
        transactionSuccess = true;
        rs.close();
        stmt.close();
      }

    } catch (SQLException e) {
      throw new ServletException(e);
    } finally {
      rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess, request, conn);
      releaseGradesConnection(gradesConn);

    }

  }
}

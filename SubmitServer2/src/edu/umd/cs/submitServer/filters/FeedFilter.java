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
 * Created on Jan 19, 2005
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.submitServer.RequestParser;


public class FeedFilter extends SubmitServerFilter {
  
  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
      ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) resp;
    RequestParser parser = new RequestParser(request, getSubmitServerFilterLog(), strictParameterChecking());
    Integer projectPK = parser.getIntegerParameter(PROJECT_PK, null);
    Integer coursePK = parser.getIntegerParameter(COURSE_PK, null);

    Project project = null;
    Course course = null;
   
    Connection conn = null;
    try {
      conn = getConnection();
      if (projectPK != null) {
        project = Project.lookupByProjectPK(projectPK, conn);
        if (coursePK == null)
          coursePK = project.getCoursePK();
        else if ( !coursePK.equals(project.getCoursePK()))
          throw new IllegalArgumentException("Course and project PK don't match");
        request.setAttribute(COURSE, course);
      }
      if (coursePK != null) {
        course = Course.lookupByCoursePK(coursePK, conn);
        request.setAttribute(COURSE, course);
      }
      List<Project> projectList = Project.lookupAllByCoursePK(coursePK, conn);
      Collections.reverse(projectList);
      request.setAttribute(PROJECT_LIST, projectList);
      
    } catch (SQLException e) {
      throw new ServletException(e);
    } finally {
      releaseConnection(conn);
    }

    chain.doFilter(request, response);
  }

 
}

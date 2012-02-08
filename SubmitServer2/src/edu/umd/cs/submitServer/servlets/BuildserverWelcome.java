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
 * Created on Jan 15, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.submitServer.MultipartRequest;

/**
 * @author jspacco
 * 
 */
public class BuildserverWelcome extends SubmitServerServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new ServletException();

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Connection conn = null;
        MultipartRequest multipartRequest = (MultipartRequest) request.getAttribute(MULTIPART_REQUEST);

        String courses = multipartRequest.getStringParameter("courses");
        try {
            conn = getConnection();
            Map<String, Course> allowedCourses = Course.lookupAllByBuildserverKey(conn, courses);

            response.setContentType("text/plain");

            PrintWriter out = response.getWriter();

            for (Map.Entry<String, Course> e : allowedCourses.entrySet()) {
                if (e.getValue() == null) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no course for course key  " + e.getKey());
                    return;
                }
                out.printf("Course key %s%n", e.getKey());
                Course c = e.getValue();
                out.printf("  %s%n", c.getFullDescription());
            }

            out.close();
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
    }

}

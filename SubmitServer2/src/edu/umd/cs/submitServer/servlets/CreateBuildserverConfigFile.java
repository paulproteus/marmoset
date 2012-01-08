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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.submitServer.UserSession;

public class CreateBuildserverConfigFile extends SubmitServerServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String coursePKs[] = request.getParameterValues("buildserverCourse");
        Connection conn = null;
        HttpSession session = request.getSession();
        UserSession userSession = (UserSession) session.getAttribute(USER_SESSION);
        Student user = (Student) request.getAttribute(USER);
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        out.printf("# Buildservr config file%n");
        out.printf("# Generated for %s%n", user.getFullname());
        out.printf("# At %tc%n", new Date());
        out.println();
        print(out, "submitURL",
                request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() 
                + request.getContextPath()
                        );

        out.println();
        StringBuilder b = new StringBuilder();

        try {
            conn = getConnection();

            for (String coursePK : coursePKs) {
                int pk = Integer.parseInt(coursePK);
                if (!userSession.hasInstructorActionCapability(pk)) {
                    response.setStatus(403);
                    return;
                }
                Course c = Course.lookupByCoursePK(pk, conn);
                out.printf("# %s%n", c.getFullName());
                out.printf("#  key %s%n", c.getBuildserverKey());
                out.println();
                if (b.length() > 0)
                    b.append(",");
                b.append(c.getBuildserverKey());

            }
        } catch (SQLException e) {
            throw new ServletException(e);

        } finally {
            releaseConnection(conn);

        }
        print(out, "supportedCourses", b.toString());

        out.close();
    }

    private void print(PrintWriter out, String key, String value) {
        if (value == null || value.length() == 0)
            return;
        out.println(key + "=" + value);
    }

}

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

import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentPicture;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;

public class ViewPicture extends SubmitServerServlet {

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

        Connection conn = null;

        try {
            conn = getConnection();

            StudentRegistration sr = (StudentRegistration) request.getAttribute(STUDENT_REGISTRATION);
            Student student = (Student) request.getAttribute(STUDENT);
            @Student.PK int pk = -1;
            if (sr != null)
                pk = sr.getStudentPK();
            else if (student != null)
                pk = student.getStudentPK();
            
            StudentPicture studentPicture = StudentPicture.lookupByStudentPK(pk, conn);
            if (studentPicture == null) {
                request.getRequestDispatcher("/images/noImageAvailable.png").forward(request, response);
            } else {
                response.setContentType(studentPicture.getType());
                response.setContentLength(studentPicture.getImage().length);
                response.getOutputStream().write(studentPicture.getImage());
                response.getOutputStream().close();
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
	}

}

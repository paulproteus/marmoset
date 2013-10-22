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
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.CodeReviewer;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;

public class TestingGroundServlet extends SubmitServerServlet {


	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");
		
		Connection conn = null;
		try {
		    conn = getConnection();
		    
		   for(StudentRegistration sr :  (Collection<StudentRegistration>) request.getAttribute("studentRegistrationCollection")) {
		     int reviewCount = CodeReviewer.countActiveReviews(sr, conn);
		     out.printf("%3d %s%n", reviewCount, sr.getFullname());
		   }
		}  catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }


	}

}

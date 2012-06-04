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
 * Created on Jan 16, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.ServerError;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.MultipartRequest;
import edu.umd.cs.submitServer.filters.ServletExceptionFilter;
import edu.umd.cs.submitServer.filters.SubmitServerFilter;


public class ReportBuildServerDeath extends SubmitServerServlet {
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		MultipartRequest multipartRequest = (MultipartRequest) request
				.getAttribute(MULTIPART_REQUEST);

		Connection conn = null;
		try {
		    System.out.println(multipartRequest.getParameterNames());
            
			// Get submission pk and the submission
			@Submission.PK
			int submissionPK = Submission.asPK(multipartRequest.getIntParameter("submissionPK"));
		
			// Get the testSetupPK
			int testSetupPK = multipartRequest.getIntParameter("testSetupPK");
			String kind =  multipartRequest.getStringParameter("kind");
			String load = multipartRequest.getOptionalStringParameter("load");
			if (load == null)
				load = "unknown";
			 String testMachine = multipartRequest
	                    .getOptionalStringParameter("testMachine");
	            if (testMachine == null)
	                testMachine = "unknown";
	        String remoteHost =  SubmitServerFilter.getRemoteHost(request);
	        String courses = multipartRequest.getStringParameter("courses");
	        
	            
	        
	        String msg = String.format("Build server %s died while testing %s/%s/%s with load %s", testMachine, submissionPK, testSetupPK, kind, load);
	        conn = getConnection();
	        Collection<Integer> allowedCourses = Course.lookupAllPKByBuildserverKey(conn, courses);
            
            if (allowedCourses.isEmpty()) {
                String errorMsg = "host " + testMachine + " from " + remoteHost +"; no courses match " + courses;
                getSubmitServerServletLog().warn(errorMsg);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no courses match " + courses);
                return;
            }

            ServerError.insert(conn, ServerError.Kind.BUILD_SERVER, 
                    null,null,null,null,
                            submissionPK, "", msg,
                            "", "", "", "", remoteHost, "", "", null);
            response.setStatus(200);
            response.setContentType("text/plain");
            response.getWriter().println("OK");
            
			
		} catch (InvalidRequiredParameterException e) {
		    ServletExceptionFilter.logError(conn, ServerError.Kind.EXCEPTION, request, this.getClass().getSimpleName(), null, e) ;
		} catch (SQLException e) {
	        ServletExceptionFilter.logError(conn, ServerError.Kind.EXCEPTION, request, this.getClass().getSimpleName(), null, e) ;
		} finally {
		    releaseConnection(conn);
		}
	}
}

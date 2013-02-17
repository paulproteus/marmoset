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

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.TestSetup;
import edu.umd.cs.submitServer.IOUtilities;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.MultipartRequest;

/**
 * @author jspacco
 * 
 */
public class GetTestSetup extends SubmitServerServlet {

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to
	 * post.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Connection conn = null;
		try {
			conn = getConnection();

			MultipartRequest multipartRequest = (MultipartRequest) request
					.getAttribute(MULTIPART_REQUEST);
			String hostname = multipartRequest.getOptionalStringParameter("hostname");
		       
	        String courses = multipartRequest.getStringParameter("courses");
	        Collection<Integer> allowedCourses = RequestSubmission.getCourses(conn, courses);
	        if (allowedCourses.isEmpty()) {
                String msg = "host " + hostname + "; no courses match " + courses;
                getSubmitServerServletLog().warn(msg);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
                return;
            }
			int testSetupPK = multipartRequest.getIntParameter("testSetupPK");
			TestSetup testSetup = TestSetup.lookupByTestSetupPK(testSetupPK,
					conn);
			 if (testSetup == null) {
	                throw new ServletException("Cannot find testSetup with PK "
	                        + testSetupPK);
	            }
			Project project = Project.lookupByProjectPK(testSetup.getProjectPK(), conn);
			if (!allowedCourses.contains(project.getCoursePK())) {
			        response.sendError(HttpServletResponse.SC_FORBIDDEN);
			        return;
			}
			

			// get the archive in bytes
			byte[] bytes = testSetup.downloadArchive(conn);
			IOUtilities.sendBytesToClient(bytes, response, "application/x-zip");
		} catch (InvalidRequiredParameterException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
	}
}

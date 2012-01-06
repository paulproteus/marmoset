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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.submitServer.IncorrectCourseProjectManagerPluginVersionException;
import edu.umd.cs.submitServer.MultipartRequest;

public class SubmitProjectViaBlueJ extends SubmitServerServlet {

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
		// set by filter
		MultipartRequest multipartRequest = (MultipartRequest) request
				.getAttribute(MULTIPART_REQUEST);

		// it's recommended we use a minimum version of the course project
		// manager client
		String submitClientTool = multipartRequest
				.getOptionalStringParameter("submitClientTool");
		String submitClientVersion = multipartRequest
				.getOptionalStringParameter("submitClientVersion");
		String minimumCPMVersion = getServletContext().getInitParameter(
				"minimum.bluej.to.marmoset.version");
		if (submitClientTool == null || submitClientVersion == null
				|| submitClientTool.equals("BlueJ")
				&& (submitClientVersion.compareTo(minimumCPMVersion) < 0)) {
			throw new ServletException(
					new IncorrectCourseProjectManagerPluginVersionException(
							"Please upgrade to the latest version of the BlueJ-To-Marmoset "
									+ "Submission Extension.  You are using "
									+ submitClientVersion
									+ " and it is recommended that you use at least "
									+ minimumCPMVersion));
		}

		if (true) {
			getSubmitServerServletLog().warn(multipartRequest);
			return;
		}

		request.setAttribute("webBasedUpload", Boolean.FALSE);
		// forward to the UploadSubmission servlet for the heavy lifting
		String uploadSubmission = "/action/UploadSubmission";
		RequestDispatcher dispatcher = request
				.getRequestDispatcher(uploadSubmission);
		dispatcher.forward(request, response);
	}

}

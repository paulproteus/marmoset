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
 * Created on Feb 1, 2005
 */
package edu.umd.cs.submitServer.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import edu.umd.cs.submitServer.MultipartRequest;

/**
 * @author jspacco
 *
 */
public class HandleBuildServerLogMessage extends SubmitServerServlet {
	/** The logger for messages from the buildserver. */
	private static final Logger buildServerLogger
	= Logger.getLogger(HandleBuildServerLogMessage.class);

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
		MultipartRequest multipartRequest = (MultipartRequest) request
				.getAttribute(MULTIPART_REQUEST);
		FileItem fileItem = null;
		LoggingEvent loggingEvent = null;
		ObjectInputStream in = null;
		try {
			fileItem = multipartRequest.getFileItem();
			byte[] data = fileItem.get();

			in = new ObjectInputStream(new ByteArrayInputStream(data));
			loggingEvent = (LoggingEvent) in.readObject();

			buildServerLogger.callAppenders(loggingEvent);
		} catch (ClassNotFoundException e) {
			throw new ServletException("Cannot find class: " + e.getMessage(),
					e);
		} finally {
			if (fileItem != null)
				fileItem.delete();
			if (in != null)
				in.close();
		}
	}


}

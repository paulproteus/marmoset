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
 * Created on Jan 17, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import metadata.identification.FormatDescription;
import metadata.identification.FormatIdentification;

import org.apache.commons.fileupload.FileItem;

import edu.umd.cs.marmoset.modelClasses.IO;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.TestSetup;
import edu.umd.cs.submitServer.MultipartRequest;

/**
 * @author jspacco
 * 
 */
public class UploadTestSetup extends SubmitServerServlet {

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
		// TODO sanity checks on the format of the test setup
		MultipartRequest multipartRequest = (MultipartRequest) request
				.getAttribute(MULTIPART_REQUEST);
		Connection conn = null;
		FileItem fileItem = null;
		boolean transactionSuccess = false;
		try {
			conn = getConnection();

			fileItem = multipartRequest.getFileItem();
			if (fileItem == null)
				throw new ServletException("fileItem is null; this is not good");

			Project project = (Project) request.getAttribute(PROJECT);
			// could be null
			String comment = multipartRequest
					.getOptionalCheckedParameter("comment");

			// get size in bytes
			long sizeInBytes = fileItem.getSize();
			if (sizeInBytes == 0) {
				throw new ServletException("Trying upload file of size 0");
			}

			// copy the fileItem into a byte array
			InputStream is = fileItem.getInputStream();
			ByteArrayOutputStream bytes = new ByteArrayOutputStream(
					(int) sizeInBytes);
			IO.copyStream(is, bytes);

			byte[] byteArray = bytes.toByteArray();

			FormatDescription desc = FormatIdentification.identify(byteArray);
			if (desc == null || !desc.getMimeType().equals("application/zip")) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"You MUST submit test-setups that are either zipped or jarred");
				return;
			}
			// start transaction here
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			TestSetup.submit(byteArray, project, comment, conn);
			conn.commit();
			transactionSuccess = true;

			String redirectUrl = request.getContextPath()
					+ "/view/instructor/projectUtilities.jsp?projectPK="
					+ project.getProjectPK();
			response.sendRedirect(redirectUrl);

		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			rollbackIfUnsuccessfulAndAlwaysReleaseConnection(
					transactionSuccess, request, conn);
			releaseConnection(conn);
			if (fileItem != null)
				fileItem.delete();
		}
	}

}

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
 * Created on Feb 8, 2005
 */
package edu.umd.cs.submitServer.servlets;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import edu.umd.cs.marmoset.modelClasses.IO;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.ZipFileAggregator;
import edu.umd.cs.marmoset.modelClasses.ZipFileAggregator.BadInputZipFileException;

/**
 * @author jspacco
 *
 */
public class RobotDownloadPassingSubmissions extends SubmitServerServlet {

	public boolean isOK(Submission submission, Connection conn)
			throws SQLException {
		if (submission == null)
			return false;
		TestOutcomeCollection outcome = TestOutcomeCollection
				.lookupByTestRunPK(submission.getCurrentTestRunPK(), conn);
		if (outcome == null)
			return false;
		if (!outcome.getPassedAllPublicTests())
			return false;
		if (!outcome.getPassedAllReleaseTests())
			return false;
		return true;
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
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
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Connection conn = null;
		File tempfile = null;
		FileOutputStream fileOutputStream = null;
		FileInputStream fis = null;
		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		PrintWriter names = new PrintWriter(byteOutputStream);
		try {
			conn = getConnection();

			// get the project and all the student registrations
			Map<Integer, Submission> lastSubmissionMap = (Map<Integer, Submission>) request
					.getAttribute("lastSubmission");
			Map<Integer, Submission> bestSubmissionMap = (Map<Integer, Submission>) request
					.getAttribute("bestSubmissionMap");

			Project project = (Project) request.getAttribute("project");
			Set<StudentRegistration> registrationSet = (Set<StudentRegistration>) request
					.getAttribute("studentRegistrationSet");

			// write everything to a tempfile, then send the tempfile
			tempfile = File.createTempFile("temp", "zipfile");
			fileOutputStream = new FileOutputStream(tempfile);

			// zip aggregator
			ZipFileAggregator zipAggregator = new ZipFileAggregator(
					fileOutputStream);

			for (StudentRegistration registration : registrationSet) {
				@StudentRegistration.PK  Integer studentRegistrationPK = registration
						.getStudentRegistrationPK();
				Submission submission = (Submission) lastSubmissionMap
						.get(studentRegistrationPK);
				if (!isOK(submission, conn)) {
					submission = (Submission) bestSubmissionMap
							.get(studentRegistrationPK);
					if (!isOK(submission, conn))
						continue;
				}

				String status = "";
				if (registration.getInstructorLevel() != StudentRegistration.STUDENT_CAPABILITY_LEVEL)
					status = "*";
				String timestamp = submission.getFormattedSubmissionTimestamp();
				names.printf("%s,%s,%s,%s %s%n", registration.getClassAccount(),
						timestamp, status, registration.getFirstname(),
						registration.getLastname());
				try {
					byte[] bytes = submission.downloadArchive(conn);
					zipAggregator.addFileFromBytes(
							registration.getClassAccount(), submission
									.getSubmissionTimestamp().getTime(), bytes);
				} catch (ZipFileAggregator.BadInputZipFileException ignore) {
					// ignore, since students could submit things that aren't
					// zipfiles
					getSubmitServerServletLog().warn(ignore.getMessage(),
							ignore);
				}

			}
			names.close();
			try {
				zipAggregator.addPlainFileFromBytes("names.txt",
						byteOutputStream.toByteArray());
			} catch (BadInputZipFileException e) {
				throw new ServletException(e);
			}
			zipAggregator.close();

			// write the zipfile to the client
			response.setContentType("application/zip");
			response.setContentLength((int) tempfile.length());

			// take into account the inability of certain browsers to download
			// zipfiles
			String filename = "p" + project.getProjectNumber() + ".zip";
			Util.setAttachmentHeaders(response, filename);

			ServletOutputStream out = response.getOutputStream();
			fis = new FileInputStream(tempfile);

			IO.copyStream(fis, out);

			out.flush();
			out.close();
		} catch (SQLException e) {
			handleSQLException(e);
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
			if (tempfile != null && !tempfile.delete())
				getSubmitServerServletLog().warn(
						"Unable to delete temporary file "
								+ tempfile.getAbsolutePath());
			IOUtils.closeQuietly(fileOutputStream);
			IOUtils.closeQuietly(fis);
		}
	}

}

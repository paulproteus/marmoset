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
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.ZipFileAggregator;

public class DownloadAllSubmissions extends SubmitServerServlet {

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Connection conn = null;

		try {
			conn = getConnection();

			Project project = (Project) request.getAttribute("project");
			Map<Integer, String> map = StudentRegistration
					.lookupStudentRegistrationMapByProjectPK(
							project.getProjectPK(), conn);


			// write the zipfile to the client
			response.setContentType("application/zip");

			String filename = project.getProjectNumber() + "-all.zip";
			Util.setAttachmentHeaders(response, filename);

			ServletOutputStream out = response.getOutputStream();

			// Zip aggregator
			ZipFileAggregator zipAggregator = new ZipFileAggregator(out);

			List<Submission> allSubmissions = Submission.lookupAllByProjectPK(
					project.getProjectPK(), conn);
			for (Submission submission : allSubmissions) {
				try {
					zipAggregator.addFileFromBytes(
							map.get(submission.getStudentRegistrationPK())
									+ "-" + submission.getSubmissionNumber(),
							submission.getSubmissionTimestamp().getTime(),
							submission.downloadArchive(conn));
				} catch (ZipFileAggregator.BadInputZipFileException ignore) {
					// Ignore, since students could submit things that aren't
					// zipfiles
					// and I don't want the entire download to fail because of
					// that.
					getSubmitServerServletLog().warn(ignore.getMessage(),
							ignore);
				}
			}

			zipAggregator.close();


			out.flush();
			out.close();
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
	}

}

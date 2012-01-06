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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.submitServer.RequestParser;
import edu.umd.cs.submitServer.content.DisplaySourceCodeAsHTML;

public class TarantulaSourceDisplay extends SubmitServerServlet {

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
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
		RequestParser parser = new RequestParser(request,
				getSubmitServerServletLog(), strictParameterChecking());
		Connection conn = null;
		try {
			conn = getConnection();

			response.setContentType("text/html");
			PrintWriter out = new PrintWriter(response.getOutputStream());

			Submission submission = (Submission) request
					.getAttribute("submission");
			TestOutcomeCollection collection = (TestOutcomeCollection) request
					.getAttribute("testOutcomeCollection");
			String filename = parser
					.getOptionalCheckedParameter("sourceFileName");

			printSubmissionAsHTML(out, filename, "all", collection, submission,
					conn);

			out.flush();
			out.close();

		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}

	}

	public static void printSubmissionAsHTML(PrintWriter out, String filename,
			String testType, TestOutcomeCollection collection,
			Submission submission, Connection conn) throws SQLException,
			IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(
				submission.downloadArchive(conn));
		ZipInputStream zipIn = new ZipInputStream(bais);
		Map<String, CodeCoverageResults> map = collection
				.getCoverageResultsMap();
		while (true) {
			ZipEntry entry = zipIn.getNextEntry();
			if (entry == null)
				break;
			if (entry.isDirectory())
				continue;
			String name = entry.getName();
			if (!name.endsWith(".java"))
				continue;
			if (!name.matches(filename))
				continue;
			DisplaySourceCodeAsHTML src2html =  DisplaySourceCodeAsHTML.build(name, zipIn);

			src2html.setOutputStream(out);

			src2html.setFileWithCoverage(map.get(TestOutcome.CARDINAL)
					.getFileWithCoverage(new File(name).getName()));
			src2html.setTestOutcomeCollection(collection);

			out.write("<html><head><title>Tarantula Coverage</title></head><body>\n"
					);
			out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"/styles.css\">\n"
					);

			src2html.convert();

			out.write("</body></html>\n");
			out.flush();
			break;
		}
	}
}

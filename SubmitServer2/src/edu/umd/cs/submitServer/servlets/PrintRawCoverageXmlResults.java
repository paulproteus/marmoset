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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.utilities.XSSScrubber;
import edu.umd.cs.submitServer.RequestParser;

public class PrintRawCoverageXmlResults extends SubmitServerServlet {

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
		Connection conn = null;
		try {
			conn = getConnection();

			RequestParser parser = new RequestParser(request,
					getSubmitServerServletLog(), strictParameterChecking());
			Integer testRunPK = parser.getIntegerParameter("testRunPK", null);
			String testName = parser.getOptionalCheckedParameter("testName");
			Integer testNumber = parser.getIntegerParameter("testNumber", null);

			String results = "Can't find raw code coverage XML results for "
					+ testRunPK + ", " + testNumber + " => " + testName;

			TestOutcomeCollection collection = TestOutcomeCollection
					.lookupByTestRunPK(testRunPK, conn);
			for (TestOutcome outcome : collection) {
				if (outcome.getTestName().equals(testName)) {
					results = outcome.getCodeCoverageXMLResultsAsString();
				}
			}

			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();

			out.print(XSSScrubber.scrubbedStr(results));
			out.flush();
			out.close();

		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
	}
}

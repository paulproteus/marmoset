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
 * Created on Jul 5, 2005
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.FileNameLineNumberPair;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.RequestParser;

/**
 * @author jspacco
 * 
 */
public class WarningWithCoverageFilter extends SubmitServerFilter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		Connection conn = null;
		try {
			conn = getConnection();

			RequestParser parser = new RequestParser(request,
					getSubmitServerFilterLog(), strictParameterChecking());
			String longTestResult = parser
					.getCheckedParameter("longTestResult");
			String warningName = parser.getCheckedParameter("warningName");
			String priority = parser.getCheckedParameter("priority");

			String shortTestResult = parser
					.getCheckedParameter("shortTestResult");
			FileNameLineNumberPair pair = TestOutcome
					.getFileNameLineNumberPair(shortTestResult);
			String fileName = pair.getFileName();
			int lineNumber = pair.getLineNumber();

			int testRunPK = parser.getIntParameter("testRunPK");

			// Create a FindBugs warning to set as a request attribute.
			// Safer to set this as parameters of the request rather than
			// giving the necessary information to look it up from the database
			// for situations where we re-run only the FindBugs results and
			// don't
			// bother to put them into the database.
			TestOutcome warning = new TestOutcome();
			warning.setTestType(TestOutcome.TestType.FINDBUGS);
			warning.setOutcome(TestOutcome.STATIC_ANALYSIS);
			warning.setTestRunPK(testRunPK);
			warning.setExceptionClassName(priority);
			warning.setTestName(warningName);
			warning.setLongTestResult(longTestResult);
			warning.setShortTestResult(shortTestResult);
			request.setAttribute("warning", warning);

			TestOutcomeCollection testOutcomeCollection = TestOutcomeCollection
					.lookupByTestRunPK(testRunPK, conn);

			getSubmitServerFilterLog().debug(warning);
			getSubmitServerFilterLog().debug(
					"File: " + fileName + " at " + lineNumber);

			testOutcomeCollection = testOutcomeCollection
					.getTestOutcomesCoveringFileAtLine(fileName, lineNumber);
			request.setAttribute("testOutcomeCollection", testOutcomeCollection);
			getSubmitServerFilterLog().debug(
					"testOutcomeCollection contains "
							+ testOutcomeCollection.size() + " elements");
		} catch (InvalidRequiredParameterException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
		chain.doFilter(request, response);
	}
}

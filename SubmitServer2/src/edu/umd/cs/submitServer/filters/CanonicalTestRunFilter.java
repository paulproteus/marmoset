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
 * Created on Jan 19, 2005
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestRun;
import edu.umd.cs.marmoset.modelClasses.TestSetup;

/**
 * Requires a testSetup
 * 
 * This filter stores canonicalTestRun, canonicalTestOutcomeCollection, and
 * canonicalTestOutcomeMap in the request.
 */
public class CanonicalTestRunFilter extends SubmitServerFilter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		Project project = (Project) request.getAttribute(PROJECT);

		Connection conn = null;
		try {
			conn = getConnection();
			TestSetup testSetup = TestSetup.lookupByTestSetupPK(
					project.getTestSetupPK(), conn);
			if (testSetup != null) {
				TestRun canonicalTestRun = TestRun.lookupByTestRunPK(
						testSetup.getTestRunPK(), conn);
				TestOutcomeCollection canonicalTestOutcomeCollection = TestOutcomeCollection
						.lookupByTestRunPK(canonicalTestRun.getTestRunPK(),
								conn);
				Map<String, TestOutcome> canonicalTestOutcomeMap = new HashMap<String, TestOutcome>();
				for (TestOutcome testOutcome : canonicalTestOutcomeCollection
						.getAllTestOutcomes()) {
					canonicalTestOutcomeMap.put(testOutcome.getTestName(),
							testOutcome);
				}
				request.setAttribute("canonicalTestSetup", testSetup);
				request.setAttribute("canonicalTestRun", canonicalTestRun);
				request.setAttribute("canonicalTestOutcomeCollection",
						canonicalTestOutcomeCollection);
				request.setAttribute("canonicalTestOutcomeMap",
						canonicalTestOutcomeMap);
			}

		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
		chain.doFilter(request, response);
	}

}

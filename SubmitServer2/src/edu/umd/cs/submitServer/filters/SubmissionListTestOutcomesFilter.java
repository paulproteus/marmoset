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
 * Created on Jan 21, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestProperties;

/**
 * @author jspacco
 * 
 *         TODO take extensions into account
 */
public class SubmissionListTestOutcomesFilter extends SubmitServerFilter {

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

			List<Submission> submissionList = (List<Submission>) request
					.getAttribute(SUBMISSION_LIST);
			Map<Integer,Integer> coverageScore = null;
			TestProperties testProperties = (TestProperties) request.getAttribute(TEST_PROPERTIES);
            if (testProperties != null &&  testProperties.isPerformCodeCoverage()) 
			    coverageScore = new HashMap<Integer, Integer>();

			Map<Integer, TestOutcomeCollection> testOutcomeMap = new HashMap<Integer, TestOutcomeCollection>();
			for (Submission submission : submissionList) {
				if (submission.getCurrentTestRunPK() == null)
					continue;
				TestOutcomeCollection outcome = TestOutcomeCollection
						.lookupByTestRunPK(submission.getCurrentTestRunPK(),
								conn);
				if (outcome == null)
					continue;
				testOutcomeMap.put(submission.getSubmissionPK(), outcome);
				
				if (coverageScore != null) 
				    coverageScore.put(submission.getSubmissionPK(),  outcome.getCoverageScore());
				

			}
			if (coverageScore != null)
			    request.setAttribute("coverageScore", coverageScore);
			request.setAttribute(TEST_OUTCOMES_MAP, testOutcomeMap);

		} catch (SQLException e) {
			handleSQLException(e);
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
		chain.doFilter(request, response);
	}
}

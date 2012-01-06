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

package edu.umd.cs.submitServer.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestProperties;

/**
 * Retrieves overall code coverage stats from the testOutcomeCollection and
 * stores the results as request attributes. Only works for Java code.
 * 
 * @author jspacco
 * 
 */
public class CodeCoverageStatsFilter extends SubmitServerFilter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		// Project project = (Project)request.getAttribute("project");
		TestProperties testProperties = (TestProperties) request
				.getAttribute(TEST_PROPERTIES);
		if (testProperties != null && testProperties.isJava()) {
			TestOutcomeCollection collection = (TestOutcomeCollection) request
					.getAttribute("testOutcomeCollection");

			if (collection != null) {
				boolean hasCodeCoverageResults = false;
				CodeCoverageResults publicCoverageResults = collection
						.getOverallCoverageResultsForPublicTests();
				CodeCoverageResults releaseCoverageResults = collection
						.getOverallCoverageResultsForReleaseTests();
				CodeCoverageResults studentCoverageResults = collection
						.getOverallCoverageResultsForStudentTests();
				CodeCoverageResults cardinalCoverageResults = collection
						.getOverallCoverageResultsForCardinalTests();
				CodeCoverageResults releaseUniqueResults = new CodeCoverageResults();

				if (studentCoverageResults.size() > 0) {
					request.setAttribute("studentCoverageStats",
							studentCoverageResults.getOverallCoverageStats());
					hasCodeCoverageResults = true;
				}
				if (releaseCoverageResults.size() > 0) {
					request.setAttribute("releaseCoverageStats",
							releaseCoverageResults.getOverallCoverageStats());
					hasCodeCoverageResults = true;
				}
				if (cardinalCoverageResults.size() > 0) {
					request.setAttribute("cardinalCoverageStats",
							cardinalCoverageResults.getOverallCoverageStats());
					hasCodeCoverageResults = true;
				}
				if (publicCoverageResults.size() > 0) {
					request.setAttribute("publicCoverageStats",
							publicCoverageResults.getOverallCoverageStats());
					hasCodeCoverageResults = true;
				}

				CodeCoverageResults publicAndStudentCoverageResults = new CodeCoverageResults(
						publicCoverageResults);
				publicAndStudentCoverageResults.union(studentCoverageResults);
				if (publicAndStudentCoverageResults.size() > 0) {
					request.setAttribute("publicAndStudentCoverageStats",
							publicAndStudentCoverageResults
									.getOverallCoverageStats());
					hasCodeCoverageResults = true;
				}
				if (releaseCoverageResults.size() > 0) {
					request.setAttribute("releaseCoverageStats",
							releaseCoverageResults.getOverallCoverageStats());
					hasCodeCoverageResults = true;
					releaseUniqueResults = new CodeCoverageResults(
							releaseCoverageResults);
					releaseUniqueResults
							.excluding(publicAndStudentCoverageResults);
					request.setAttribute("releaseUniqueStats",
							releaseUniqueResults.getOverallCoverageStats());
				}

				CodeCoverageResults intersectionCoverageResults = new CodeCoverageResults(
						studentCoverageResults);
				intersectionCoverageResults
						.union(publicAndStudentCoverageResults);
				intersectionCoverageResults.intersect(cardinalCoverageResults);
				if (intersectionCoverageResults.size() > 0) {
					request.setAttribute("intersectionCoverageStats",
							intersectionCoverageResults
									.getOverallCoverageStats());
					hasCodeCoverageResults = true;
				}
				request.setAttribute("hasCodeCoverageResults",
						hasCodeCoverageResults);
			}
		}
		chain.doFilter(request, response);
	}
}

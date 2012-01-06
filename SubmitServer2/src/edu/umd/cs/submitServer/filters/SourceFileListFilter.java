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
 * Created on April 11, 2005
 */
package edu.umd.cs.submitServer.filters;

import java.io.File;
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

import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.codeCoverage.CoverageStats;
import edu.umd.cs.marmoset.codeCoverage.FileWithCoverage;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestProperties;
import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.content.DisplaySubmissionSourceCode;

/**
 * Filter to add the List of source file names for a submission.
 * 
 * Also adds a map from the source file names to their coverageStats.
 * 
 * @author David Hovemeyer
 */
public class SourceFileListFilter extends SubmitServerFilter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		Connection conn = null;
		try {
			conn = getConnection();

			Submission submission = (Submission) request
					.getAttribute(SUBMISSION);
			TestProperties testProperties = (TestProperties) request
					.getAttribute(TEST_PROPERTIES);

			// List of the source files
			List<String> sourceFileList = DisplaySubmissionSourceCode
					.getSourceFilesForSubmission(conn, submission,
							testProperties);
			request.setAttribute(SubmitServerConstants.SOURCE_FILE_LIST,
					sourceFileList);

			if (testProperties == null
					|| !testProperties.isPerformCodeCoverage()) {
				chain.doFilter(request, response);
				return;
			}

			// For Java projects, map the sourcefiles to their corresponding
			// coverage results
			CodeCoverageResults codeCoverageResults = (CodeCoverageResults) request
					.getAttribute("codeCoverageResults");
			if (testProperties.isJava() && codeCoverageResults != null) {
				getSubmitServerFilterLog().trace(
						"test.properties language = "
								+ testProperties.getLanguage());
				Map<String, CoverageStats> filenameToCoverageStatsMap = new HashMap<String, CoverageStats>();
				// Set attribute for total coverageResults.

				// Create a map from the full pathname of the file to the code
				// coverage stats
				for (String longFileName : sourceFileList) {
					if (longFileName.endsWith("package-info.java")) {
						// Skip over package-level annotation sources,
						// which have no implementation in them and are
						// skipped by the coverage tool.
						continue;
					}
					String shortFileName = new File(longFileName).getName();
					FileWithCoverage fileWithCoverage = codeCoverageResults
							.getFileWithCoverage(shortFileName);

					getSubmitServerFilterLog().debug(
							"shortFileName: " + shortFileName
									+ ", longFileName: " + longFileName);

					if (fileWithCoverage == null) {
						getSubmitServerFilterLog().warn(
								"Cannot find coverage information for "
										+ shortFileName);
						continue;
					}
					filenameToCoverageStatsMap.put(longFileName,
							fileWithCoverage.getCoverageStats());
				}

				// Set attribute for filename => coverageStats map
				request.setAttribute("filenameToCoverageStatsMap",
						filenameToCoverageStatsMap);
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
		chain.doFilter(request, response);
	}

}

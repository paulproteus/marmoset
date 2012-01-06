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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestProperties;
import edu.umd.cs.submitServer.content.DisplaySubmissionSourceCode;

public class DownloadCodeCoverageResultsByPackage extends SubmitServerServlet {

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
			Project project = (Project) request.getAttribute("project");
			Set<StudentRegistration> registrationSet = (Set<StudentRegistration>) request
					.getAttribute("studentRegistrationSet");
			Map<Integer, Submission> bestSubmissionMap = (Map<Integer, Submission>) request
					.getAttribute("bestSubmissionMap");

			response.setContentType("text/plain");
			String filename = "coverage-results-by-package"
					+ project.getProjectNumber() + "-"
					+ project.getTestSetupPK() + ".csv";
			Util.setAttachmentHeaders(response, filename);

			PrintWriter out = response.getWriter();

			String extraTabs = "\t\t\t\t";
			out.println("class_acct\tstatus\tstudentTests\tpackage\t"
					+ "student" + extraTabs + "public" + extraTabs
					+ "public_and_student" + extraTabs
					+ "public_release_secret" + extraTabs
					+ "public_release_intersect_public_student" + extraTabs
					+ "totals" + extraTabs);
			String headers = "stmts\t" + "cond\t" + "methods\t" + "elements";
			String totalHeaders = "stmts\t" + "cond\t" + "methods\t"
					+ "elements";
			out.println("class_acct\tstatus\tstudentTests\tpackage\t" + headers
					+ "\t" + headers + "\t" + headers + "\t" + headers + "\t"
					+ headers + "\t" + totalHeaders);

			for (StudentRegistration registration : registrationSet) {
				Submission submission = bestSubmissionMap.get(registration
						.getStudentRegistrationPK());

				if (submission == null) {
					continue;
				}
				TestProperties testProperties = (TestProperties) request
						.getAttribute(TEST_PROPERTIES);

				List<String> sourceFileList = DisplaySubmissionSourceCode
						.getSourceFilesForSubmission(conn, submission,
								testProperties);
				Set<String> packageNameSet = getPackageSet(sourceFileList);

				// Get the testOutcomeCollection
				TestOutcomeCollection collection = TestOutcomeCollection
						.lookupByTestRunPK(submission.getCurrentTestRunPK(),
								conn);

				Map<String, Map<String, CodeCoverageResults>> codeCoverageResultsMap = collection
						.getCoverageResultsByPackageMap(packageNameSet);

				// map of package-names to a map of "interesting" categories
				// mapped to their results
				for (Entry<String, Map<String, CodeCoverageResults>> entry : codeCoverageResultsMap
						.entrySet()) {
					// print cvs account, ontime-status and package-name
					out.print(registration.getClassAccount() + "\t"
							+ submission.getStatus() + "\t"
							+ collection.getNumStudentWrittenTests() + "\t"
							+ entry.getKey() + "\t");

					// TODO factor the next bunch of lines into a static method
					// (or a utility class)
					Map<String, CodeCoverageResults> coverageResults = entry
							.getValue();
					CodeCoverageResults publicCoverageResults = coverageResults
							.get("public");
					CodeCoverageResults studentCoverageResults = coverageResults
							.get("student");
					CodeCoverageResults publicAndStudentCoverageResults = coverageResults
							.get("public_and_student");
					CodeCoverageResults cardinalCoverageResults = coverageResults
							.get("cardinal");
					CodeCoverageResults intersectionCoverageResults = coverageResults
							.get("public_and_student_intersect_cardinal");

					out.print(studentCoverageResults.getOverallCoverageStats()
							.getCSVValues() + "\t");
					out.print(publicCoverageResults.getOverallCoverageStats()
							.getCSVValues() + "\t");
					out.print(publicAndStudentCoverageResults
							.getOverallCoverageStats().getCSVValues() + "\t");
					out.print(cardinalCoverageResults.getOverallCoverageStats()
							.getCSVValues() + "\t");
					out.print(intersectionCoverageResults
							.getOverallCoverageStats().getCSVValues() + "\t");
					// Now print the totals once, make sure to include a newline
					out.print(cardinalCoverageResults.getOverallCoverageStats()
							.getCSVTotals() + "\n");
				}
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	private static Set<String> getPackageSet(List<String> sourceFiles) {
		Set<String> set = new HashSet<String>();
		for (String filename : sourceFiles) {
			String packageName = getPackageName(filename);
			set.add(packageName);
		}
		return set;
	}

	private static Map<String, List<String>> getPackageMap(
			List<String> sourceFiles) {
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for (String filename : sourceFiles) {
			String packageName = getPackageName(filename);
			mapPackageNameToFileName(packageName, filename, map);
		}
		return map;
	}

	private static void mapPackageNameToFileName(String packageName,
			String filename, Map<String, List<String>> map) {
		List<String> list = map.get(packageName);
		if (list == null)
			list = new LinkedList<String>();
		list.add(filename);
		map.put(packageName, list);
	}

	private static String getPackageName(String filename) {
		int index = filename.lastIndexOf('/');
		if (index >= 0) {
			return filename.substring(0, index);
		}
		return "";
	}
}

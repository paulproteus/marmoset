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

/**
 * Created on Nov 5, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.content;

import java.util.Iterator;

import edu.umd.cs.marmoset.codeCoverage.CoverageStats;
import edu.umd.cs.marmoset.codeCoverage.FileWithCoverage;
import edu.umd.cs.marmoset.parser.JavaTokenScanner;

/**
 * DisplayJavaSourceCodeWithCoverage
 *
 * @author jspacco
 */
public class DisplayJavaSourceCodeWithCoverage extends DisplaySourceCodeAsHTML {
	public DisplayJavaSourceCodeWithCoverage(Iterator<String> txt, FileWithCoverage fileWithCoverage) {
		super(new JavaTokenScanner(txt));

	}

	@Override
	protected void beginCode() {
		// Display code coverage stats if we have code coverage information
		// avaialble.
		CoverageStats coverageStats = fileWithCoverage.getCoverageStats();
		out.println("<table>");
		out.println("<tr>");
		out.println("<th>Source file</th>");
		out.println("<th>statements</th>");
		out.println("<th>conditionals</th>");
		out.println("<th>methods</th>");
		out.println("<th>total</th>");

		out.println("<tr>");
		out.println("<td>" + fileWithCoverage.getShortFileName() + "</td>");
		out.println(coverageStats.getHTMLTableRow());
		out.println("</tr>");

		out.println("</table>");
		out.println("<p>");

		super.beginCode();
	}

}

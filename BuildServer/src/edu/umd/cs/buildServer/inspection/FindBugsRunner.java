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
 * Created on Sep 25, 2004
 */
package edu.umd.cs.buildServer.inspection;

import java.io.InputStream;

import edu.umd.cs.buildServer.util.DevNullOutputStream;
import edu.umd.cs.buildServer.util.IO;
import edu.umd.cs.findbugs.BugAnnotation;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.ClassAnnotation;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

/**
 * Run FindBugs on a project and report the generated warnings as TestOutcomes.
 *
 * @author David Hovemeyer
 */
public class FindBugsRunner extends AbstractFindBugsRunner {

	/**
	 * Constructor. setProjectSubmission() must be called before the object can
	 * be used.
	 */
	public FindBugsRunner() {
	}

	@Override
	protected String[] getExtraFindBugsOptions() {
		// TODO Refactor to read options from test.properties file.

		return new String[] { "-maxRank", "12", };

	}

	@Override
	protected Thread createStdoutMonitor(InputStream in) {
		return new FindBugsDocumentBuilder(in, projectSubmission.getLog());
	}

	@Override
	protected Thread createStderrMonitor(InputStream err) {
		return IO.monitor(err, new DevNullOutputStream());
	}

	@Override
	protected void inspectFindBugsResults(Thread stdoutMonitor,
			Thread stderrMonitor) {

		SortedBugCollection bugCollection = ((FindBugsDocumentBuilder) stdoutMonitor)
				.getBugCollection();
		if (bugCollection == null) {
			getProjectSubmission().getLog().warn(
					"Could not get BugCollection from findbugs process");
			return;
		}

		TestOutcomeCollection testOutcomeCollection = getTestOutcomeCollection();

		int count = TestOutcome.FIRST_TEST_NUMBER;
		for(BugInstance bug : bugCollection.getCollection()) {
			TestOutcome outcome = convert(bug);
			outcome.setTestNumber(Integer.toString(count++));
			testOutcomeCollection.add(outcome);
		}
	}

	public TestOutcome convert(BugInstance bug) {
		// Turn the warning into a TestOutcome
		TestOutcome testOutcome = new TestOutcome();
		testOutcome.setTestType(TestOutcome.FINDBUGS_TEST);
		testOutcome.setTestName(bug.getBugPattern().getType());
		testOutcome.setOutcome(TestOutcome.STATIC_ANALYSIS);
		testOutcome.setShortTestResult(bug.getPrimarySourceLineAnnotation()
				.toString());
		String msg = bug.getMessageWithoutPrefix();
		testOutcome.setExceptionClassName(Integer.toString(bug.getBugRank()));
		ClassAnnotation primaryClass = bug.getPrimaryClass();
		StringBuilder buf = new StringBuilder(msg);
		for (BugAnnotation b : bug.getAnnotationsForMessage(false)) {
			buf.append("\n");
			buf.append(b.toString(primaryClass));
		}
		String description = buf.toString();
		System.out.println(description);
		testOutcome.setLongTestResult(description);
		testOutcome.setDetails(description);
		return testOutcome;
	}


}

// vim:ts=4

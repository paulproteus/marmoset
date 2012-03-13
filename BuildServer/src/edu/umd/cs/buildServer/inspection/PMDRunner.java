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
 * Created on Mar 25, 2005
 */
package edu.umd.cs.buildServer.inspection;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import edu.umd.cs.buildServer.ConfigurationKeys;
import edu.umd.cs.buildServer.ProjectSubmission;
import edu.umd.cs.buildServer.XMLDocumentBuilder;
import edu.umd.cs.buildServer.util.Alarm;
import edu.umd.cs.buildServer.util.DevNullOutputStream;
import edu.umd.cs.buildServer.util.IO;
import edu.umd.cs.marmoset.modelClasses.JUnitTestProperties;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.utilities.MarmosetUtilities;

/**
 * Run PMD on a compiled submission.
 *
 * @see <a href="http://pmd.sourceforge.net">PMD website</a>
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class PMDRunner implements ConfigurationKeys, ISubmissionInspectionStep<JUnitTestProperties> {
	private ProjectSubmission<JUnitTestProperties> projectSubmission;
	private TestOutcomeCollection testOutcomeCollection;

	/**
	 * Constructor. setProjectSubmission() must be called before this object can
	 * be executed.
	 */
	public PMDRunner() {
		this.testOutcomeCollection = new TestOutcomeCollection();
	}

	private static final int PMD_TIMEOUT_IN_SECONDS = 120;

	@Override
	public void setProjectSubmission(ProjectSubmission<JUnitTestProperties> projectSubmission) {
		this.projectSubmission = projectSubmission;
	}

	@Override
	public TestOutcomeCollection getTestOutcomeCollection() {
		return testOutcomeCollection;
	}

	@Override
	public void execute() {
		projectSubmission.getLog().debug("Running PMD on the code");
		// rulesets of pmd detectors

		String RULESETS = "rulesets/basic.xml,rulesets/braces.xml,"
				+ "rulesets/codesize.xml,rulesets/controversial.xml,"
				+ "rulesets/coupling.xml,rulesets/design.xml,"
				+ "rulesets/naming.xml,rulesets/strictexception.xml,"
				+ "rulesets/strings.xml,rulesets/unusedcode.xml";

		File buildServerRoot = projectSubmission.getConfig().getBuildServerRoot();
		
		RULESETS += "," + new File(buildServerRoot, "pmd/langfeatures.xml").getPath();
		

		// first stry the pmd.sh on the path
		String pmdExe = "pmd.sh";
		// if PMD_HOME is specified, use this instead
		String pmdHome = projectSubmission.getConfig().getConfig().getOptionalProperty(PMD_HOME);
        if (pmdHome != null) {
			pmdExe = new File(pmdHome, "bin/pmd.sh").toString();
		}
		List<String> args = new LinkedList<String>();
		args.add(pmdExe);
		args.add(projectSubmission.getZipFile().getPath());
		args.add("xml");
		args.add(RULESETS);

		projectSubmission.getLog().debug(
				"pmd command: " + MarmosetUtilities.commandToString(args));

		Process process = null;
		boolean exited = false;
		Alarm alarm = new Alarm(PMD_TIMEOUT_IN_SECONDS, Thread.currentThread());
		try {
			process = Runtime.getRuntime().exec(
					args.toArray(new String[args.size()]));
			alarm.start();

			XMLDocumentBuilder stdoutMonitor = new XMLDocumentBuilder(
					process.getInputStream(), projectSubmission.getLog());
			Thread stderrMonitor = IO.monitor(process.getErrorStream(),
					new DevNullOutputStream());
			stdoutMonitor.start();
			stderrMonitor.start();

			// Wait for process to exit
			process.waitFor();
			stdoutMonitor.join();
			stderrMonitor.join();
			exited = true;
			alarm.turnOff();

			readPMDTestOutcomes(stdoutMonitor);

		} catch (IOException e) {
			projectSubmission.getLog().warn("Could not run PMD", e);
		} catch (InterruptedException e) {
			projectSubmission.getLog().info("PMD process timed out", e);
		} finally {
			if (process != null && !exited) {
				process.destroy();
			}
		}
	}

	private void readPMDTestOutcomes(XMLDocumentBuilder stdoutMonitor) {
		Document document = stdoutMonitor.getDocument();
		if (document == null)
			return;

		int count = TestOutcome.FIRST_TEST_NUMBER;
		Iterator<?> fileNodeIter = document.selectNodes("//pmd/file")
				.iterator();
		while (fileNodeIter.hasNext()) {
			Node fileElement = (Node) fileNodeIter.next();
			String fileName = fileElement.valueOf("@name");
			Iterator<?> violationIter = fileElement.selectNodes("./violation")
					.iterator();
			while (violationIter.hasNext()) {
				Node violationElement = (Node) violationIter.next();
				String line = violationElement.valueOf("@line");
				String rule = violationElement.valueOf("@rule");
				String description = violationElement.getText();
				String priority = violationElement.valueOf("@priority");

				// Turn the warning into a TestOutcome
				TestOutcome testOutcome = new TestOutcome();
				testOutcome.setTestType(TestOutcome.TestType.PMD);
				testOutcome.setTestName(rule);
				testOutcome.setOutcome(TestOutcome.STATIC_ANALYSIS);
				testOutcome.setShortTestResult(fileName + ":" + line);
				testOutcome.setLongTestResultCompressIfNeeded(description);
				testOutcome.setTestNumber(Integer.toString(count++));
				testOutcome.setExceptionClassName(priority); // XXX: HACK!


				testOutcomeCollection.add(testOutcome);
			}
		}

		projectSubmission.getLog().info(
				"Recorded " + count + " PMD warnings as test outcomes");
	}

	
}

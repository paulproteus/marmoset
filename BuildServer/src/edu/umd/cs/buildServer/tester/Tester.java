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
 * Created on Sep 1, 2004
 */
package edu.umd.cs.buildServer.tester;

import java.io.InputStream;

import org.apache.log4j.Logger;

import edu.umd.cs.buildServer.BuilderException;
import edu.umd.cs.buildServer.ConfigurationKeys;
import edu.umd.cs.buildServer.ProjectSubmission;
import edu.umd.cs.buildServer.builder.DirectoryFinder;
import edu.umd.cs.buildServer.util.Alarm;
import edu.umd.cs.buildServer.util.CombinedStreamMonitor;
import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcome.OutcomeType;
import edu.umd.cs.marmoset.modelClasses.TestOutcome.TestType;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestProperties;
import edu.umd.cs.marmoset.modelClasses.TestPropertyKeys;

/**
 * Test a student project and build a TestOutcomeCollection to represent which
 * tests failed and which succeeded.
 * 
 * @author David Hovemeyer
 */
public abstract class Tester<T extends TestProperties> implements ConfigurationKeys, TestPropertyKeys {

	private T testProperties;
	protected ProjectSubmission<? extends T> projectSubmission;
	private DirectoryFinder directoryFinder;

	private TestOutcomeCollection testOutcomeCollection;

	private boolean executeStudentTests;

	protected CodeCoverageResults publicStudentCoverage = new CodeCoverageResults();
	protected CodeCoverageResults releaseCoverage = new CodeCoverageResults();

	/**
	 * Constructor.
	 * 
	 * @param testProperties
	 *            test properties
	 * @param haveSecurityPolicyFile
	 *            true if a security policy file was specified
	 * @param projectSubmission
	 *            the ProjectSubmission
	 * @param directoryFinder
	 *            DirectoryFinder used to locate build and testfiles directories
	 */
	public Tester(T testProperties,
	        ProjectSubmission<? extends T> projectSubmission, DirectoryFinder directoryFinder) {

		this.testProperties = testProperties;
		this.projectSubmission = projectSubmission;
		this.directoryFinder = directoryFinder;

		this.testOutcomeCollection = new TestOutcomeCollection();

	}

	/**
	 * @return Returns the log.
	 */
	public Logger getLog() {
		return projectSubmission.getLog();
	}

	/**
	 * @return Returns the projectSubmission.
	 */
	public ProjectSubmission<? extends T> getProjectSubmission() {
		return projectSubmission;
	}

	/**
	 * @return Returns the testOutcomeCollection.
	 */
	public TestOutcomeCollection getTestOutcomeCollection() {
		return testOutcomeCollection;
	}

	
	/**
	 * @return Returns the testProperties.
	 */
	public T getTestProperties() {
		return testProperties;
	}

	/**
	 * Set whether or not to execute student-written test cases (if the Tester
	 * supports this). By default, we don't execute student tests.
	 * 
	 * @param executeStudentTests
	 *            true if student tests should be executed, false if not
	 */
	public void setExecuteStudentTests(boolean executeStudentTests) {
		this.executeStudentTests = executeStudentTests;
	}

	/**
	 * Return whether or not student tests should be executed.
	 * 
	 * @return true if student tests should be executed, false if not
	 */
	public boolean executeStudentTests() {
		return this.executeStudentTests;
	}

	/**
	 * Execute the tests.
	 * 
	 * @throws BuilderException
	 *             if an internal (unexpected) error occurs
	 */
	public void execute() throws BuilderException {
		getLog().debug(
				"Preparing to test submission "
						+ getProjectSubmission().getSubmissionPK()
						+ " with test setup "
						+ projectSubmission.getTestSetupPK());
		// XXX Loading test properties should be done as early as possible since
		// an error here
		// precludes testing?
		loadTestProperties();
		executeTests();
		getLog().info(
				"Submission " + getProjectSubmission().getSubmissionPK()
						+ " for test setup "
						+ projectSubmission.getTestSetupPK()
						+ " tested successfully");
	}

	/**
	 * Subclasses override this method to actually execute the tests in some
	 * implementation-defined manner. The test properties will be loaded before
	 * this method is called.
	 * 
	 * @throws BuilderException
	 */
	protected abstract void executeTests() throws BuilderException;

	/**
	 * Get the DirectoryFinder.
	 * 
	 * @return the DirectoryFinder
	 */
	protected DirectoryFinder getDirectoryFinder() {
		return directoryFinder;
	}

	public static TestOutcome createCouldNotRunOutcome(TestType testType,
			String shortTestResult, String longTestresult) {
		TestOutcome outcome = new TestOutcome();
		outcome.setTestType(testType);
		outcome.setTestName("All " + testType + " tests");
		outcome.setOutcome(TestOutcome.COULD_NOT_RUN);
		outcome.setShortTestResult(shortTestResult);
		outcome.setLongTestResult(longTestresult);
		outcome.setTestNumber("0");
		return outcome;
	}

	public static TestOutcome createUnableToRunOneTestOutcome(TestType testType,
			String testMethod, String testClass, int testNumber,
			@OutcomeType String outcome, String shortTestResult, String longTestResult) {
		TestOutcome testOutcome = new TestOutcome();
		testOutcome.setTestType(testType);
		testOutcome.setTestName(testMethod + "(" + testClass + ")");
		testOutcome.setOutcome(outcome);
		testOutcome.setTestNumber(Integer.toString(testNumber));
		testOutcome.setShortTestResult(shortTestResult);
		testOutcome.setLongTestResult(longTestResult);
		return testOutcome;
	}

	/**
	 * Create a stream monitor to read the stdout and stderr from a test
	 * process.
	 * 
	 * @param in
	 *            input stream from the process's stdout
	 * @param err
	 *            input stream from the process's stderr
	 * @return a CombinedStreamMonitor for the process
	 */
	protected CombinedStreamMonitor createStreamMonitor(InputStream in,
			InputStream err) {
		CombinedStreamMonitor monitor = new CombinedStreamMonitor(in, err);
		monitor.setDrainLimit(this.getTestProperties()
				.getMaxDrainOutputInBytes());
		return monitor;
	}

	/**
	 * Get a new test process Alarm.
	 * 
	 * @return a test process Alarm
	 */
	public Alarm getTestProcessAlarm() {
		return new Alarm(getTestProperties().getTestTimeoutInSeconds(),
				Thread.currentThread());
	}

	/**
	 * Load test properties. This should be called before executing any test
	 * processes.
	 */
	protected void loadTestProperties() throws BuilderException {
	}

	/**
	 * Callback for java code that wants to post-process the test outcomes.
	 */
	protected void javaCallback() {
	}

	/**
	 * Prints a message when all tests are completed.
	 */
	protected void testsCompleted() {
		javaCallback();

		for (TestOutcome outcome : getTestOutcomeCollection()) {
			getLog().debug("outcome: " + outcome.toConciseString());
		}

		getLog().info(
				"All tests completed for submission "
						+ projectSubmission.getSubmissionPK()
						+ " for test setup "
						+ projectSubmission.getTestSetupPK() + ": "
						+ getTestOutcomeCollection().getAllOutcomes().size()
						+ " outcomes recorded, "
						+ getTestOutcomeCollection().getNumPassedOverall()
						+ " passed, "
						+ getTestOutcomeCollection().getNumFailedOverall()
						+ " failed");
	}
}

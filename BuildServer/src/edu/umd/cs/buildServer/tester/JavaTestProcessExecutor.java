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
 * Created on Mar 10, 2005
 */
package edu.umd.cs.buildServer.tester;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import edu.umd.cs.buildServer.BuilderException;
import edu.umd.cs.buildServer.ConfigurationKeys;
import edu.umd.cs.buildServer.ProjectSubmission;
import edu.umd.cs.buildServer.builder.DirectoryFinder;
import edu.umd.cs.buildServer.util.Alarm;
import edu.umd.cs.buildServer.util.CombinedStreamMonitor;
import edu.umd.cs.buildServer.util.Untrusted;
import edu.umd.cs.marmoset.modelClasses.JUnitTestProperties;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcome.TestType;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.utilities.MarmosetUtilities;

/**
 * Execute our TestRunner class in a subprocess, in order to run one or more
 * JUnit tests.
 *
 * @see edu.umd.cs.buildServer.tester.JavaTester
 * @see edu.umd.cs.buildServer.tester.TestRunner
 * @author David Hovemeyer
 */
public class JavaTestProcessExecutor implements ConfigurationKeys {
	private final JavaTester tester;
	private final String testClass;
	private final TestType testType;
	private String outputFilename;
	private final String classPath;
	private String testMethod;
	private JUnitTestCase testCase;
	private int nextTestNumber;

	/**
	 * Constructor.
	 *
	 * @param tester
	 *            the JavaTester
	 * @param testClass
	 *            name of test class
	 * @param testType
	 *            type of test (public, release, secret of student)
	 * @param classPath
	 *            classpath to use when executing the tests
	 */
	public JavaTestProcessExecutor(JavaTester tester, String testClass,
			TestType testType, String classPath) {
		this.tester = tester;
		this.testClass = testClass;
		this.testType = testType;
		this.classPath = classPath;
		this.nextTestNumber = TestOutcome.FIRST_TEST_NUMBER;
	}

	/**
	 * Set the method name of the single test case to execute. This is useful
	 * for running a single test case out of a test suite class. Will also set
	 * the outputFilename based on the name of the test case we're running.
	 *
	 * @param testMethod
	 *            name of the single test method to run
	 */
	public void setTestMethod(JUnitTestCase testCase) {
		this.testCase = testCase;
		this.testMethod = testCase.getMethodName();
		this.outputFilename = new File(
				getDirectoryFinder().getBuildDirectory(), testType + "."
						+ testMethod + ".out").getAbsolutePath();
	}


	/**
	 * Set the starting test number to be used when collecting TestOutcomes.
	 *
	 * @param startTestNumber
	 */
	public void setStartTestNumber(int startTestNumber) {
		this.nextTestNumber = startTestNumber;
	}


	public Logger getLog() {
		return tester.getLog();
	}

	public TestOutcomeCollection getTestOutcomeCollection() {
		return tester.getTestOutcomeCollection();
	}

	public DirectoryFinder getDirectoryFinder() {
		return tester.getDirectoryFinder();
	}

	public ProjectSubmission<? extends JUnitTestProperties> getProjectSubmission() {
		return tester.getProjectSubmission();
	}

	public TrustedCodeBaseFinder getTrustedCodeBaseFinder() {
		return tester.getTrustedCodeBaseFinder();
	}

	public boolean getDebugJavaSecurity() {
		return tester.getDebugJavaSecurity();
	}

	/**
	 * Execute test(s) in test suite class.
	 *
	 * @throws IOException
	 * @throws BuilderException
	 */
	public TestOutcome executeTests()

	{
	    try {
		String buildServerTestFilesDir = getDirectoryFinder()
				.getTestFilesDirectory().getCanonicalPath() + File.separator;

		// Build arguments to java process
		List<String> javaArgs = new LinkedList<String>();
		javaArgs.add("java");
		// TODO Factor the amount of memory and the extra -D parameters into
		// config.properties
		String vmArgs = tester.getTestProperties().getVmArgs();
		boolean memorySet = false;
		if (vmArgs != null) {
		    if (vmArgs.contains("-Xmx"))
		        memorySet = true;
			// Break up into separate tokens if necessary
			StringTokenizer tokenizer = new StringTokenizer(vmArgs);
			while (tokenizer.hasMoreElements()) {
				String nextArg = tokenizer.nextToken();
				nextArg = nextArg.replace("${buildserver.test.files.dir}",
						buildServerTestFilesDir);
				javaArgs.add(nextArg);
			}
		}
		if (!memorySet)
		    javaArgs.add("-Xmx456m");
        javaArgs.add("-Dcom.sun.management.jmxremote");
        
		javaArgs.add("-classpath");
		javaArgs.add(classPath);

		// Tests must run headless, for obvious reasons
		javaArgs.add("-Djava.awt.headless=true");

		// Do not allow access to home directory
		javaArgs.add("-Duser.home=/dev/null");

		// Specify filename of project jar file
		javaArgs.add("-Dbuildserver.test.jar.file="
				+ getProjectSubmission().getTestSetup().getCanonicalPath());
		// Specify the path of the build directory
		javaArgs.add("-Dbuildserver.build.dir="
				+ getDirectoryFinder().getBuildDirectory().getCanonicalPath());
		// Add trusted code bases
		for (TrustedCodeBase trustedCodeBase 
		        : getTrustedCodeBaseFinder().getCollection()) {
			javaArgs.add("-D" + trustedCodeBase.getProperty() + "="
					+ trustedCodeBase.getValue());
//			getLog().debug("adding trusted codebase " + trustedCodeBase);
		}
		// Let the test classes know where test files are.
		// Append a separator to the end, because this makes it
		// easier for the tests to determine how to access
		// the test files.
		javaArgs.add("-Dbuildserver.test.files.dir=" + buildServerTestFilesDir);
		if (getDebugJavaSecurity()) {
			javaArgs.add("-Djava.security.debug=access,failure");
		}
		if (tester.getHasSecurityPolicyFile()) {
			// Project jar file contained a security policy file
			javaArgs.add("-Djava.security.manager");
			javaArgs.add("-Djava.security.policy=file:"
					+ new File(getDirectoryFinder().getTestFilesDirectory(),
							"security.policy").getCanonicalPath());
		}
		// XXX TestRunner
		javaArgs.add(TestRunner.class.getName());
		javaArgs.add("-startTestNumber");
		javaArgs.add(String.valueOf(nextTestNumber));

		javaArgs.add(getProjectSubmission().getSubmissionPK());
		javaArgs.add(testType.toString());
		javaArgs.add(testClass);
		javaArgs.add(outputFilename);
		int timeoutInSeconds = tester.getTestProperties()
				.getTestTimeoutInSeconds();
		if (testCase != null && testCase.getMaxTimeInMilliseconds() != 0) {
			timeoutInSeconds = 1 + (int)( testCase.getMaxTimeInMilliseconds() / 1000);
			getLog().trace(
					"Using @Test(timeout=" + timeoutInSeconds
							+ ") annotation");
		}
		javaArgs.add(String.valueOf(timeoutInSeconds));
		if (testMethod != null) {
			javaArgs.add(testMethod);
		}

		// Which directory to execute the TestRunner in.
		// By default, this is the build directory, but the
		// cwd.testfiles.dir property may set it to
		// be the testfiles directory.
		File testRunnerCWD = getDirectoryFinder().getBuildDirectory();
		// Student-written tests must be run from the build directory
		// (where the student code is extracted) no matter what
		if (tester.getTestProperties().isTestRunnerInTestfileDir()
				&& !testType.equals(TestOutcome.TestType.STUDENT))
			testRunnerCWD = getDirectoryFinder().getTestFilesDirectory();

		getLog().debug("TestRunner working directory: " + testRunnerCWD);

		// Execute the test!
		int exitCode;
		Alarm alarm = tester.getTestProcessAlarm();
		CombinedStreamMonitor monitor = null;
		
		Process testRunner = null;
		boolean isRunning = false;
		try {
			// Spawn the TestRunner process
		    String cmd = MarmosetUtilities.commandToString(javaArgs);
            getLog().debug("TestRunner command: " + cmd);
            
			testRunner = Untrusted.execute(
					testRunnerCWD, javaArgs.toArray(new String[javaArgs.size()]));

                int pid = MarmosetUtilities.getPid(testRunner);
                getLog().debug(
                        "Subprocess for submission "
                                + getProjectSubmission().getSubmissionPK()
                                + " for testSetup "
                                + getProjectSubmission().getTestSetupPK()
                                + " for " + testType + " " + nextTestNumber
                                + " " + testMethod + " in testClass "
                                + testClass + " has pid = " + pid);

			isRunning = true;

			// Start the timeout alarm
			alarm.start();

			// Record the output
			monitor = tester.createStreamMonitor(testRunner.getInputStream(),
					testRunner.getErrorStream());
			monitor.start();

			// Wait for the test runner to finish.
			// This may be interrupted by the timeout alarm.
			monitor.join();
			exitCode = testRunner.waitFor();
			isRunning = false;
			// Groovy, we finished before the alarm went off.
			// Disable it (and clear our interrupted status)
			// in case it went off just after the process wait
			// finished.
			alarm.turnOff();

			// Just for debugging...
			getLog().debug(
					"TestRunner process finished; captured to stdout/stderr output was: ");
			getLog().debug(monitor.getCombinedOutput());
			if (monitor.getCombinedOutput().contains("AccessControlException")) {
				getLog().warn(
						"Clover could not be initialized due to an AccessControlException. "
								+ " Please check your security.policy file and make sure that student code "
								+ "has permission to read/write/delete /tmp and can install shutdown hooks");
			}

		} catch (IOException e) {
			String shortTestResult = getFullTestName()
					+ " failed with IOException: " + e.getMessage();
			// TODO get a stack trace into here
			String longTestResult = e.toString();
			getLog().error(shortTestResult, e);
			return Tester.createUnableToRunOneTestOutcome(testType, testMethod,
					testClass, nextTestNumber, TestOutcome.FAILED,
					shortTestResult, longTestResult);
		} catch (InterruptedException e) {
			if (!alarm.fired())
				getLog().error("Someone unexpectedly interrupted the timer");

			String shortTestResult = "Timeout!";
			String longTestResult = monitor.getCombinedOutput();
			getLog().error(shortTestResult, e);
			getLog().trace(
					"Timeout for " + testType + " " + testMethod + " "
							+ nextTestNumber);
			return Tester.createUnableToRunOneTestOutcome(testType, testMethod,
					testClass, nextTestNumber, TestOutcome.TIMEOUT,
					shortTestResult, longTestResult);
		} finally {
			// Make sure the process is cleaned up.
			if (isRunning && testRunner != null) {
				testRunner.destroy();
				// process should already be dead, kill again
				MarmosetUtilities.destroyProcessGroup(testRunner, getLog());
			}
		}

		if (exitCode == 2) {
            // Test runner couldn't execute the tests for some reason.
            // This is probably not our fault.
            // Just add an outcome recording the output of
            // the test runner process.
            String longTestResult = monitor.getCombinedOutput();
            String shortTestResult = "An expected class could not be found";
            int end = longTestResult.indexOf('\n');
            if (end > 0 && longTestResult.startsWith("java.lang.NoClassDefFoundError: ")) {
                String missing = longTestResult.substring("java.lang.NoClassDefFoundError: ".length(), end);
                missing = missing.replace('/','.');
                shortTestResult = "Could not find expected class " + missing;
                longTestResult = "";
            }
            getLog().error(shortTestResult);
            return Tester.createUnableToRunOneTestOutcome(testType, testMethod,
                    testClass, nextTestNumber, TestOutcome.MISSING_COMPONENT,
                    shortTestResult, longTestResult);
        } else if (exitCode != 0) {
			// Test runner couldn't execute the tests for some reason.
			// This is probably not our fault.
			// Just add an outcome recording the output of
			// the test runner process.
			String shortTestResult = getFullTestName()
					+ " subprocess failed to return with 0 status";
			String longTestResult = monitor.getCombinedOutput();
			getLog().error(shortTestResult);
			return Tester.createUnableToRunOneTestOutcome(testType, testMethod,
					testClass, nextTestNumber, TestOutcome.FAILED,
					shortTestResult, longTestResult);
		}

		getLog().debug(
				getFullTestName() + " test finished with "
						+ new File(outputFilename).length()
						+ " bytes of output");

		return readTestOutcomeFromFile();
	    } catch (IOException e) {
	        e.printStackTrace();
	        throw new RuntimeException("Unexpected IO Exception", e);
	    }
	}

	private String getFullTestName() {
		return testType + "-" + nextTestNumber + "-" + testMethod;
	}

	/**
	 * Reads (and deserializes) a testOutcome from the file produced by the call
	 * in executeTests() that runs a test. This call spawns a JVM in a separate
	 * process and so produces a file for us to read. TODO Delete the file when
	 * we're done with it.
	 *
	 * @return The deserialized testOutcome read from the file.
	 */
	private TestOutcome readTestOutcomeFromFile() {
		ObjectInputStream in = null;
		// TODO refactor and just read a single test outcome
		TestOutcomeCollection currentOutcomeCollection = new TestOutcomeCollection();
		try {
			// Open test outcome file.
			if (!currentOutcomeCollection.isEmpty()) {
				getLog().debug(
						"Non-empty outcome collection and we're about to read "
								+ testType + "." + outputFilename);
			}
			FileInputStream fis = tester.openTestOutcomeFile(testType,
					outputFilename);

			// XXX Legacy issue: We used to run all of the test outcomes in
			// separate threads,
			// producing a testOutcomeCollection. Now we run each individual
			// JUnit test case
			// in a separate JVM process (makes it easier to kill a test
			// cleanly) and so we
			// are still reading/writing testOutcomeCollections rather than
			// individual testOutcomes.
			in = new ObjectInputStream(new BufferedInputStream(fis));
			currentOutcomeCollection.read(in);

			// Find all the outcomes that running this test produced
			// See earlier legacy issue:
			// We are running each JUnit test in a separate process
			// and should always have exactly one testOutcome
			if (currentOutcomeCollection.size() > 1) {
			    List<String> tests = new ArrayList<String>(currentOutcomeCollection.size());
				for (TestOutcome outcome : currentOutcomeCollection) {
					getLog().debug(
							"Multiple test outcomes coming back! " + outcome);
					tests.add(outcome.getShortTestName());
				}
				throw new IOException("JUnit tests must be run one at a time! Got " + tests);
			} else if (currentOutcomeCollection.isEmpty()) {
				// TODO put large messages into a messages.properties file
				String message = "Test "
						+ testMethod
						+ " produced no outcome.  "
						+ "This usually happens when a test case times out, but it "
						+ "can happen for a variety of other reasons.  "
						+ "For example, if a student calls System.exit(), this causes the test case to "
						+ "stop suddenly and prevents the SubmitServer from figuring out what went wrong.\n"
						+ "In general, you should not call System.exit(); if your program gets into a "
						+ "situation where you want to halt immediately and signal an error, "
						+ "then instead try throwing a RuntimeException, like this:\n"
						+ "throw new RuntimeException(\"Halting program because...\")\n";
				throw new IOException(message);
			}
			return currentOutcomeCollection.getAllOutcomes().get(0);
		} catch (IOException e) {
			getLog().warn(
					"IOException while reading currentOutcomeCollection from file",
					e);

			TestOutcome outcome = new TestOutcome();
			outcome.setTestType(testType);
			outcome.setTestName(testMethod + "(" + testClass + ")");    
			outcome.setOutcome(TestOutcome.FAILED);
			outcome.setShortTestResult("Unable to read test results for "
					+ testMethod);
			outcome.setLongTestResult(e.getMessage());
			outcome.setTestNumber(Integer.toString(nextTestNumber));
			outcome.setExceptionClassName("");
			return outcome;
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException ignore) {
				getLog().warn(
						"Unable to close input Stream for " + outputFilename,
						ignore);
			}
		}
	}

	/**
	 * @return
	 */
	private boolean isStudentTestType() {
		return testType.equals(TestOutcome.TestType.STUDENT);
	}
}

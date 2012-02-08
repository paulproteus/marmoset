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
 */
package edu.umd.cs.buildServer.tester;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import edu.umd.cs.buildServer.BuilderException;
import edu.umd.cs.buildServer.ProjectSubmission;
import edu.umd.cs.buildServer.builder.DirectoryFinder;
import edu.umd.cs.buildServer.util.CombinedStreamMonitor;
import edu.umd.cs.buildServer.util.ProcessExitMonitor;
import edu.umd.cs.buildServer.util.Untrusted;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcome.TestType;
import edu.umd.cs.marmoset.modelClasses.TestProperties;
import edu.umd.cs.marmoset.utilities.MarmosetUtilities;

/**
 * Tester for C, OCaml and Ruby submissions.
 * <p>
 * <b>NOTE:</b> "CTester" is a legacy name. We use the same infrastructure for
 * building and testing C, OCaml and Ruby code because the process is exactly
 * the same. For more details see {@see CBuilder}.
 *
 * @author David Hovemeyer
 * @author jspacco
 */
public class CTester extends Tester {

	/**
	 * Constructor.
	 *
	 * @param testProperties
	 *            TestProperties loaded from the project jarfile's
	 *            test.properties
	 * @param haveSecurityPolicyFile
	 *            true if there is a security.policy file in the project jarfile
	 * @param projectSubmission
	 *            the ProjectSubmission
	 * @param directoryFinder
	 *            DirectoryFinder to locate build and testfiles directories
	 */
	public CTester(TestProperties testProperties,
			boolean haveSecurityPolicyFile,
			ProjectSubmission projectSubmission, DirectoryFinder directoryFinder) {
		super(testProperties, haveSecurityPolicyFile, projectSubmission,
				directoryFinder);
	}


	@Override
	protected void loadTestProperties() throws BuilderException {
		super.loadTestProperties();
	}


	@Override
	protected void executeTests() throws BuilderException {
		loadTestProperties();
		String[] dynamicTestTypes = TestOutcome.DYNAMIC_TEST_TYPES;
		for (int i = 0; i < dynamicTestTypes.length; ++i) {
			String testType = dynamicTestTypes[i];

			StringTokenizer testExes = getTestExecutables(getTestProperties(),
					testType);
			if (testExes == null)
				// No tests of this kind specified
				continue;

			// Create list of the executables
			int testCount = TestOutcome.FIRST_TEST_NUMBER;
			while (testExes.hasMoreTokens()) {
				executeTest(testExes.nextToken(), testType,
						Integer.toString(testCount++));
			}
		}
		testsCompleted();
	}

	public static StringTokenizer getTestExecutables(TestProperties testProperties,
			String testType) {
		String testExes = testProperties.getTestClass(testType);
		if (testExes == null)
			// No tests of this kind specified
			return null;

		return new StringTokenizer(testExes, ", \t\r\n");
	}

	/**
	 * Execute a single test executable.
	 *
	 * @param exeName
	 *            name of the test executable
	 * @param testType
	 *            test type (public, release, secret, etc.)
	 * @param testNumber
	 *            test number (among other tests of the same type)
	 */
	private void executeTest(String exeName, @TestType String testType, String testNumber)
			throws BuilderException {
		Process process = null;
		boolean finished = false;

		CombinedStreamMonitor streamMonitor = null;
		
		String execString = getTestProperties().getTestExec(testType, exeName);
		if (execString == null) {
		    // Hopefully the test executable is really there.
		    execString = checkTestExe(exeName);
		}

		try {
		

			// Run the test executable in the build directory.
			getLog().debug(
					"Running C test number " + testNumber + ": " + exeName
							+ " process in directory "
							+ getDirectoryFinder().getBuildDirectory());
			getLog().debug(
                    "executing " + execString);
            
			// Add LD_LIBRARY_PATH according to the environment, if requested
			String[] environment = null;
			if (getTestProperties().getLdLibraryPath() != null) {
				environment = new String[] { getTestProperties()
						.getLdLibraryPath() };
				getLog().debug("Library path: " + getTestProperties().getLdLibraryPath());
			}

			int testTimeoutInSeconds = getTestProperties()
					.getTestTimeoutInSeconds();
			
			process = Untrusted.execute(getDirectoryFinder().getBuildDirectory(), 
			        "/bin/bash", "-c", execString);
					

			// Read the stdout/stderr from the test executable.
			getLog().trace("Starting stream monitor");
			streamMonitor = new CombinedStreamMonitor(process.getInputStream(),
					process.getErrorStream());
			streamMonitor.start();

			// Start a thread which will wait for the process to exit.
			// The issue here is that Java has timed monitor waits,
			// but not timed process waits. We emulate the latter
			// using the former.
			getLog().trace("Starting exit monitor");
			ProcessExitMonitor exitMonitor = new ProcessExitMonitor(process, getLog());
			exitMonitor.start();

			// Record a test outcome.
			TestOutcome testOutcome = new TestOutcome();
			testOutcome.setTestNumber(testNumber);
			testOutcome.setTestName(exeName);
			testOutcome.setTestType(testType);
			long processTimeoutMillis = testTimeoutInSeconds * 1000L;

			// Wait for the process to exit.
			getLog().trace(
					"Waiting " + testTimeoutInSeconds
							+ " seconds for the process to stop...");
			if (exitMonitor.waitForProcessToExit(processTimeoutMillis)) {
				int exitCode = exitMonitor.getExitCode();
				getLog().debug("Process exited with exit code: " + exitCode);
				finished = true;
				streamMonitor.join();
				getLog().trace("Joined with stream monitor " + exitCode);

				// Use the process exit code to decide whether the test
				// passed or failed.
				boolean passed = exitCode == 0;
				String outcome = passed ? TestOutcome.PASSED
						: TestOutcome.FAILED;

				getLog().debug("Process exited with exit code " + exitCode);

				// Add a TestOutcome to the TestOutcomeCollection
				testOutcome.setOutcome(outcome);

				testOutcome.setShortTestResult("Test " + exeName + " "
						+ testOutcome.getOutcome());
				// XXX We're storing the output from the streamMonitor in the
				// testOutcome record whether it passes or fails
				testOutcome
						.setLongTestResult(streamMonitor.getCombinedOutput());
			} else {
			    finished = true;
				// Test timed out!
				getLog().warn("Process timed out!");

				testOutcome.setOutcome(TestOutcome.TIMEOUT);
				testOutcome.setShortTestResult("Test " + exeName
						+ " did not complete before the timeout of "
						+ testTimeoutInSeconds
						+ " seconds)");
				testOutcome
						.setLongTestResult(streamMonitor != null ? streamMonitor
								.getCombinedOutput() : "");
			}

			getTestOutcomeCollection().add(testOutcome);
		} catch (IOException e) {
			// Possible reasons this could happen are:
			// - the Makefile is buggy and didn't create the exes it should have
			// - a temporary resource exhaustion prevented the process from
			// running
			// In any case, we can't trust the test results at this point,
			// so we'll abort all testing of this submission.
			throw new BuilderException("Could not run test process", e);
		} catch (InterruptedException e) {
			throw new BuilderException(
					"Test process wait interrupted unexpectedly", e);
		} finally {
			// Whatever happens, make sure we don't leave the process running
			if (process != null && !finished) {
				MarmosetUtilities.destroyProcessGroup(process, getLog());
			}
		}
	}

	/**
	 * Check if a test executable really exists in the build directory. Right
	 * now we just emit log messages if it doesn't.
	 *
	 * @param exeName
	 *            name of the test executable.
	 */
	private String checkTestExe(String exeName) {
		File buildDirectory = getDirectoryFinder().getBuildDirectory();
        File exeFile = new File(buildDirectory, exeName);
        try {
            if (!exeFile.getCanonicalPath().startsWith(buildDirectory.getCanonicalPath()))
                    throw new IllegalArgumentException("executable not in build directory: " + exeName);
        } catch (IOException e1) {
            throw new IllegalArgumentException("Unable to resolve canonical path for " + exeFile.getAbsolutePath());
        }
		int tries = 0;
		while (tries++ < 5) {
			if (exeFile.isFile())
				break;

			getLog().warn(
					"Test executable " + exeFile + " doesn't exist -- sleeping");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
		if (!exeFile.canExecute())
		    getLog().warn(
                    "Test executable " + exeFile + " isn't executable");
		return "./" + exeName;
            
	}
}

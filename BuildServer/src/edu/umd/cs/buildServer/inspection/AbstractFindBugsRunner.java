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
 * Created on April 18, 2005
 */
package edu.umd.cs.buildServer.inspection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import edu.umd.cs.buildServer.Configuration;
import edu.umd.cs.buildServer.ProjectSubmission;
import edu.umd.cs.buildServer.util.Alarm;
import edu.umd.cs.buildServer.util.ArgumentParser;
import edu.umd.cs.buildServer.util.Untrusted;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.utilities.MarmosetUtilities;

public abstract class AbstractFindBugsRunner implements
		ISubmissionInspectionStep {

	/**
	 * Test properties key for additional command line options to be passed to
	 * FindBugs process.
	 */
	private static final String OPTIONS_PROPERTY_KEY = "findbugs.options";

	private static final boolean DEBUG = Boolean
			.getBoolean("buildserver.findbugs.debug");

	private static final int FINDBUGS_TIMEOUT_IN_SECONDS = 240;

	// Fields
	protected ProjectSubmission projectSubmission;
	private TestOutcomeCollection testOutcomeCollection;

	/**
	 * Constructor. setProjectSubmission() must be called before the object can
	 * be used.
	 */
	public AbstractFindBugsRunner() {
		this.testOutcomeCollection = new TestOutcomeCollection();
	}

	@Override
	public void setProjectSubmission(ProjectSubmission projectSubmission) {
		this.projectSubmission = projectSubmission;
	}

	@Override
	public TestOutcomeCollection getTestOutcomeCollection() {
		return testOutcomeCollection;
	}

	public ProjectSubmission getProjectSubmission() {
		return projectSubmission;
	}

	static boolean warnedAboutFindBugs = false;
	/**
	 * Execute FindBugs.
	 */
	@Override
	public void execute() {
		LinkedList<String> args = new LinkedList<String>();


		Configuration config = projectSubmission.getConfig().getConfig();
		String findBugsHome = config.getStringProperty("findbugs.home",
		        projectSubmission.getConfig().getBuildServerRoot() + "/findbugs");

		String findbugsCmd = null;
		if (findBugsHome != null) {    
		    File fb = new File(findBugsHome);
		    File findBugsJar = new File(new File(fb, "lib"), "findbugs.jar");
		    if (fb.exists() && fb.isDirectory() && fb.canRead() && findBugsJar.canRead() ) {
		        findbugsCmd = "java";
		        args.add("-jar");
		        args.add(findBugsJar.getAbsolutePath());
		    }
		} 
		
		if (findbugsCmd == null) {
		    findbugsCmd = "findbugs";
		    if (!warnedAboutFindBugs) {
		    projectSubmission.getLog().warn(
                    "didn't find findbugs; hoping it is on the path");
		    System.err.println("didn't find findbugs; hoping it is on the path");
		    }
		}
			

		
		
		// Build argument list
		args.add(findbugsCmd);
		args.add("-textui");
		// See if findbugs options were specified in the test properties file.
		if (projectSubmission.getTestProperties().getOptionalStringProperty(
				OPTIONS_PROPERTY_KEY) != null) {
			ArgumentParser parser = new ArgumentParser(projectSubmission
					.getTestProperties().getOptionalStringProperty(
							OPTIONS_PROPERTY_KEY));
			while (parser.hasNext())
				args.add(parser.next());
		}

		// Use XML output with messages
		args.add("-xml:withMessages");

		// Add any extra findbugs options specified by the concrete subclass
		String[] extraOptions = getExtraFindBugsOptions();
		if (extraOptions == null) {
			projectSubmission.getLog().warn(
					"Cancelling FindBugs step for submission "
							+ projectSubmission.getSubmissionPK());
			return;
		}
		for (int i = 0; i < extraOptions.length; ++i)
			args.add(extraOptions[i]);

		// Project jar is on auxclasspath.
		args.add("-auxclasspath");
		args.add(projectSubmission.getTestSetup().getAbsolutePath());

		// Analyze all classes in this directory and subdirectories.
		args.add(".");

		projectSubmission.getLog().debug(
				"findbugs command: " + MarmosetUtilities.commandToString(args));
		projectSubmission.getLog().debug(
				"running findbugs in "
						+ projectSubmission.getBuildOutputDirectory()
						+ " directory");

		Process process = null;
		boolean exited = false;
		Alarm alarm = new Alarm(FINDBUGS_TIMEOUT_IN_SECONDS,
				Thread.currentThread());
		try {
			process = Untrusted.execute(
					args.toArray(new String[args.size()]), null,
					projectSubmission.getBuildOutputDirectory());

			alarm.start();

			// Capture stdout, and drain stderr.
			// FIXME: should limit output
			Thread stdoutMonitor = createStdoutMonitor(process.getInputStream());
			Thread stderrMonitor = createStderrMonitor(process.getErrorStream());
			stdoutMonitor.start();
			stderrMonitor.start();

			// Wait for process to exit
			int exitCode = process.waitFor();
			stdoutMonitor.join();
			stderrMonitor.join();
			exited = true;
			alarm.turnOff();

			if (exitCode != 0) {
				projectSubmission.getLog().warn(
						"FindBugs process returned non-zero exit code "
								+ exitCode);
			}

			inspectFindBugsResults(stdoutMonitor, stderrMonitor);

			projectSubmission.getLog().info(
					"Collected "
							+ testOutcomeCollection.getAllOutcomes().size()
							+ " findbugs warnings as test outcomes");
		} catch (InterruptedException e) {
			// Process timed out.
			// Oh well.
			if (DEBUG)
				System.out.println("FindBugs process timed out");
			projectSubmission.getLog().warn("FindBugs process timed out", e);
		} catch (IOException e) {
			projectSubmission.getLog().warn("Could not execute FindBugs", e);
		} finally {
			if (!exited && process != null) {
				process.destroy();
			}
		}
	}

	/**
	 * Get extra FindBugs options to add to the command line. This is useful for
	 * choosing which detectors to run, etc.
	 *
	 * @return list of extra FindBugs command line options; or null if the
	 *         FindBugs step should be cancelled
	 */
	protected abstract String[] getExtraFindBugsOptions();

	/**
	 * Create a Thread to monitor the stdout from the FindBugs process.
	 *
	 * @param in
	 *            InputStream reading from the stdout of the FindBugs process
	 * @return the output monitor Thread
	 */
	protected abstract Thread createStdoutMonitor(InputStream in);

	/**
	 * Create a Thread to monitor the stderr from the FindBugs process.
	 *
	 * @param err
	 *            InputStream reading from the stderr of the FindBugs process
	 * @return the output monitor Thread
	 */
	protected abstract Thread createStderrMonitor(InputStream err);

	/**
	 * Inspect the FindBugs results.
	 *
	 * @param stdoutMonitor
	 *            the thread monitoring the stdout from FindBugs
	 * @param stderrMonitor
	 *            the thread monitoring the stderr from FindBugs
	 */
	protected abstract void inspectFindBugsResults(Thread stdoutMonitor,
			Thread stderrMonitor);
}

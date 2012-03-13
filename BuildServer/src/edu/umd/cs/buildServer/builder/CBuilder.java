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
package edu.umd.cs.buildServer.builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import edu.umd.cs.buildServer.BuilderException;
import edu.umd.cs.buildServer.CompileFailureException;
import edu.umd.cs.buildServer.ProjectSubmission;
import edu.umd.cs.buildServer.util.CombinedStreamMonitor;
import edu.umd.cs.buildServer.util.ProcessExitMonitor;
import edu.umd.cs.buildServer.util.Untrusted;
import edu.umd.cs.diffText.TextDiff;
import edu.umd.cs.marmoset.modelClasses.ExecutableTestCase;
import edu.umd.cs.marmoset.modelClasses.MakeTestProperties;
import edu.umd.cs.marmoset.modelClasses.TestPropertyKeys;

/**
 * Builder for C, OCaml and Ruby submissions.
 * <p>
 * <b>NOTE:</b> "CBuilder" is a legacy name from when we only supported C and
 * Java code.
 * <p>
 * Our process for building non-Java code is simple; the test-setup provides a
 * Makefile that produces executables, and we run each executable. In more
 * precise detail, the process is as follows:
 * <ul>
 * <li>In the test-setup zipfile, the instructor provides a Makefile
 * <li>The BuildServer will call the version of make specified by the
 * build.make.command in the test.properties file of the test-setup in the
 * unpacked directory. This will produce a number of executables.
 * <li>In the test.properties file of the test setup zipfile, the instructor
 * provides a list of executables that will be created by running the Makefile.
 * <li>The CTester class (also a misnomer since it covers C, OCaml, Ruby and any
 * other non-Java project) will then run all of the executables that are created
 * by the Makefile and listed in test.properties.
 * <li>Any executable that returns 0 (zero) passes, and any executable that
 * returns a non-zero value fails.
 * <li>This mechanism is extremely flexible because the executables that are
 * generated can be shell scripts that diff files produced by running other
 * executables or something like that.
 * </ul>
 * 
 * @author David Hovemeyer
 * @author jspacco
 */
public class CBuilder extends Builder<MakeTestProperties> implements TestPropertyKeys {

	/**
	 * Constructor.
	 * 
	 * @param testProperties
	 *            TestProperties loaded from the project jarfile's
	 *            test.properties
	 * @param projectSubmission
	 *            the submission to build
	 * @param directoryFinder
	 *            DirectoryFinder used to locate build and testfiles directories
	 * @param submissionExtractor
	 *            SubmissionExtractor to be used to extract the submission
	 */
	protected CBuilder(MakeTestProperties testProperties,
			ProjectSubmission<MakeTestProperties> projectSubmission,
			DirectoryFinder directoryFinder,
			SubmissionExtractor submissionExtractor) {
		super(testProperties, projectSubmission, directoryFinder,
				submissionExtractor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cs.buildServer.Builder#getProjectPathPrefix()
	 */
	@Override
	protected String getProjectPathPrefix() throws IOException {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cs.buildServer.Builder#compileProject()
	 */
	@Override
	protected void compileProject() throws BuilderException,
			CompileFailureException {

		// Make sure there aren't any old student-compiled test executables
		// hanging around.
		deleteTestExecutables();

		// The test properties may specify the make command.
		String makeCommand = getTestProperties().getMakeCommand();

		// First invoke the student's make command, if any
		String studentMakeFile = getTestProperties().getStudentMakeFile();
		if (studentMakeFile != null) {
			getLog().trace(
					"Invoking student-written makefile " + studentMakeFile);
			// TODO invoke student-written makefile with unprivileged account
			invokeMakeCommand(makeCommand, studentMakeFile);
		}

		// Invoke instructor's make command
		String makeFile = getTestProperties().getMakefileName();

		invokeMakeCommand(makeCommand, makeFile);
	}

	 ExecutorService executor = Executors
	            .newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true)
	                    .build());

	 
	/**
	 * Invoke 'make' for a given makefile. If no makefile exists, the make
	 * command will be invoked with no arguments and use the default makefile in
	 * the directory.
	 * 
	 * @param makeCommand
	 * @param makeFile
	 * @throws CompileFailureException
	 * @throws BuilderException
	 */
	private void invokeMakeCommand(String makeCommand, String makeFile)
			throws CompileFailureException, BuilderException {
		Process process = null;
		boolean finished = false;
		try {
			List<String> args = new LinkedList<String>();
			args.add(makeCommand);
			if (makeFile != null) {
				args.add("-f");
				args.add(makeFile);
			}

			process = Untrusted.executeCombiningOutput(
					getDirectoryFinder().getBuildDirectory(), args.toArray(new String[args.size()]));

			StringWriter makeOutput = new StringWriter();
	        
	        
	        FutureTask<Void> copyMakeOutput = TextDiff.copyTask("copy make output", new InputStreamReader(
	                process.getInputStream()), makeOutput, testProperties.getMaxDrainOutputInBytes());
	        executor.submit(copyMakeOutput);
	        
			ProcessExitMonitor exitMonitor = new ProcessExitMonitor(process, getLog());
           
            long processTimeoutMillis = getTestProperties().getBuildTimeoutInSeconds()*1000L;

            boolean done = exitMonitor.waitForProcessToExit(processTimeoutMillis);
            copyMakeOutput.cancel(true);
			if (done) {

                int exitCode = exitMonitor.getExitCode();
                finished = true;

                if (exitCode != 0) {
                    setCompilerOutput(makeOutput.toString());

                    throw new CompileFailureException("make failed for project " + getProjectSubmission().getZipFile().getPath(),
                    		makeOutput.toString());
                }

                // Wait for a while, to give files a chance to settle
                pause(20);
            } else {
                throw new CompileFailureException("make timed-out" + getProjectSubmission().getZipFile().getPath(),
                		makeOutput.toString());
            }
		} catch (IOException e) {
			throw new BuilderException("Could not execute make", e);
		} finally {
			if (process != null && !finished) {
				process.destroy();
			}
		}
	}

	/**
	 * Delete all test executables from the test directory. We do this in case
	 * students submit them accidentally; if we don't remove them, then the
	 * Makefile might not rebuild them, and we would be executing who-knows-what
	 * as test cases.
	 * 
	 * @throws CompileFailureException
	 * @throws BuilderException
	 */
	private void deleteTestExecutables() throws CompileFailureException,
			BuilderException {
	    
	    for(ExecutableTestCase e : getTestProperties().getExecutableTestCases())  {
	        String exec = e.getProperty(ExecutableTestCase.Property.EXEC).split("\\s+")[0];
	        getLog().info("Cleaning " + exec + " for " + e.getTestType() + " test case " + e.getName());
	                
	        if (!filesExtractedFromTestSetup.contains(exec))
                deleteTestExecutable(exec);
	    }
	 
	}

	/**
	 * Delete a (stale) test executable from the build directory. We do this to
	 * get rid of files that would prevent a fresh test executable from being
	 * built.
	 * 
	 * @param testExe
	 *            the filename of the test executable
	 * @throws CompileFailureException
	 * @throws BuilderException
	 */
	private void deleteTestExecutable(String testExe)
			throws CompileFailureException, BuilderException {
		File testExeFile = new File(getDirectoryFinder().getBuildDirectory(),
				testExe);
		if (testExeFile.exists()) {
			if (testExeFile.isDirectory()) {
				throw new CompileFailureException("Directory " + testExeFile
						+ " in build directory has "
						+ "the same name as a test case", "");
			} else if (!testExeFile.delete()) {
				throw new BuilderException("Could not delete test executable "
						+ testExe + " prior to building project");
			} else {
				getLog().info(
						"Deleted file " + testExe + " in build directory, "
								+ "which would have obscured a test executable");
			}
		}
	}

}

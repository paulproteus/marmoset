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
 * Created on Apr 26, 2005
 */
package edu.umd.cs.buildServer.inspection;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.umd.cs.buildServer.BuilderException;
import edu.umd.cs.buildServer.ProjectSubmission;
import edu.umd.cs.buildServer.tester.JUnitTestCase;
import edu.umd.cs.buildServer.tester.JUnitTestCaseFinder;
import edu.umd.cs.buildServer.util.BuildServerUtilities;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

/**
 * @author jspacco
 * 
 */
public class StudentTestFinderRunner implements ISubmissionInspectionStep {
	private TestOutcomeCollection collection = new TestOutcomeCollection();
	private ProjectSubmission projectSubmission;
	private Set<JUnitTestCase> publicTestSet;

	/**
	 * @return the log
	 */
	public Logger getLog() {
		return projectSubmission.getLog();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.umd.cs.buildServer.ISubmissionInspectionStep#setProjectSubmission
	 * (edu.umd.cs.buildServer.ProjectSubmission)
	 */
	@Override
	public void setProjectSubmission(ProjectSubmission projectSubmission) {
		this.projectSubmission = projectSubmission;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cs.buildServer.ISubmissionInspectionStep#execute()
	 */
	@Override
	public void execute() throws BuilderException {
		getLog().info("execute!");

		JUnitTestCaseFinder testFinder = new JUnitTestCaseFinder();

		File objectFileDir = getProjectSubmission().getBuildOutputDirectory();

		List<File> studentClassFileList = BuildServerUtilities
				.listClassFilesInDirectory(getProjectSubmission()
						.getBuildOutputDirectory());

		for (Iterator<File> i = studentClassFileList.iterator(); i.hasNext();) {
			File classFile = i.next();
			try {
				testFinder.addClassPathEntry(objectFileDir.getAbsolutePath());
				testFinder.setInspectedClass(classFile);
				testFinder.findTestCases();
				if (testFinder.getTestCaseCollection().isEmpty()) {
					getLog().warn("No student tests found in " + classFile);
					continue;
				}
				getLog().info(
						"Found " + testFinder.getTestCaseCollection().size()
								+ " unit tests in class " + classFile);
			} catch (IOException e) {
				getLog().warn(
						"Could not inspect class file " + classFile
								+ " for JUnit tests", e);
				return;
			} catch (ClassNotFoundException e) {
				getLog().warn(
						"Could not inspect class file " + classFile
								+ " for JUnit tests", e);
				return;
			}
		}

		publicTestSet = buildPublicTestSet();
		for (Iterator<JUnitTestCase> ii = testFinder.getTestCaseCollection()
				.iterator(); ii.hasNext();) {
			JUnitTestCase testCase = ii.next();
			if (publicTestSet.contains(testCase)) {
				getLog().info(
						"Test " + testCase + " appears to be a public test");
				continue;
			}
			TestOutcome outcome = new TestOutcome();
			outcome.setTestRunPK(Integer.parseInt(projectSubmission
					.getSubmissionPK()));
			outcome.setTestType("student");
			outcome.setTestName(testCase.getMethodName());
			getTestOutcomeCollection().add(outcome);
			System.out.println(testCase.getMethodName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.umd.cs.buildServer.ISubmissionInspectionStep#getTestOutcomeCollection
	 * ()
	 */
	@Override
	public TestOutcomeCollection getTestOutcomeCollection() {
		return collection;
	}

	/**
	 * @return the projectSubmission
	 */
	public ProjectSubmission getProjectSubmission() {
		return projectSubmission;
	}

	private Set<JUnitTestCase> buildPublicTestSet() {
		Set<JUnitTestCase> publicTestSet = new HashSet<JUnitTestCase>();
		String publicTestClass = projectSubmission.getTestProperties()
				.getTestClass(TestOutcome.PUBLIC_TEST);
		if (publicTestClass != null) {
			JUnitTestCaseFinder publicTestFinder = new JUnitTestCaseFinder();
			publicTestFinder.addClassPathEntry(getProjectSubmission()
					.getTestSetup().getAbsolutePath());
			try {
				publicTestFinder.setInspectedClass(publicTestClass);
				publicTestFinder.findTestCases();

				publicTestSet.addAll(publicTestFinder.getTestCaseCollection());
			} catch (ClassNotFoundException e) {
				getLog().warn("Could not inspect public tests", e);
			}
		}
		return publicTestSet;
	}
}

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
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

import edu.umd.cs.buildServer.ProjectSubmission;
import edu.umd.cs.buildServer.util.BuildServerUtilities;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

/**
 * Extract bytecode features from a compiled submission and report them as
 * TestOutcomes.
 *
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class CodeFeaturesRunner implements ISubmissionInspectionStep {
	private ProjectSubmission projectSubmission;
	private TestOutcomeCollection testOutcomeCollection;

	/**
	 * Constructor.
	 */
	public CodeFeaturesRunner() {
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

	@Override
	public void execute() {
		CodeFeatures visitor = new CodeFeatures();

		List<File> classFiles = BuildServerUtilities
				.listClassFilesInDirectory(projectSubmission
						.getBuildOutputDirectory());
		for (Iterator<File> ii = classFiles.iterator(); ii.hasNext();) {
			File classFile = ii.next();
			try {
				ClassParser parser = new ClassParser(
						classFile.getAbsolutePath());
				JavaClass javaClass = parser.parse();
				javaClass.accept(visitor);
			} catch (IOException e) {
				projectSubmission.getLog().error(
						"Could not parse " + classFile, e);
			}
		}
		StringWriter writer = new StringWriter();
		visitor.report(writer);
		try {
			writer.close();
		} catch (IOException ignore) {
			// ignore
		}
		// create testOutcomes for each feature
		int count = TestOutcome.FIRST_TEST_NUMBER;
		String[] features = writer.getBuffer().toString().split("\n");
		for (int ii = 0; ii < features.length; ii++) {
			String[] nameValue = features[ii].split(":\\s+");
			String featureName = nameValue[0];
			String featureValue = nameValue[1];
			TestOutcome testOutcome = new TestOutcome();
			testOutcome.setTestType(featureName);
			testOutcome.setTestName(featureValue);
			testOutcome.setTestNumber(Integer.toString(count++));
			testOutcome.setOutcome(TestOutcome.FEATURE);
			testOutcome.setShortTestResult(featureValue); // XXX: repeated info
															// - harmless?
			testOutcome.setLongTestResult("");
			testOutcome.setExceptionClassName(null);
			testOutcome.setDetails(null);
			testOutcomeCollection.add(testOutcome);
		}
		projectSubmission.getLog().info(
				"Added " + features.length + " code features as TestOutcomes");
	}

	public static void main(String[] args) {

	}
}

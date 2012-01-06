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
 * Created on Jan 20, 2005
 */
package edu.umd.cs.buildServer.builder;

import edu.umd.cs.buildServer.BuildServerConfiguration;
import edu.umd.cs.buildServer.BuilderException;
import edu.umd.cs.buildServer.ConfigurationKeys;
import edu.umd.cs.buildServer.MissingConfigurationPropertyException;
import edu.umd.cs.buildServer.ProjectSubmission;
import edu.umd.cs.buildServer.tester.JavaTester;
import edu.umd.cs.buildServer.tester.Tester;
import edu.umd.cs.buildServer.util.ArgumentParser;
import edu.umd.cs.marmoset.modelClasses.TestProperties;
import edu.umd.cs.marmoset.utilities.ZipExtractorException;

/**
 * Factory for creating Builder and Tester objects for Java ProjectSubmissions.
 * 
 * @author David Hovemeyer
 */
public class JavaBuilderAndTesterFactory implements BuilderAndTesterFactory {

	private TestProperties testProperties;
	private DirectoryFinder directoryFinder;

	/**
	 * Constructor.
	 * 
	 * @param config
	 *            the build server Configuration
	 * @param testProperties
	 *            TestProperties loaded from test jarfile's test.properties
	 * @throws MissingConfigurationPropertyException
	 */
	public JavaBuilderAndTesterFactory(BuildServerConfiguration config,
			TestProperties testProperties)
			throws MissingConfigurationPropertyException {
		this.testProperties = testProperties;
		this.directoryFinder = new JavaDirectoryFinder(config);
	}

	@Override
	public DirectoryFinder getDirectoryFinder() {
		return directoryFinder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.umd.cs.buildServer.BuilderAndTesterFactory#createBuilder(edu.umd.
	 * cs.buildServer.ProjectSubmission)
	 */
	@Override
	public Builder createBuilder(ProjectSubmission projectSubmission)
			throws MissingConfigurationPropertyException, BuilderException,
			ZipExtractorException {
		JavaSubmissionExtractor submissionExtractor = new JavaSubmissionExtractor(
				projectSubmission.getZipFile(),
				directoryFinder.getBuildDirectory(), projectSubmission.getLog());
		// If the buildserver configuration specifies source files to exclude,
		// add them to the SubmissionExtractor
		String excludedSourceFileList = projectSubmission.getConfig().getConfig()
				.getOptionalProperty(
						ConfigurationKeys.EXCLUDED_SOURCE_FILE_LIST);
		if (excludedSourceFileList != null) {
			ArgumentParser parser = new ArgumentParser(excludedSourceFileList);
			while (parser.hasNext()) {
				submissionExtractor.addExcludedSourceFilePattern(parser.next());
			}
		}

		Builder builder = new JavaBuilder(testProperties, projectSubmission,
				directoryFinder, submissionExtractor);

		builder.addExpectedFile(projectSubmission.getZipFile().getName());
		builder.addExpectedFile(projectSubmission.getTestSetup().getName());
		builder.addExpectedFile("test.properties");
		builder.addExpectedFile("security.policy");

		return builder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cs.buildServer.BuilderAndTesterFactory#createTester(boolean,
	 * edu.umd.cs.buildServer.ProjectSubmission)
	 */
	@Override
	public Tester createTester(boolean haveSecurityPolicyFile,
			ProjectSubmission projectSubmission)
			throws MissingConfigurationPropertyException {
		return new JavaTester(testProperties, haveSecurityPolicyFile,
				projectSubmission, directoryFinder);
	}
}

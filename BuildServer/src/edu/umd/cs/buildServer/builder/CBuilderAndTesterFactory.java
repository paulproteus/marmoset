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

import edu.umd.cs.buildServer.BuildServerConfiguration;
import edu.umd.cs.buildServer.BuilderException;
import edu.umd.cs.buildServer.MissingConfigurationPropertyException;
import edu.umd.cs.buildServer.ProjectSubmission;
import edu.umd.cs.buildServer.tester.CTester;
import edu.umd.cs.buildServer.tester.Tester;
import edu.umd.cs.marmoset.modelClasses.TestProperties;
import edu.umd.cs.marmoset.utilities.ZipExtractorException;

/**
 * Factory for producing Builder and Tester objects for C, OCaml and Ruby
 * ProjectSubmissions.
 * <p>
 * <b>NOTE:</b> "CBuilderAndTester" is a legacy name. We use the same
 * infrastructure for building and testing C, OCaml and Ruby code because the
 * process is exactly the same. For more details see {@see CBuilder}.
 *
 * @author David Hovemeyer
 * @author jspacco
 */
public class CBuilderAndTesterFactory implements BuilderAndTesterFactory {

	private TestProperties testProperties;
	private DirectoryFinder directoryFinder;

	/**
	 * Constructor.
	 *
	 * @param config
	 *            the build server Configuration
	 * @param testProperties
	 *            Properties loaded from test jarfile's test.properties
	 * @throws MissingConfigurationPropertyException
	 */
	public CBuilderAndTesterFactory(BuildServerConfiguration config,
			TestProperties testProperties)
			throws MissingConfigurationPropertyException {
		this.testProperties = testProperties;
		this.directoryFinder = new CDirectoryFinder(config);
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
			throws BuilderException, MissingConfigurationPropertyException,
			ZipExtractorException {
		CSubmissionExtractor submissionExtractor = new CSubmissionExtractor(
				projectSubmission.getZipFile(),
				directoryFinder.getBuildDirectory(), projectSubmission.getLog());

		CBuilder builder = new CBuilder(testProperties, projectSubmission,
				directoryFinder, submissionExtractor);
		builder.addExpectedFile(projectSubmission.getZipFile().getName());
		builder.addExpectedFile("test.properties");

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

		CTester tester = new CTester(testProperties, haveSecurityPolicyFile,
				projectSubmission, directoryFinder);

		return tester;
	}

}

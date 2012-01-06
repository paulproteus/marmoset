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
 * Created on Jan 19, 2005
 */
package edu.umd.cs.buildServer;

import java.io.File;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.log4j.Logger;

import edu.umd.cs.buildServer.builder.BuilderAndTesterFactory;
import edu.umd.cs.buildServer.builder.CBuilderAndTesterFactory;
import edu.umd.cs.buildServer.builder.JavaBuilderAndTesterFactory;
import edu.umd.cs.marmoset.modelClasses.CodeMetrics;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestProperties;
import edu.umd.cs.marmoset.modelClasses.TestPropertyKeys;

/**
 * A project submission to be compiled and tested. This object stores all of the
 * state information about a submission to avoid having to pass the state as
 * parameters to the various BuildServer subsystems.
 *
 * @author David Hovemeyer
 */
public class ProjectSubmission implements ConfigurationKeys, TestPropertyKeys {
	private BuildServerConfiguration config;
	private Logger log;
	private String submissionPK;
	private String testSetupPK;
	private String isNewTestSetup;
	private String isBackgroundRetest;
	/**
	 * Auxiliary information about the source to be built, such as an md5sum of
	 * the classfiles and/or the names of student-written tests. So far we don't
	 * compute any auxiliary information for C builds.
	 */
	private CodeMetrics codeMetrics;

	private File zipFile;
	private File testSetup;

	private TestOutcomeCollection testOutcomeCollection;

	private HttpMethod method;
	private TestProperties testProperties;
	private BuilderAndTesterFactory builderAndTesterFactory;

	/**
	 * Constructor.
	 *
	 * @param config
	 *            the BuildServer's Configuration
	 * @param log
	 *            the BuildServer's Log
	 * @param submissionPK
	 *            the submission PK
	 * @param projectJarfilePK
	 *            the project jarfile PK
	 * @param isNewTestSetup
	 *            boolean: whether or not the submission is for a new project
	 *            jarfile being tested with the canonical project solution
	 * @throws MissingConfigurationPropertyException
	 */
	public ProjectSubmission(BuildServerConfiguration config, Logger log,
			String submissionPK, String projectJarfilePK,
			String isNewTestSetup, String isBackgroundRetest)
			throws MissingConfigurationPropertyException {
		this.config = config;
		this.log = log;
		this.submissionPK = submissionPK;
		this.testSetupPK = projectJarfilePK;
		this.isNewTestSetup = isNewTestSetup;
		this.isBackgroundRetest = isBackgroundRetest;

		// Choose a name for the zip file based on
		// the build directory and the submission PK.
		File zipFileName = new File(config.getBuildDirectory(),
		        "submission_" + getSubmissionPK() + ".zip");
		this.setZipFile(zipFileName);

		// Choose a name for the project jar file based
		// on the build directory and the project PK.
		File projectJarFileName = new File(
				config.getJarCacheDirectory(), "proj_"
						+ getTestSetupPK() + ".jar");
		this.setTestSetup(projectJarFileName);

		this.testOutcomeCollection = new TestOutcomeCollection();
	}

	/**
	 * @return Returns the config.
	 */
	public BuildServerConfiguration getConfig() {
		return config;
	}

	/**
	 * @return Returns the log.
	 */
	public Logger getLog() {
		return log;
	}

	/**
	 * @return Returns the isNewTestSetup value.
	 */
	public String getIsNewTestSetup() {
		return isNewTestSetup;
	}

	/**
	 * @return Returns the projectJarfilePK.
	 */
	public String getTestSetupPK() {
		return testSetupPK;
	}

	/**
	 * @return Returns the submissionPK.
	 */
	public String getSubmissionPK() {
		return submissionPK;
	}

	/**
	 * Get File storing submission zip file.
	 *
	 * @return the File storing the submission zip file
	 */
	public File getZipFile() {
		return zipFile;
	}

	/**
	 * Get the File storing the project jar file.
	 *
	 * @return the File storing the project jar file
	 */
	public File getTestSetup() {
		return testSetup;
	}

	/**
	 * @return Returns the testOutcomeCollection.
	 */
	public TestOutcomeCollection getTestOutcomeCollection() {
		return testOutcomeCollection;
	}

	/**
	 * @param method
	 *            The method to set.
	 */
	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	/**
	 * @return Returns the method.
	 */
	public HttpMethod getMethod() {
		return method;
	}

	/**
	 * Set the TestProperties.
	 *
	 * @param testProperties
	 *            the TestProperties
	 */
	public void setTestProperties(TestProperties testProperties) {
		this.testProperties = testProperties;
	}

	/**
	 * Get the TestProperties.
	 *
	 * @return the TestProperties
	 */
	public TestProperties getTestProperties() {
		return testProperties;
	}

	/**
	 * Based on the language specified in test.properties, create a
	 * BuilderAndTesterFactory.
	 *
	 * @return a BuilderAndTesterFactory to be used to build and test the
	 *         submission
	 * @throws BuilderException
	 */
	public BuilderAndTesterFactory createBuilderAndTesterFactory()
			throws BuilderException {
		try {
			String language = testProperties.getLanguage();
			if (language.equals(JAVA)) {
				this.builderAndTesterFactory = new JavaBuilderAndTesterFactory(
						getConfig(), testProperties);
			} else if (language.equals(C) || language.equals(OCAML)
					|| language.equals(RUBY)) {
				// XXX The CBuilder and CTester are also used for OCaml and Ruby
				// projects.
				// The CBuilder and CTester are flexible and only require a
				// Makefile and
				// a list of test executables that return zero to signal passing
				// and non-zero
				// to signal failure.
				this.builderAndTesterFactory = new CBuilderAndTesterFactory(
						getConfig(), testProperties);
			} else {
				throw new BuilderException(
						"Unknown language specified in test.properties: "
								+ language);
			}
		} catch (MissingConfigurationPropertyException e) {
			throw new BuilderException(
					"Could not create builder/tester factory for submission", e);
		}
		return builderAndTesterFactory;
	}

	/**
	 * Get the BuilderAndTesterFactory.
	 *
	 * @return the BuilderAndTesterFactory
	 */
	public BuilderAndTesterFactory getBuilderAndTesterFactory() {
		return builderAndTesterFactory;
	}

	/**
	 * Should we use the directory with src code instrumented for code coverage?
	 *
	 * TODO Instrumented source is specific to Clover; other code coverage tools
	 * (such as Emma) don't have instrument the source and so make this step
	 * unnecessary. It's still not clear how to integrate everything together.
	 *
	 * @return True if we should use the src directory instrumented for code
	 *         coverage; false otherwise.
	 *         <p>
	 *         TODO If the buildServer's configuration asks for code coverage,
	 *         but we notice that we don't have permission to read and write the
	 *         directory where the code coverage data is being written, then we
	 *         need to either:
	 *         <ul>
	 *         <li>over-ride the code coverage setting or else all the test
	 *         outcomes will fail.
	 *         <li>add the necessary permissions to the security policy file.
	 *         </ul>
	 *         This would be easy if there were some way to ask a
	 *         security.policy file what permissions it is granting. I don't
	 *         know if this is possible or how to do so. Future work.
	 *
	 */
	public boolean isPerformCodeCoverage() {
		return getTestProperties().isPerformCodeCoverage();
	}

	/**
	 * Get the build output directory. It is not legal to call this method until
	 * the BuilderAndTesterFactory has been created.
	 *
	 * @return the File specifying the build output directory
	 */
	public File getBuildOutputDirectory() {
		return new File(getBuilderAndTesterFactory().getDirectoryFinder()
				.getBuildDirectory(), BuildServer.BUILD_OUTPUT_DIR);
	}

	public File getSrcDirectory() {
		return new File(getBuilderAndTesterFactory().getDirectoryFinder()
				.getBuildDirectory(), BuildServer.SOURCE_DIR);
	}

	public File getInstSrcDirectory() {
		return new File(getBuilderAndTesterFactory().getDirectoryFinder()
				.getBuildDirectory(), BuildServer.INSTRUMENTED_SOURCE_DIR);
	}


	public String getIsBackgroundRetest() {
		return isBackgroundRetest;
	}

	public void setCodeMetrics(CodeMetrics codeMetrics) {
		this.codeMetrics = codeMetrics;
	}

	public CodeMetrics getCodeMetrics() {
		return codeMetrics;
	}

	public void setTestSetup(File testSetup) {
		this.testSetup = testSetup;
	}

	public void setZipFile(File zipFile) {
		this.zipFile = zipFile;
	}
}

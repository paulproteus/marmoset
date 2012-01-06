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

import edu.umd.cs.buildServer.BuilderException;
import edu.umd.cs.buildServer.MissingConfigurationPropertyException;
import edu.umd.cs.buildServer.ProjectSubmission;
import edu.umd.cs.buildServer.tester.Tester;
import edu.umd.cs.marmoset.utilities.ZipExtractorException;

/**
 * Factory for creating Builder and Tester objects for a ProjectSubmission.
 * 
 * @author David Hovemeyer
 */
public interface BuilderAndTesterFactory {
	public DirectoryFinder getDirectoryFinder();

	/**
	 * Create a Builder for a ProjectSubmission.
	 * 
	 * @param projectSubmission
	 *            the ProjectSubmission to build
	 * @return a Builder which can build the ProjectSubmission
	 * @throws BuilderException
	 * @throws MissingConfigurationPropertyException
	 */
	public Builder createBuilder(ProjectSubmission projectSubmission)
			throws BuilderException, MissingConfigurationPropertyException,
			ZipExtractorException;

	/**
	 * Create a Tester for a ProjectSubmission.
	 * 
	 * @param haveSecurityPolicyFile
	 *            true if there is a security.policy file
	 * @param projectSubmission
	 *            the ProjectSubmission to test
	 * @return a Tester which can test the ProjectSubmission
	 * @throws MissingConfigurationPropertyException
	 */
	public Tester createTester(boolean haveSecurityPolicyFile,
			ProjectSubmission projectSubmission)
			throws MissingConfigurationPropertyException;
}

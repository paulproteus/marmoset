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

import edu.umd.cs.buildServer.BuildServerConfiguration;
import edu.umd.cs.buildServer.ConfigurationKeys;
import edu.umd.cs.buildServer.MissingConfigurationPropertyException;

/**
 * DirectoryFinder for C, OCaml and Ruby projects.
 * <p>
 * <b>NOTE:</b> "CSubmissionExtractor" is a legacy name. We use the same
 * infrastructure for for building and testing C, OCaml and Ruby code because
 * the process is exactly the same. For more details see {@see CBuilder}.
 * 
 * @author David Hovemeyer
 */
public class CDirectoryFinder extends DirectoryFinder implements
		ConfigurationKeys {

	private File buildDirectory;

	/**
	 * Constructor.
	 * 
	 * @param config
	 *            the BuildServer Configuration
	 * @throws MissingConfigurationPropertyException
	 */
	public CDirectoryFinder(BuildServerConfiguration config)
			throws MissingConfigurationPropertyException {
		super(config);
		this.buildDirectory = config.getBuildDirectory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cs.buildServer.DirectoryFinder#getBuildDirectory()
	 */
	@Override
	public File getBuildDirectory() {
		return buildDirectory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cs.buildServer.DirectoryFinder#getTestFilesDirectory()
	 */
	@Override
	public File getTestFilesDirectory() {
		// For C submissions, the test files are extracted into the
		// build directory, not the testfiles directory.
		return buildDirectory;
	}

}

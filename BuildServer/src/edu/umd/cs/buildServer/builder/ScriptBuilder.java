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

import java.io.IOException;

import edu.umd.cs.buildServer.BuilderException;
import edu.umd.cs.buildServer.CompileFailureException;
import edu.umd.cs.buildServer.ProjectSubmission;
import edu.umd.cs.marmoset.modelClasses.ScriptTestProperties;
import edu.umd.cs.marmoset.modelClasses.TestPropertyKeys;


public class ScriptBuilder extends Builder<ScriptTestProperties> implements TestPropertyKeys {

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
	protected ScriptBuilder(ScriptTestProperties testProperties,
			ProjectSubmission<ScriptTestProperties> projectSubmission,
			DirectoryFinder directoryFinder,
			SubmissionExtractor submissionExtractor) {
		super(testProperties, projectSubmission, directoryFinder,
				submissionExtractor);
	}

	@Override
	protected String getProjectPathPrefix() throws IOException {
		return "";
	}


	@Override
	protected void compileProject() throws BuilderException,
			CompileFailureException {

		// nothing to do
	}

	

}

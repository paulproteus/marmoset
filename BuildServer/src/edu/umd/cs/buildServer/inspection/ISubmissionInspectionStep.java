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
 * Created on Mar 28, 2005
 */
package edu.umd.cs.buildServer.inspection;

import edu.umd.cs.buildServer.BuilderException;
import edu.umd.cs.buildServer.ProjectSubmission;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

/**
 * An analysis step for examining a compiled submission and reporting test
 * outcomes on it.
 * 
 * @author David Hovemeyer
 */
public interface ISubmissionInspectionStep {
	/**
	 * Set the ProjectSubmission to inspect.
	 * 
	 * @param projectSubmission
	 *            the ProjectSubmission to inspect
	 */
	public void setProjectSubmission(ProjectSubmission projectSubmission);

	/**
	 * Execute the analysis.
	 * 
	 * @throws BuilderException
	 */
	public void execute() throws BuilderException;

	/**
	 * Get the TestOutcomeCollection containing TestOutcomes reported by this
	 * inspection step.
	 * 
	 * @return the TestOutcomeCollection
	 */
	public TestOutcomeCollection getTestOutcomeCollection();
}

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

/**
 * @author jspacco
 */
package edu.umd.cs.marmoset.modelClasses;

/**
 * @author jspacco
 *
 */
public interface ITestSummary<T extends ITestSummary<T>> extends Comparable<T>
{


	/**
     * Computes the total number of points received.
     * <p><b>NOTE</b>Does not subtract points for test categories that were
     * marked COULD_NOT_RUN and therefore returned -1.
     * @return total number of points received for this submission;
     * -1 if the submission did not compile.
     */
    public int getValuePassedOverall();

    /**
     * @return true if the TestSummary compiled, false otherwise
     */
    public boolean isCompileSuccessful();

    /**
     * Gets the total number of points received for public tests.
     * @return total number of points received for public tests;
     * return -1 if the public tests are marked COULD_NOT_RUN;
     * returns 0 if there are no public tests.
     */
    public int getValuePublicTestsPassed();

    /**
     * Gets the total number of points received for release tests.
     * @return total number of points received for release tests;
     * return -1 if the release tests are marked COULD_NOT_RUN;
     * returns 0 if there are no release tests.
     */
    public int getValueReleaseTestsPassed();

    /**
     * Gets the total number of points received for secret tests.
     * @return total number of points received for secret tests;
     * return -1 if the secret tests are marked COULD_NOT_RUN;
     * returns 0 if there are no secret tests.
     */
    public int getValueSecretTestsPassed();

    /**
     * Gets the number of FindBugs warnings.
     * This is a raw count of the number of warnings.
     * @return the number of FindBugs warnings.
     */
    public int getNumFindBugsWarnings();
}

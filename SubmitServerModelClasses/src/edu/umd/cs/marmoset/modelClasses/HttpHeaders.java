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

package edu.umd.cs.marmoset.modelClasses;

/**
 * @author jspacco
 *
 */
public final class HttpHeaders
{
    public static final String HTTP_SUBMISSION_PK_HEADER = "X-SubmitServer-Submission_PK";
    public static final String HTTP_TEST_SETUP_PK_HEADER = "X-SubmitServer-TestSetup_PK";

    public static final String HTTP_NEW_TEST_SETUP = "X-SubmitServer-NewTestSetup";

    public static final String HTTP_BACKGROUND_RETEST = "X-SubmitSever-BackgroundRetest";
    public static final String HTTP_HUH_HEADER = "X-SubmitServer-Huh";
    public static final String HTTP_KIND_HEADER = "X-SubmitServer-Kind";
    public static final String HTTP_PRIORITY_HEADER = "X-SubmitServer-Priority";
    public static final String HTTP_PREVIOUS_BUILD_STATUS_HEADER = "X-SubmitServer-Previous-BuildStatus";
}

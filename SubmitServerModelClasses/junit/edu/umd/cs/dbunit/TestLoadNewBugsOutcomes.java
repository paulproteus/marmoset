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

package edu.umd.cs.dbunit;

import java.sql.Connection;
import java.util.Random;

import junit.framework.TestCase;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestRun;
import edu.umd.cs.marmoset.utilities.DatabaseUtilities;

/**
 * @author jspacco
 *
 */
public class TestLoadNewBugsOutcomes extends TestCase
{

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestLoadNewBugsOutcomes.class);
    }

    public void testLoadNewFindBugsOutcomes()
    throws Exception
    {
        Connection conn=null;
        try {
            conn = DatabaseUtilities.getConnection();

            @Submission.PK  int submissionPK = 56107;

            TestOutcomeCollection newBugCollection = new TestOutcomeCollection();

            Random r = new Random();
            int totalNewFindBugsOutcomes = r.nextInt(5) + 1;

            for (int testNumber=0; testNumber < totalNewFindBugsOutcomes; testNumber++)
            {
                TestOutcome outcome = new TestOutcome();

                outcome.setTestType(TestOutcome.FINDBUGS_TEST);
                outcome.setTestNumber(Integer.toString(testNumber));
                outcome.setOutcome(TestOutcome.STATIC_ANALYSIS);
                outcome.setPointValue(0);
                outcome.setTestName("FAKE_WARNING_TESTING_PURPOSES");
                outcome.setShortTestResult("Foo.java:[line " +testNumber+ "]");
                outcome.setLongTestResult("This is actually a fake warning for testing purposes");
                outcome.setExceptionClassName(null);
                outcome.setDetails(null);

                newBugCollection.add(outcome);
            }

            System.out.println("loading " +totalNewFindBugsOutcomes+ " new findbugs outcomes");

            Submission oldSubmission = Submission.lookupBySubmissionPK(submissionPK, conn);
            System.err.println("oldSubmission.getCurrentTestRunPK() = " +oldSubmission.getCurrentTestRunPK());

            Submission.loadNewFindBugsOutcomes(newBugCollection, submissionPK, conn);

            Submission newSubmission = Submission.lookupBySubmissionPK(submissionPK, conn);
            System.err.println("newSubmission.getCurrentTestRunPK() = " +newSubmission.getCurrentTestRunPK());

            assertNotSame(oldSubmission.getCurrentTestRunPK(), newSubmission.getCurrentTestRunPK());

            assertEquals(newBugCollection.size(), newSubmission.getNumFindBugsWarnings());

            TestRun newTestRun = TestRun.lookupByTestRunPK(newSubmission.getCurrentTestRunPK(), conn);
            assertEquals(newBugCollection.size(), newTestRun.getNumFindBugsWarnings());

        } finally {
            DatabaseUtilities.releaseConnection(conn);
        }
    }

}

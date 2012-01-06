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

import java.sql.Connection;

import junit.framework.TestCase;
import edu.umd.cs.marmoset.utilities.DatabaseUtilities;

/**
 * TestOutcomeTest
 * @author jspacco
 */
public class TestOutcomeTest extends TestCase
{

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestOutcomeTest.class);
    }

	private Connection conn;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (false) {
			String databaseUrl = System.getProperty("database.jdbc.url");
			if (databaseUrl == null) {
				throw new Exception(
						"Cannot connect to database host: system property "
								+ "'database.jdbc.url' is not defined");
			} else {
				conn = DatabaseUtilities.getConnection(databaseUrl);
			}
		} else
			conn = DatabaseUtilities.getConnection();

	}

    @Override
	protected void tearDown() throws Exception
    {
        super.tearDown();
        DatabaseUtilities.releaseConnection(conn);
    }
    
    private TestOutcome lookupByTestRunPK(int testRunPK, String testType, int testNumber)
    throws Exception
    {
        TestOutcomeCollection collection=TestOutcomeCollection.lookupByTestRunPK(testRunPK, conn);
        return collection.getOutcomeByTestTypeAndTestNumber(testType, Integer.toString(testNumber));
    }
    
    public void testCoversLineOrPreviousThreeLines()
    throws Exception
    {
        int testRunPK = 99988;
        TestOutcomeCollection collection=TestOutcomeCollection.lookupByTestRunPK(testRunPK,conn);
        TestOutcome outcome=collection.getOutcomeByTestTypeAndTestNumber(TestOutcome.RELEASE_TEST, "10");
        System.out.println(outcome.getExceptionSourceFromLongTestResult());
        System.out.println(collection.isExceptionSourceApproximatelyCovered(outcome, 3));
        
        assertEquals(true, collection.isExceptionSourceApproximatelyCovered(outcome, 3));
        
    }

}

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

package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.UserSession;

public class PrintTestOutcome extends SubmitServerServlet {


	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	    HttpSession session = request.getSession();
        
	    UserSession userSession = (UserSession) session
                .getAttribute(USER_SESSION);

		PrintWriter out = response.getWriter();
		TestOutcomeCollection testOutcomeCollection =  (TestOutcomeCollection) request.getAttribute(TEST_OUTCOME_COLLECTION);

		response.setContentType("text/plain");
		String testType = request.getParameter("testType");
		String testNumber = request.getParameter("testNumber");
        
		if (testType.equals(TestOutcome.RELEASE_TEST) || testType.equals(TestOutcome.SECRET_TEST)) {
            if (!((Boolean) request
                    .getAttribute(SubmitServerConstants.INSTRUCTOR_CAPABILITY))
                    .booleanValue()) {
                    out.println("Must be an instructor to print release or secret test results");
                    return;
                }
                
            }
        
		TestOutcome testoutcome = testOutcomeCollection.getTest(testType, testNumber);
	  
	    if (testoutcome == null) {
	        out.println("No such test ");
	        return;
	    }
		
		out.printf("name: %s%n", testoutcome.getShortTestName());
		out.printf("outcome: %s%n", testoutcome.getOutcome());
		out.printf("result: %s%n", testoutcome.getShortTestResult());
		out.println();
		out.println(testoutcome.getLongTestResult());

	}

}

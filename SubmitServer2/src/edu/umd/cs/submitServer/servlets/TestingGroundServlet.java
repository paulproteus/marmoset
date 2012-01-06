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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.ServerError;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.utilities.Multiset;

public class TestingGroundServlet extends SubmitServerServlet {

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 *
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");
		
		Connection conn = null;
		try {
		    conn = getConnection();
		    
		    Timestamp since = new Timestamp(System.currentTimeMillis() 
		            - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
		    
		    
		    List<ServerError> errors = ServerError.recentErrors(10, since, conn);
		    for(ServerError e : errors) {
		        out.printf("%20s %s%n", e.getWhen(), e.getMessage());
		    }
		    out.println();
	           
		    Multiset<Date> dates = Submission.lookupSubmissionTimes(since, conn);
		    out.println(dates.uniqueKeys().size() + " unique times");
		    for(Map.Entry<Date, Integer> e : dates.entrySet()) {
		        out.printf("%5d %s\n", e.getValue(), e.getKey());
		    }
		    out.flush();
		    
		}  catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }


	}

}

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
 * Created on Dec 5, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.filters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.zip.ZipInputStream;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import edu.umd.cs.marmoset.modelClasses.MissingRequiredTestPropertyException;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.TestProperties;
import edu.umd.cs.marmoset.modelClasses.TestRun;
import edu.umd.cs.marmoset.modelClasses.TestSetup;

/**
 * TestPropertiesFilter
 * 
 * @author jspacco
 */
public class TestPropertiesFilter extends SubmitServerFilter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		Connection conn = null;
		try {
			conn = getConnection();
			Project project = (Project) request.getAttribute(PROJECT);
			TestSetup testSetup = null;
			if (project != null) {
			    request.setAttribute(THIS_PROJECT_BUILD_STATUS_COUNT, project.getBuildStatusCount(conn));
	            
				testSetup = TestSetup.lookupByTestSetupPK(
						project.getTestSetupPK(), conn);
			}
			if (testSetup == null) {
				TestRun testRun = (TestRun) request.getAttribute("testRun");
				if (testRun != null)
					testSetup = TestSetup.lookupByTestSetupPK(
							testRun.getTestSetupPK(), conn);
			}
			if (testSetup != null) {
				ZipInputStream zipIn = new ZipInputStream(
						new ByteArrayInputStream(
								testSetup.downloadArchive(conn)));
				TestProperties testProperties =  TestProperties.load(zipIn);
				request.setAttribute(TEST_PROPERTIES, testProperties);
			}
		} catch (MissingRequiredTestPropertyException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
		chain.doFilter(request, resp);
	}

}

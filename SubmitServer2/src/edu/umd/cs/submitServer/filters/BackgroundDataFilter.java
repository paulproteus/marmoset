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
 * Created on Apr 13, 2005
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.BackgroundData;
import edu.umd.cs.marmoset.modelClasses.IncorrectBackgroundDataException;
import edu.umd.cs.submitServer.CheckedFormManager;
import edu.umd.cs.submitServer.UserSession;

/**
 * @author jspacco
 * 
 */
public class BackgroundDataFilter extends SubmitServerFilter {

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
		HttpServletResponse response = (HttpServletResponse) resp;
		HttpSession session = request.getSession();
		UserSession userSession = (UserSession) session
				.getAttribute(USER_SESSION);

		Connection conn = null;
		try {
			conn = getConnection();

			BackgroundData backgroundData = BackgroundData.lookupByStudentPK(
					userSession.getStudentPK(), conn);

			// unfinishedData is a message to the user (in red at the top)
			// describing what part of the form is unfinished
			String unfinishedMessage = "";
			if (backgroundData == null) {
				backgroundData = new BackgroundData();
			} else {
				try {
					backgroundData.verifyFormat();
				} catch (IncorrectBackgroundDataException e) {
					// the exceptin will give us the infinished message
					unfinishedMessage = e.getMessage();
				}
			}

			userSession.setBackgroundDataComplete(backgroundData.isComplete());

			// CheckedFormManager is a meta-class that tells the jsp form which
			// fields
			// should be checked to what values
			// TODO There must be a better way to handle partially-completed
			// forms?
			CheckedFormManager checkedFormManager = new CheckedFormManager(
					backgroundData);

			request.setAttribute("checkedFormManager", checkedFormManager);
			request.setAttribute("unfinishedMessage", unfinishedMessage);
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
		chain.doFilter(request, response);
	}

}

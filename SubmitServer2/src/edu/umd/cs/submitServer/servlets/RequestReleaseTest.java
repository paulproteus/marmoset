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
 * Created on Jan 14, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.ReleaseInformation;
import edu.umd.cs.submitServer.RequestParser;
import edu.umd.cs.submitServer.UserSession;

/**
 * @author jspacco
 *
 */
public class RequestReleaseTest extends SubmitServerServlet {

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO make fetching the userSession a method in
		// SubmitServerServlet/SubmitServerFilter
		HttpSession session = request.getSession();
		UserSession userSession = (UserSession) session
				.getAttribute(USER_SESSION);

		boolean transactionSuccess = false;
		Connection conn = null;
		try {
			conn = getConnection();

			// fetch submissionPK parameter from request
			RequestParser parser = new RequestParser(request,
					getSubmitServerServletLog(), strictParameterChecking());
			 @Submission.PK int submissionPK = Submission.asPK(parser.getIntParameter("submissionPK"));

			// XXX CANNOT use SubmissionFilter because I need a transaction
			// start new transaction
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

			// fetch submission
			Submission submission = Submission
					.lookupByStudentPKAndSubmissionPK(
							userSession.getStudentPK(), submissionPK, conn);

			if (submission == null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"There is no submission with submissionPK "
								+ submissionPK);
				return;
			}

			// fetch project
			Project project = Project.getByProjectPK(submission.getProjectPK(),
					conn);
			// fetch previous release-tested submissions
			List<Submission> submissionList = Submission
					.lookupAllForReleaseTesting(userSession.getStudentPK(),
							project.getProjectPK(), conn);

			ReleaseInformation releaseInformation = new ReleaseInformation(
					project, submissionList);
			if (!releaseInformation.isReleaseRequestOK()) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN,
						"You don't have enough release tokens remaining!  Tokens remaining: "
								+ releaseInformation.getTokensRemaining());
				return;
			}

			Timestamp now = new Timestamp(System.currentTimeMillis());
			submission.setReleaseRequest(now);

			submission.update(conn);

			conn.commit();
			transactionSuccess = true;

			// redirect to /view/oneSubmission.jsp?submissionPK=submissionPK
			String target = request.getContextPath()
					+ "/view/submission.jsp?submissionPK=" + submissionPK;

			response.sendRedirect(target);
		} catch (InvalidRequiredParameterException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			handleSQLException(e);
			throw new ServletException(e);
		} finally {
			rollbackIfUnsuccessfulAndAlwaysReleaseConnection(
					transactionSuccess, request, conn);
			releaseConnection(conn);
		}
	}

}

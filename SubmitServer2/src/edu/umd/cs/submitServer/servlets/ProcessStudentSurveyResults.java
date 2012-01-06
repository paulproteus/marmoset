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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Queries;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.utilities.SqlUtilities;

public class ProcessStudentSurveyResults extends SubmitServerServlet {

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to
	 * post.
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
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Connection conn = null;
		boolean transactionSuccess = false;
		PreparedStatement stmt = null;
		try {
			conn = getConnection();

			Student student = (Student) request.getAttribute(STUDENT);

			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

			makePreparedStatementFromMap(request.getParameterMap(), conn);

			stmt = conn
					.prepareStatement("INSERT INTO survey_responses SET student_pk = ?");
			SqlUtilities.setInteger(stmt, 1, student.getStudentPK());
			stmt.executeUpdate();

			conn.commit();
			transactionSuccess = true;

			response.sendRedirect(request.getContextPath() + "/view");

		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			rollbackIfUnsuccessfulAndAlwaysReleaseConnection(
					transactionSuccess, request, conn);
			Queries.closeStatement(stmt);
		}
	}

	private static void makePreparedStatementFromMap(Map<String, String[]> map,
			Connection conn) throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = " INSERT INTO spring2006_survey " + " SET ";
			Pattern acceptableKey = Pattern.compile("[A-Za-z0-9_]+");
			List<String> list = new LinkedList<String>();

			String[] wantsAnonymous = map.get("anonymous");
			// Default is anonymous, so if anonymous is null assume they want to
			// be anonymous
			boolean anonymous = wantsAnonymous == null
					|| Boolean.valueOf(wantsAnonymous[0]);
			boolean first = true;
			for (Map.Entry<String, String[]> e : map.entrySet()) {
				String s = e.getKey();
				if (s.endsWith("__survey")) {
					if (first)
						first = false;
					else
						sql += ", ";
					String key = s.replace("__survey", "");
					if (!acceptableKey.matcher(key).matches())
						continue;
					sql += key + " = ? ";

					// Make sure that if the student wanted things anonymous,
					// that we throw
					// away their student_pk
					if (anonymous && s.contains("student_pk")) {
						list.add("0");
					} else
						list.add(e.getValue()[0]);
				}
			}
			stmt = conn.prepareStatement(sql);
			int index = 1;
			for (String s : list) {
				stmt.setString(index++, s);
			}

			stmt.executeUpdate();

		} finally {
			Queries.closeStatement(stmt);
		}
	}
}

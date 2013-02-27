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
 * Created on Mar 5, 2005
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.RequestParser;

/**
 * @author jspacco
 *
 */
public class UpdateProject extends SubmitServerServlet {

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Connection conn = null;
		try {
			conn = getConnection();
			RequestParser parser = new RequestParser(request,
					getSubmitServerServletLog(), strictParameterChecking());

			Project project = Project.lookupByProjectPK(parser.getIntegerParameter("projectPK", null), conn);
			boolean wasTested = project.isTested();
			parser.updateProject(project);
			project.update(conn);
			Project project2 = Project.lookupByProjectPK(project.getProjectPK(), conn);
			if (!project.getTitle().equals(project2.getTitle()))
			  System.out.println(project2.getTitle());
     if (project.isTested() != wasTested)
				Submission.setBuildStatusForAllSubmissions(conn, project);

			String redirectUrl = request.getContextPath()
					+ "/view/instructor/projectUtilities.jsp?projectPK="
					+ project.getProjectPK();
			response.sendRedirect(redirectUrl);
		} catch (InvalidRequiredParameterException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			handleSQLException(e);
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
	}

}

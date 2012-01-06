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
 * Created on Jun 24, 2005
 */
package edu.umd.cs.submitServer.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import edu.umd.cs.marmoset.modelClasses.EclipseLaunchEvent;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.StudentSubmitStatus;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.MultipartRequest;
import edu.umd.cs.submitServer.filters.ServletExceptionFilter;

/**
 * @author jspacco
 *
 */
public class LogEclipseLaunchEvent extends SubmitServerServlet {


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
		BufferedReader reader = null;
		try {
			MultipartRequest multipartRequest = (MultipartRequest) request
					.getAttribute(MULTIPART_REQUEST);

			long clientTime = multipartRequest.getLongParameter("clientTime");
			long serverTime = System.currentTimeMillis();
			// Compute the "skew" between client and server in minutes.
			// This implicitly throw out things that are < 1 min so we lose the
			// regular
			// lagtime it takes to upload the submission and post the launch
			// events.
			int skew = (int) ((serverTime - clientTime) / 1000 / 60);

			StudentRegistration registration = (StudentRegistration) request
					.getAttribute("studentRegistration");
			Project project = (Project) request.getAttribute("project");

			FileItem fileItem = multipartRequest.getFileItem();
			reader = new BufferedReader(new InputStreamReader(
					fileItem.getInputStream()));
			String prevLine = null;
			conn = getConnection();
			PreparedStatement stmt = EclipseLaunchEvent.makeInsertStatement(conn);
			int count = 0;
            while (true) {
				String line = reader.readLine();
				if (line == null)
					break;
				if (line.equals(prevLine))
				    continue;
				prevLine = line;
				// eclipseLaunchEvent date timestamp projectName event
				String tokens[] = line.split("\t");
				String timestampStr = tokens[1];
				String md5sum = tokens[2];
				String projectName = tokens[3];
				String event = tokens[4];

				getSubmitServerServletLog().debug(
						timestampStr + "\t" + md5sum + "\t" + projectName
								+ "\t" + event);

				EclipseLaunchEvent eclipseLaunchEvent = new EclipseLaunchEvent();
				eclipseLaunchEvent.setStudentRegistrationPK(registration
						.getStudentRegistrationPK());
				eclipseLaunchEvent.setProjectNumber(projectName);
				eclipseLaunchEvent.setProjectPK(project.getProjectPK());
				eclipseLaunchEvent.setEvent(event);
				long timestamp = Long.valueOf(timestampStr);
				eclipseLaunchEvent.setTimestamp(new Timestamp(timestamp));
				eclipseLaunchEvent.setMd5sum(md5sum);
				eclipseLaunchEvent.setSkew(skew);
				eclipseLaunchEvent.fillInInsertStatement(stmt);
				stmt.addBatch();
				count++;
            }
            if (count > 0) {
                stmt.executeBatch();
                StudentSubmitStatus status = StudentSubmitStatus.lookupByStudentRegistrationPKAndProjectPK(
                        registration.getStudentRegistrationPK(), project.getProjectPK(), conn);
                if (status != null) {
                    int totalEclipseLaunchEvents = EclipseLaunchEvent.countEclipseLaunchEventsByProjectPKAndStudentRegistration(

                    project, registration, conn);
                    status.setNumberRuns(totalEclipseLaunchEvents);
                    status.update(conn);
                }
            }

		} catch (InvalidRequiredParameterException e) {
		    ServletExceptionFilter.logErrorAndSendServerError(conn, request, null, "LogEclipseLaunchEvent missing required parameter", "", e);
		} catch (SQLException e) {
		    ServletExceptionFilter.logErrorAndSendServerError(conn, request, null, "LogEclipseLaunchEvent missing required parameter", "", e);
		} finally {
			releaseConnection(conn);
			if (reader != null)
				reader.close();
		}
	}

}

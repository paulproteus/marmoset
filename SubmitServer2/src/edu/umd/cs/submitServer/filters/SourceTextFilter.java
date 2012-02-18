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
 * Created on Apr 8, 2005
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.utilities.DisplayProperties;

/**
 * @author jspacco
 * 
 */
public class SourceTextFilter extends SubmitServerFilter {


	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		Submission submission = (Submission) request.getAttribute(SUBMISSION);
		List<Submission> submissionList = (List<Submission>) 
		        request.getAttribute(SUBMISSION_LIST);
        Project project = (Project) request.getAttribute(PROJECT);
        Course course = (Course) request.getAttribute(COURSE);
        
        if (submission == null && submissionList != null && !submissionList.isEmpty())
            for(Submission s : submissionList)
                if (s.isMostRecent()) {
                    submission = s;
                    break;
                }
            
        if (submission != null)
            request.setAttribute("sourceSubmission", submission);
        
		Connection conn = null;
		if (submission != null || course.getAllowsBaselineDownload())
		try {
			conn = getConnection();

			DisplayProperties fileProperties = new DisplayProperties();
			Map<String, List<String>> sourceContents = project.getBaselineText(conn, fileProperties);
            
			if (submission != null)
			    sourceContents = submission.getText(conn, fileProperties);
			   
			request.setAttribute("sourceContents", sourceContents);
			if (sourceContents != null) {
			  request.setAttribute("sourceFiles", sourceContents.keySet());
			  request.setAttribute("sourceProperties", fileProperties.getProperties());
			}
			
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
		chain.doFilter(request, response);
	}

}

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
 * Created on Jun 8, 2005
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.BuildServer;
import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.ServerError;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.utilities.SystemInfo;

public class AdminStatusFilter extends SubmitServerFilter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		Map<Project,Map<String,Integer>> buildStatusCount = new HashMap<Project,Map<String,Integer>>();
		Connection conn = null;
		try {
			conn = getConnection();

			Timestamp now = new Timestamp(System.currentTimeMillis());
			
		       
			Timestamp since = new Timestamp(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS));
            
	        List<ServerError> exceptions = ServerError.recentErrors(20,  ServerError.Kind.EXCEPTION, since, conn);
	        request.setAttribute("recentExceptions", exceptions);
	        List<ServerError> errors = ServerError.recentErrorsExcludingKind(20, ServerError.Kind.EXCEPTION, since, conn);
            request.setAttribute("recentErrors", errors);;
         
	            
			List<Course> courseList = Course.lookupAll(conn);
			request.setAttribute(COURSE_LIST, courseList);
			Map<Integer, Course> courseMap = new HashMap<Integer, Course>();
			for (Course c : courseList)
				courseMap.put(c.getCoursePK(), c);
			request.setAttribute(COURSE_MAP, courseMap);

            List<Project> projectList = Project.lookupAllUpcoming(now, conn);
            request.setAttribute(UPCOMING_PROJECTS, projectList);
			Set<Course> coursesThatNeedTesting = new HashSet<Course>();
			for(Project p : projectList) {
				buildStatusCount.put(p, p.getBuildStatusCount(conn));
				if (p.isTested())
					coursesThatNeedTesting.add(courseMap.get(p.getCoursePK()));
			}
			
			request.setAttribute(PROJECT_BUILD_STATUS_COUNT, buildStatusCount);
			
			Map<Submission, Integer> slowTestSubmissions = Submission.getSlowTestSubmissions(since, 20, conn);
			request.setAttribute("slowSubmissions", slowTestSubmissions.keySet());
			request.setAttribute("testDelay", slowTestSubmissions);
			Set<Course> coursesThatNeedBuildServers = new HashSet<Course>();
			
			Collection<BuildServer> buildServers = BuildServer.getAll(conn);
			examineCourses: for(Course course : coursesThatNeedTesting) {
				for(BuildServer b : buildServers) {
					if (b.canBuild(course))
						continue examineCourses;
				}
				coursesThatNeedBuildServers.add(course);
				
			}
			request.setAttribute("coursesThatNeedBuildServers", coursesThatNeedBuildServers);
			request.setAttribute("buildServers", buildServers);
			request.setAttribute("systemLoad", SystemInfo.getSystemLoad());
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
		chain.doFilter(request, response);
	}

}

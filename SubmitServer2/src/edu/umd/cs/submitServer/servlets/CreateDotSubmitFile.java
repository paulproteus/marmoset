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

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.WebConfigProperties;

public class CreateDotSubmitFile extends SubmitServerServlet {
	private static final WebConfigProperties webProperties = WebConfigProperties.get();

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Course course = (Course) request.getAttribute("course");
		Project project = (Project) request.getAttribute("project");
		response.setContentType("text/plain");
		response.setHeader("Content-Disposition", "inline; filename=\".submit\"");
        
		PrintWriter out = response.getWriter();
		printComment(out, "Submit server .submit file for");
        
		printComment(out, " Course " + course.getCourseName());   
		printComment(out, " Project " + project.getProjectNumber() + ": " + project.getTitle());   
		    
		out.println();
		printProperty(out, "courseName", course.getCourseName());
		 
		printProperty(out, "section", course.getSection());
		printProperty(out, "semester" ,course.getSemester());
        printProperty(out, "projectNumber",  project.getProjectNumber());
        
        printProperty(out, "courseKey" , course.getSubmitKey());
        
        String authentication = webProperties.getRequiredProperty(SubmitServerConstants.AUTHENTICATION_TYPE);
        printProperty(out, SubmitServerConstants.AUTHENTICATION_TYPE,  authentication );
        printProperty(out, "baseURL", request.getScheme() + "://"
                + request.getServerName() + ":" + request.getServerPort()
                + request.getContextPath());

		printProperty(out, "submitURL", request.getScheme() + "://"
				+ request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath() + ECLIPSE_SUBMIT_PATH);

		out.flush();
		out.close();
	}
	
	
	

}

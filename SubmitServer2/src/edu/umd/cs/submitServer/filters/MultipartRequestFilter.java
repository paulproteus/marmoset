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
 * Created on Jan 13, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.submitServer.MultipartRequest;
import edu.umd.cs.submitServer.WebConfigProperties;

/**
 * @author jspacco
 * 
 *         XXX This only handles form uploads with <b>ONE</b> File. If there are
 *         more we will ignore them.
 */
public class MultipartRequestFilter extends SubmitServerFilter {
	private static final WebConfigProperties webProperties = WebConfigProperties.get();

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		int maxSize = Integer.parseInt(webProperties.getRequiredProperty("fileupload.maxsize"));
		MultipartRequest multipartRequest = MultipartRequest.parseRequest(
				request, maxSize, getSubmitServerFilterLog(),
				strictParameterChecking(), servletContext);

		request.setAttribute(MULTIPART_REQUEST, multipartRequest);

		chain.doFilter(request, response);
	}
}

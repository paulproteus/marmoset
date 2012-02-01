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
 * Created on Jan 15, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.mail.MessagingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.utilities.JavaMail;
import edu.umd.cs.marmoset.utilities.XSSScrubber;
import edu.umd.cs.submitServer.RequestParser;
import edu.umd.cs.submitServer.WebConfigProperties;

/**
 * @author Nat Ayewah
 *
 */
@SuppressWarnings("serial")
public class RequestCourse extends SubmitServerServlet {
	private static final WebConfigProperties webProperties = WebConfigProperties.get();

    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	    StringBuffer buffer = new StringBuffer();

		buffer.append(createPair(request, "courseName"));
		buffer.append(createPair(request, "courseTitle"));
		buffer.append(createPair(request, "url"));
		buffer.append(createPair(request, "semester"));

		buffer.append("\n");

		buffer.append(createPair(request, "type1"));
		buffer.append(createPair(request, "firstname1"));
		buffer.append(createPair(request, "lastname1"));
		buffer.append(createPair(request, "loginName1"));
		buffer.append(createPair(request, "campusUID1"));
		buffer.append(createPair(request, "classAccount1"));
		buffer.append(createPair(request, "password1"));

		buffer.append("\n");

		buffer.append(createPair(request, "type2"));
		buffer.append(createPair(request, "firstname2"));
		buffer.append(createPair(request, "lastname2"));
		buffer.append(createPair(request, "loginName2"));
		buffer.append(createPair(request, "campusUID2"));
		buffer.append(createPair(request, "classAccount2"));
		buffer.append(createPair(request, "password2"));

		buffer.append("\n");

		buffer.append(createPair(request, "type3"));
		buffer.append(createPair(request, "firstname3"));
		buffer.append(createPair(request, "lastname3"));
		buffer.append(createPair(request, "loginName3"));
		buffer.append(createPair(request, "campusUID3"));
		buffer.append(createPair(request, "classAccount3"));
		buffer.append(createPair(request, "password3"));

		buffer.append("\n");

		buffer.append(createPair(request, "emailAddress"));
		buffer.append(createPair(request, "requireBuildServer"));
		buffer.append(createPair(request, "comments"));

		// We're about to write this string to a (potentially HTML-
		// enabled) email and an HTML response, so we have to scrub
		// out (escape) the HTML directives to about a cross-site
		// vulnerability.
		String result = XSSScrubber.scrubbedStr(buffer.toString(), false);
		try {
			// sendEmailToAdmin(result, null);
			sendEmailToAdmin(result, null,
					XSSScrubber.scrubbedStr(requestParam(request, "semester")));
		} catch (MessagingException e) {
			result = e.getMessage();
		}

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>Request a course</TITLE></HEAD>");
		out.println("  <BODY>");
		out.println("  <p>The following data was sent to the administrator</p>");
		out.println("  <PRE>");
		out.println(result);

		String link = "<a href=\"" + request.getContextPath()
				+ "/index.jsp\">Home</a>";
		out.println("</pre><p>" + link);
		out.println("</body>");
		out.println("</html>");
		out.flush();
		out.close();

	}

	/*
	 * Get a single parameter from request
	 *
	 * @param request the request sent by the client to the server
	 *
	 * @param parm the parameter to return
	 *
	 * @return parameter value
	 */
	private String requestParam(HttpServletRequest request, String parm) {
		RequestParser parser = new RequestParser(request,
				getSubmitServerServletLog(), strictParameterChecking());
		return parser.getParameter(parm);
	}

	private String createPair(HttpServletRequest request, String parm) {
		RequestParser parser = new RequestParser(request,
				getSubmitServerServletLog(), strictParameterChecking());
		return createPair(parm, parser.getParameter(parm));
	}

	private String createPair(String parm, String value) {
		return pad(parm) + ": " + value + "\n";
	}

	private String pad(String parm) {
		if (parm == null)
			return parm;
		int length = 25 - parm.length();
		for (int i = 0; i < length; i++)
			parm = " " + parm;
		return parm;
	}


	private void sendEmailToAdmin(String content, String requestEmail,
			String semester) throws MessagingException {
		ServletContext servletContext = getServletContext();
		String adminEmail = webProperties.getProperty(ADMIN_EMAIL);
		String host = webProperties.getProperty(SMTP_HOST);
		String fromEmail = webProperties.getProperty(EMAIL_RETURN_ADDRESS);
		
		if (fromEmail == null)
			fromEmail = adminEmail;

		String subject = "Request for New Course";

		if (adminEmail == null)
			throw new MessagingException(
					"A configuration error has been detected (1). "
							+ "Please contact your system administrator for assistance.");
		if (host == null)
			throw new MessagingException(
					"A configuration error has been detected (2). "
							+ "Please contact your system administrator for assistance.");
		
		if (requestEmail == null)
			requestEmail = fromEmail;
		if (semester != null)
			subject = semester + " - " + subject;
		JavaMail.sendMessage(adminEmail, requestEmail, host, subject, content);
	}

}

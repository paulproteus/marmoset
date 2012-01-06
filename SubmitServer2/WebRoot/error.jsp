<%--

 Marmoset: a student project snapshot, submission, testing and code review
 system developed by the Univ. of Maryland, College Park
 
 Developed as part of Jaime Spacco's Ph.D. thesis work, continuing effort led
 by William Pugh. See http://marmoset.cs.umd.edu/
 
 Copyright 2005 - 2011, Univ. of Maryland
 
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy of
 the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 License for the specific language governing permissions and limitations under
 the License.

--%>

<%@ page isErrorPage="true" language="java"%>
<%@ page import="edu.umd.cs.submitServer.UserSession"%>
<%@ page import="edu.umd.cs.submitServer.servlets.SubmitServerServlet"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.StringWriter"%>
<%@ page import="org.apache.log4j.Logger"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<%@ taglib prefix="log" uri="http://jakarta.apache.org/taglibs/log-1.0" %>


<!DOCTYPE HTML>
<html>
  <ss:head title="Error Page"/>
  <body>
  <ss:header/>
  <ss:loginBreadCrumb/>

<c:url var="bugImage" value="/images/Bug.gif"/>
<p><img src="${bugImage}">

<%
// TODO There must be an easier way to log this information!

// URL of request
String url = request.getRequestURI();
if (request.getQueryString() != null) 
	url += "?" + request.getQueryString();
	
// studentPK of the student who initiated the request
UserSession userSession = session == null ? null : (UserSession) session.getAttribute("userSession");
if (userSession != null)
	url += " for studentPK " + userSession.getStudentPK();

// Get the servletException logger
Logger logger = Logger.getLogger("edu.umd.cs.submitServer.logging.servletExceptionLog");
//System.out.println("error.jsp found logger named " +logger.getName());

// Build appropriate message
String msg = 
"URL: " +url +"\n"+
"Referer: " + request.getHeader("referer") +"\n"+
"Status code: " +pageContext.getErrorData().getStatusCode();

// Log the message to the logger I have under my control.
// So far it looks like setting 'swallowOutput="true"' in the
// <Context> part of server.xml keeps this info from being
// replicated into catalina.out.  I don't yet know how to get a handle
// on the Logger that used to be appending this information to
// catalina.out, and I don't much care right now because I'm able to
// log everything that I want.
logger.fatal(msg, pageContext.getErrorData().getThrowable());

%>

<h1>Oops!</h1>
<p><c:out value="${pageContext.errorData.throwable}"/> 


<c:if test="${! empty errorPK}">
<p>This error has been logged. Reference error number <c:out value="${errorPK}"/> if reporting this error.
</p></c:if>


<ss:footer/>
</html>

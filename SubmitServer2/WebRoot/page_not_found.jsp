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

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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

<h1>Error ${pageContext.errorData.statusCode}</h1>
<p>Sorry, we could not process your request.

<c:if test="${! empty pageContext.errorData.throwable.message}">
  <p>Message:  <c:out value="${pageContext.errorData.throwable.message}"/>
</c:if>
<c:if test="${! empty pageContext.errorData.requestURI}">
  <p>Request URI:  <c:out value="${pageContext.errorData.requestURI}"/>
</c:if>

     <ss:footer/>
</html>

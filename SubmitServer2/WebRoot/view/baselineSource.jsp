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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<!DOCTYPE HTML>
<html>
  <head>
   <ss:headContent title="Baseline source for ${course.courseName} project ${project.projectNumber}"/>
   ${ss:displayAllSourceFilesHead()}
  </head>

	<body>
	<ss:header />

	<c:choose>
	<c:when test="${instructorCapability == 'true'}">
		<ss:instructorBreadCrumb />
	</c:when>
	<c:otherwise>
		<ss:breadCrumb />
	</c:otherwise>
	</c:choose>

<ss:projectTitle />

	<h3>Pretty Print Options</h3>

	<c:set var="baseURL"  value="${pageContext.request.requestURL}?"/>
	<c:set var="qs"       value="${pageContext.request.queryString}"/>
 	<c:set var="baseURL3" value="${baseURL}tabWidth=3&${qs}"/>
 	<c:set var="baseURL4" value="${baseURL}tabWidth=4&${qs}"/>
 	<c:set var="baseURL8" value="${baseURL}tabWidth=8&${qs}"/>

	<p style="margin-left:1em">
		<b><abbr title="The number of spaces assigned to each tab">Tab Width</abbr>:</b>
		  <a href="${baseURL3}">3</a>
		| <a href="${baseURL4}">4</a>
		| <a href="${baseURL8}">8</a>
	</p>

   <c:choose>
   <c:when test="${project.archivePK == null  || project.archivePK  == 0}">
   <p>This project doesn't have any baseline/starting code.
   </c:when>
   <c:otherwise>
   ${ss:displayBaselineSource(connection, project, tabWidth)}
   </c:otherwise>
   </c:choose>







	</body>
</html>

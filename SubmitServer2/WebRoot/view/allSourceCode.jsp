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
   <ss:headContent title="${title}"/>
   <link href='http://fonts.googleapis.com/css?family=Ubuntu+Mono' rel='stylesheet' type='text/css'>
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


	<c:if test="${instructorCapability == 'true'}">
		<ss:studentPicture/>
	</c:if>

<c:if test="${reviewer != null}">
<c:url var="codeReviewLink" value="/view/codeReview/index.jsp">
<c:param name="submissionPK" value="${submission.submissionPK}"/>
<c:param name="reviewerPK" value="${reviewer.codeReviewerPK}"/>
</c:url>
<p><a href="${codeReviewLink}">Code Review</a>
</c:if>

	<h3>Pretty Print Options</h3>

	<c:set var="baseURL"  value="${pageContext.request.requestURL}?"/>
	<c:set var="qs"       value="${pageContext.request.queryString}"/>
 	<c:set var="baseURL3" value="${baseURL}tabWidth=3&${qs}"/>
 	<c:set var="baseURL4" value="${baseURL}tabWidth=4&${qs}"/>
 	<c:set var="baseURL8" value="${baseURL}tabWidth=8&${qs}"/>

	<p style="margin-left:1em">
		<b><acronym title="The number of spaces assigned to each tab">Tab Width</acronym>:</b>
		  <a href="${baseURL3}">3</a>
		| <a href="${baseURL4}">4</a>
		| <a href="${baseURL8}">8</a>
	</p>

   <c:url var="codeReviewLink" value="/view/codeReview/index.jsp">
	<c:param name="submissionPK" value="${submission.submissionPK}" />
   </c:url>

   <h3>Source Code</h3>
    <ss:selectCodeCoverage/>

	<c:choose>
     <c:when test="${codeReviewer}">
     <p><a href="${codeReviewLink}" target="codeReview">View code review</a>
      </p>
     </c:when>
     <c:when test="${instructorActionCapability || instructorCapability}">
     <p><a href="${codeReviewLink}"  target="codeReview">start code review</a>
       </p>
     </c:when>
     </c:choose>

	${ss:displayAllSourceFiles(connection, submission, project, tabWidth, codeCoverageResults)}


	</body>
</html>

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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>


<!DOCTYPE HTML>
<html>
<ss:head
	title="All test runs for submissionPK ${submission.submissionPK}" />
<body>

<ss:header />
<ss:instructorBreadCrumb />

<h1>Project <c:out value="${project.projectNumber}"/>:
<c:out value="${project.title}"/></h1>
<ss:studentPicture />
<h1><c:out value="${studentRegistration.fullname}"/></h1>
<h2>Submission # ${submission.submissionNumber}, <fmt:formatDate
	value="${submission.submissionTimestamp}" pattern="dd MMM, hh:mm a" /></h2>

<table>
	${ss:formattedColumnHeaders(7, canonicalTestOutcomeCollection)}
	<tr>
	    <th colspan="2">test setup</th>
	    <th colspan="2">submission tested</th>
		<th rowspan="2">successful<br>background<br>retests</th>
		<th rowspan="2"># inconsistent<br>background<br>retests</th>
		<th rowspan="2">view results</th>
		${ss:formattedTestHeaderTop(canonicalTestOutcomeCollection, true)}
	</tr>

	<tr>
	   <th>Version</th>
	   <th>Activated</th>
	   <th>machine</th><th>When</th>
		${ss:formattedTestHeader(canonicalTestOutcomeCollection, true)}
	</tr>

	<c:forEach var="testRun" items="${testRunList}" varStatus="counter">
	   <c:set var="testSetup" value="${testSetupMap[testRun.testSetupPK]}"/>
		<c:choose>
		<c:when test="${testRun.testRunPK == submission.currentTestRunPK}">
		<tr class="highlight">
		</c:when>
		<c:otherwise>
		<tr class="r${counter.index % 2}">
		</c:otherwise>
		</c:choose>
		  <td>
		  <c:url var="downloadTestSetupLink" value="/data/instructor/DownloadTestSetup">
                    <c:param name="testSetupPK" value="${testRun.testSetupPK}" />
                </c:url>
                <a href="${downloadTestSetupLink}">
                <c:out value="${testSetup.version}"/>
                <c:if test="${!empty testSetup.comment}">
                <c:out value="${testSetup.comment}"/>
                </c:if>
                </a>
            <td><fmt:formatDate
                value="${testSetupMap[testRun.testSetupPK].datePosted}"
                pattern="E',' dd MMM 'at' hh:mm a" /></td>
  			<td>${testRun.testMachine}</td>
            <td><fmt:formatDate value="${testRun.testTimestamp}"
				pattern="dd MMM KK:mm:ss a" /></td>

			<c:choose>
			<c:when test="${testRun.testRunPK == submission.currentTestRunPK}">

			<td>${submission.numSuccessfulBackgroundRetests}</td>

			<td>${submission.numFailedBackgroundRetests}</td>
			</c:when>
			<c:otherwise>
			<td colspan=2/>
			</c:otherwise>
			</c:choose>
		


			<td><c:url var="submissionLink"
				value="/view/instructor/submission.jsp">
				<c:param name="testRunPK" value="${testRun.testRunPK}" />
			</c:url> <a href="${submissionLink}"> view test results </a></td>

${ss:formattedTestResults(canonicalTestOutcomeCollection,testOutcomeCollectionMap[testRun.testRunPK])}



	</c:forEach>
	</table>
	<ss:footer />
</body>
</html>

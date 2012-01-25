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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>


<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>


<!DOCTYPE HTML>
<html>
<ss:head
	title="Test results with failed background retests for project ${project.projectNumber}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<ss:projectTitle/>

<c:choose>
	<c:when test="${!project.tested}">
		<h1>Upload only project</h1>
		<p>This project is set up to accept uploads only, and not perform any
		server based compilation and testing</p>
	</c:when>

	<c:otherwise>

	<ss:projectMenu />
	<p>
These submissions were re-tested against the current test setup (using the "background re-test"
mechanism) and returned different results.
		<table>

		<tr>
		<th>num</th>
		<th>class account</th>
		<th>submissionPK</th>
		<th>num successful<br>background retests</th>
		<th># inconsistent<br>background retests</th>
		</tr>

		<c:forEach var="submission" items="${failedBackgroundRetestSubmissionList}" varStatus="counter">
			<tr class="r${counter.index % 2}">
			<td class="number">${1+counter.index}</td>

			<c:url var="studentLink" value="/view/instructor/studentProject.jsp">
				<c:param name="studentPK" value="${studentRegistrationMap[submission.studentRegistrationPK].studentPK}"/>
				<c:param name="projectPK" value="${project.projectPK}"/>
			</c:url>
			<td><a href="${studentLink}">
            <c:out value="${studentRegistrationMap[submission.studentRegistrationPK].classAccount}"/></a></td>

			<c:url var="submissionAllTestsLink" value="/view/instructor/submissionAllTests.jsp">
				<c:param name="submissionPK" value="${submission.submissionPK}"/>
			</c:url>
			<td><a href="${submissionAllTestsLink}">${submission.submissionPK}</a></td>
			<td>${submission.numSuccessfulBackgroundRetests}</td>
			<td>${submission.numFailedBackgroundRetests}</td>
			</tr>
		</c:forEach>

		</table>
	</c:otherwise>

</c:choose>
<ss:footer />
</body>
</html>

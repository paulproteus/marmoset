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
<ss:head
	title="All test runs for submissionPK ${submission.submissionPK}" />
<body>
<ss:header />
<ss:breadCrumb />

<ss:projectTitle />

<p>Submission #${submission.submissionNumber}, submitted at <fmt:formatDate
	value="${submission.submissionTimestamp}"
	pattern="E',' dd MMM 'at' hh:mm a" />
<h1>All testing results<br></h1>

<p>
<table>
	<tr>
		<th colspan=2>Test Setup
		<th rowspan="2">Date Ran</th>
		<th rowspan="2">View Results<br><abbr title="Public Tests">P</abbr> 
									| <abbr title="Release Tests">R</abbr></th>
	<tr>
		<th>Version</th>
		<th>Test Setup tested</th>
        <!-- Ben L: did away with comment field since instructors were
             using it for comments that they didn't intend the students
             to see
		<th class="description">Comment</th>
		-->
	</tr>

	<c:forEach var="testRun" items="${testRunList}" varStatus="counter">
		<tr class="r${counter.index % 2}">


			<td>${testSetupMap[testRun.testSetupPK].version}</td>
			<td><fmt:formatDate
				value="${testSetupMap[testRun.testSetupPK].datePosted}"
				pattern="E',' dd MMM 'at' hh:mm a" /></td>

            <!--  
			<td class="description">
			${testSetupMap[testRun.testSetupPK].comment}</td>
			-->
			<td><fmt:formatDate value="${testRun.testTimestamp}"
				pattern="dd MMM KK:mm:ss a" /></td>
			<td><c:url var="submissionLink" value="/view/submission.jsp">
				<c:param name="testRunPK" value="${testRun.testRunPK}" />
			</c:url> <a href="${submissionLink}"> <c:choose>
				<c:when test="${testRun.compileSuccessful}">
            ${testRun.valuePublicTestsPassed} 
					<c:choose>
					<c:when test="${submission.releaseTestingRequested && testOutcomeCollection.passedAllPublicTests}">
						| ${submission.valueReleaseTestsPassed}
					</c:when>
					<c:otherwise>
					| ?
					</c:otherwise>
					</c:choose>

				</c:when>
				<c:otherwise>
			did not compile
			</c:otherwise>
			</c:choose> </a></td>

		</tr>
	</c:forEach>
</table>

<ss:inconsistentBackgroundRetestDescription/>

<ss:footer />
</body>
</html>

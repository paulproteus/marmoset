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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML>
<html>
	<ss:head title="Findbugs warning with coverage for ${submission.submissionPK} ${testOutcome.testType} ${testOutcome.testNumber}"/>
  <body>
  	<ss:header/>
  		<ss:instructorBreadCrumb/>

<c:url var="viewSourceLink" value="/view/sourceCode.jsp"/>

	<h2>FindBugs warning</h2>
	<p>
	<table>
		<tr>
			<th>Location</th>
			<th>Warning</th>
			<th>Rank</th>
			<th>Link to longer description</th>

			<tr class="r${numDisplayed % 2}">
				<c:set var="numDisplayed" value="${numDisplayed + 1}" />
				<td class="description">
					<c:out value="${ss:hotlink(warning, viewSourceLink)}" escapeXml="false"/>
				</td>

				<td class="description">
				<%--<pre>--%>
					<c:out
					value="${warning.longTestResult}" />
				<%--</pre>--%>
					</td>
				<td class="description">${warning.exceptionClassName}</td>
				<td><a href="${ss:webProperty('findbugsDescriptionURL')}#${warning.testName}">${warning.testName}</a></td>
			</tr>
	</table>

	<h2>Test results that cover this warning</h2>
<p>
<table class="testResults">
	<c:set var="numDisplayed" value="${numDisplayed + 1}" />

	<tr>
		<th>type</th>
		<th>test #</th>
		<th>outcome</th>
		<th>all source</th>
		<th>covered file</th>
		<th>points</th>
		<th>name</th>
		<th>short result</th>
		<th>long result</th>
	</tr>
	<c:forEach var="test" items="${testOutcomeCollection.publicOutcomes}">

	<tr class="r${numDisplayed % 2}">
		<c:set var="numDisplayed" value="${numDisplayed + 1}" />
		<td>${test.testType}</td>
		<td>${test.testNumber}</td>
		<td>${test.outcome}</td>

		<c:url var="coverageLink" value="/view/sourceFiles.jsp">
			<c:if test="${test.testNumber != null and test.testType != null}">
				<c:param name="testType" value="${test.testType}"/>
				<c:param name="testNumber" value="${test.testNumber}"/>
			</c:if>
			<c:param name="submissionPK" value="${submission.submissionPK}"/>
		</c:url>
		<td> <a href="${coverageLink}"> all source </a></td>

		<c:url var="coverageOnlyLink" value="/view/sourceCode.jsp">
			<c:param name="testType" value="${test.testType}" />
			<c:param name="testNumber" value="${test.testNumber}" />
			<c:param name="sourceFileName" value="${warning.fileName}" />
			<c:param name="testRunPK" value="${ss:scrub(param.testRunPK)}"/>
		</c:url>
		<td> <a href="${coverageOnlyLink}"> covered file</a></td>

		<td>${test.pointValue}</td>
		<td>${test.shortTestName}</td>
		<td class="description">

		<c:out value="${test.shortTestResult}" /></td>
		<td class="description">
		<%--<pre>--%>
			<c:out value="${ss:hotlink(test, viewSourceLink)}" escapeXml="false"/>
		<%--</pre>--%>
		</td>
	</tr>
	</c:forEach>


	<c:forEach var="test" items="${testOutcomeCollection.releaseOutcomes}">

		<tr class="r${numDisplayed % 2}">
			<c:set var="numDisplayed" value="${numDisplayed + 1}" />
			<td>${test.testType}</td>
			<td>${test.testNumber}</td>
			<td>${test.outcome}</td>

			<c:url var="coverageLink" value="/view/sourceFiles.jsp">
				<c:if test="${test.testNumber != null and test.testType != null}">
					<c:param name="testType" value="${test.testType}"/>
					<c:param name="testNumber" value="${test.testNumber}"/>
				</c:if>
				<c:param name="submissionPK" value="${submission.submissionPK}"/>
			</c:url>
			<td> <a href="${coverageLink}"> source </a></td>

			<c:url var="coverageOnlyLink" value="/view/sourceCode.jsp">
				<c:param name="testType" value="${test.testType}" />
				<c:param name="testNumber" value="${test.testNumber}" />
				<c:param name="sourceFileName" value="${warning.fileName}" />
				<c:param name="testRunPK" value="${ss:scrub(param.testRunPK)}"/>
			</c:url>
			<td> <a href="${coverageOnlyLink}"> covered file</a></td>

			<td>${test.pointValue}</td>
			<td>${test.shortTestName}</td>
			<td class="description"><c:out value="${test.shortTestResult}" /></td>
			<td class="description">
			<%--<pre>--%>
				<c:out value="${ss:hotlink(test, viewSourceLink)}" escapeXml="false"/>
			<%--</pre>--%>
			</td>
		</tr>
	</c:forEach>

	<c:forEach var="test" items="${testOutcomeCollection.secretOutcomes}">

		<tr class="r${numDisplayed % 2}">
			<c:set var="numDisplayed" value="${numDisplayed + 1}" />
			<td>${test.testType}</td>
			<td>${test.testNumber}</td>
			<td>${test.outcome}</td>
					<td>${test.pointValue}</td>
			<td>${test.shortTestName}</td>
			<td class="description"><c:out value="${test.shortTestResult}" /></td>
			<td class="description">
			<pre><c:out value="${ss:hotlink(test, viewSourceLink)}" escapeXml="false"/>
			</pre>
			</td>
		</tr>
	</c:forEach>
	</table>
	</body>
</html>

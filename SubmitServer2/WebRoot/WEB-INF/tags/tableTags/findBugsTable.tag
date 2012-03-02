<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>


<c:url var="viewLink" value="/view"/>

<c:set var="instructor" value="${fn:contains(pageContext.request.servletPath,'instructor')}"/>
<c:if test="${not empty testOutcomeCollection.findBugsOutcomes}">
	<h2>FindBugs warnings</h2>
	<table>
			<tr>
				<th>Location</th>
				<c:if test="${instructor}">
				<th>Unit tests covered</th>
				</c:if>
				<th>Warning</th>
				<th>Rank</th>
				<th>Link to longer description</th>
			</tr>
			<c:forEach var="test" items="${testOutcomeCollection.findBugsOutcomes}">
			<tr class="r${numDisplayed % 2}">
				<c:set var="numDisplayed" value="${numDisplayed + 1}" />

				<td class="description">
					<c:out value="${ss:hotlink(test, viewLink)}" escapeXml="false"/>
				</td>

				<c:if test="${instructor}">
				<c:url var="warningWithCoverageLink" value="/view/instructor/warningWithCoverage.jsp">
					<c:param name="longTestResult" value="${test.longTestResult}"/>
					<c:param name="warningName" value="${test.testName}"/>
					<c:param name="priority" value="${test.exceptionClassName}"/>
					<c:param name="shortTestResult" value="${test.shortTestResult}"/>
					<c:param name="testRunPK" value="${submission.currentTestRunPK}"/>
				</c:url>
				<td>
					<a href="${warningWithCoverageLink}">
					unit tests<br>covered
					</a>
				</td>
				</c:if>

				<td class="description">
					${test.longTestResultAsHtml}
				</td>

				<td class="description">${test.findBugsRankDescription}</td>

				<td class="description"><a href="${ss:webProperty('findbugsDescriptionsURL')}#${test.testName}">${test.testName}</a></td>
			</tr>
			</c:forEach>
	</table>
</c:if>

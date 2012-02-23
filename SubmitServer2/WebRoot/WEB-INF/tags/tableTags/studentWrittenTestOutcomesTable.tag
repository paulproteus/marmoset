<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>


<c:url var="viewLink" value="/view"/>

<c:if test="${not empty testOutcomeCollection.studentOutcomes}">
<h2>Student-written test results</h2>

	<table class="testResults">
	<c:set var="numDisplayed" value="0" />

	<tr>
		<th>type</th>
		<th>test #</th>
		<th>outcome</th>
		<th>source<br>coverage</th>
		<th>name</th>
		<th>short result</th>
		<th>long result</th>

		<c:if test="${userSession.superUser}">
			<th>raw coverage xml</th>
		</c:if>
	</tr>
	<c:forEach var="test" items="${testOutcomeCollection.studentOutcomes}">

		<tr class="r${numDisplayed % 2}">
			<c:set var="numDisplayed" value="${numDisplayed + 1}" />
			<td>${test.testType}</td>
			<td>${test.testNumber}</td>
			<td>${test.outcome}</td>

			<c:url var="studentCoverageLink" value="/view/sourceFiles.jsp">
				<%-- TODO WTF Does a null testNumber of testType mean? --%>
				<c:if test="${test.testNumber != null and test.testType != null}">
					<c:param name="testType" value="${test.testType}"/>
					<c:param name="testNumber" value="${test.testNumber}"/>
				</c:if>
				<c:param name="testRunPK" value="${testRun.testRunPK}"/>
			</c:url>
			<td> <a href="${studentCoverageLink}"> source </a></td>

			<td>${test.shortTestName}</td>
			<td class="description">

			<c:out value="${test.shortTestResult}" /></td>
			<td class="description">
				<c:out value="${ss:hotlink(test, viewLink)}" escapeXml="false"/>
			</td>
			<c:if test="${userSession.superUser}">
			<td>
				<c:url var="rawCoverageLink" value="/research/PrintRawCoverageXmlResults">
					<c:param name="testRunPK" value="${test.testRunPK}"/>
					<c:param name="testName" value="${test.testName}"/>
					<c:param name="testNumber" value="${test.testNumber}"/>
				</c:url>
				<a href="${rawCoverageLink}"> raw coverage </a>
			</td>
			</c:if>
		</tr>
	</c:forEach>
	</table>
</c:if>
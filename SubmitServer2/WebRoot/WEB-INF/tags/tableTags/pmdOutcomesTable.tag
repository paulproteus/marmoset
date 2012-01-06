<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>


<c:url var="viewSourceLink" value="/view/sourceCode.jsp"/>

<h2>PMD Warnings</h2>
	<p>
	<table>
		<tr>
			<th> Name</th>
			<th> Description</th>
			<th> Location</th>
			<th> Priority</th>
		</tr>
		<c:forEach var="pmd" items="${testOutcomeCollection.pmdOutcomes}">
			<tr class="r${numDisplayed % 2}">
			<c:set var="numDisplayed" value="${numDisplayed + 1}" />
			<td> ${pmd.testName}</td>
			<td> ${pmd.longTestResult}</td>
			<td> <c:out value="${ss:hotlink(pmd, viewSourceLink)}" escapeXml="false"/></td>
			<td> ${pmd.exceptionClassName}</td>
			</tr>
		</c:forEach>
	</table>

	<%-- Unsure this data will even be accurate
	<h2>Code Features</h2>
	<p>
	<table>
		<tr>
			<th> feature </th>
			<th> count </th>
		</tr>
		<tr>
			<td>methods</td>
			<td>${testOutcomeCollection.numMethods}</td>
		</tr>
		<tr>
			<td>classes</td>
			<td>${testOutcomeCollection.numClasses}</td>
		</tr>
		<tr>
			<td>opcodes</td>
			<td>${testOutcomeCollection.numOpcodes}</td>
		</tr>

	</table>
	--%>
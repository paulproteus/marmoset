<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>


<c:url var="viewSourceLink" value="/view/sourceCode.jsp"/>

<c:if test="${not empty testOutcomeCollection.uncoveredMethods}">
	<h3> These methods are not covered by a Public or student-written test </h3>
	<table class="testResults">
		<tr>
			<th>#</th>
			<th>class</th>
			<th>method</th>
			<th>line number</th>
			<th>link</th>
		</tr>
		<c:forEach var="outcome" items="${testOutcomeCollection.uncoveredMethods}">
		<tr>
			<td>${outcome.testNumber}</td>
			<td class="left">${outcome.exceptionClassName}</td>
			<td class="left">${outcome.htmlTestName}</td>
			<td class="right">${outcome.pointValue}</td>
			<td class="left">${ss:hotlink(outcome, viewSourceLink)}</td>
		</tr>
		</c:forEach>
	</table>
</c:if>
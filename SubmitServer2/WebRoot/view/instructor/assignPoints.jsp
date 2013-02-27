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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML>
<html>
	<ss:head title="Assign points for project ${project.title} in ${course.courseName}"/>

<SCRIPT type="text/javascript" >
function getValue(num)
{
	num = parseInt(num);
    if(isNaN(num))
		num = 0;
	return num;
}

function change()
{
	num = 0;
	for (var ii = 0; ii <  document.inputForm.elements.length; ii++) {
		if (document.inputForm.elements[ii].name != "testRunPK" &&
			document.inputForm.elements[ii].name != "comment" &&
			document.inputForm.elements[ii].name != "total")
		{
			num = num + getValue(document.inputForm.elements[ii].value);
		}
	}
	document.inputForm.total.value = num;
}
$(document).ready(change);
</SCRIPT>

  <body>
  	<ss:header/>
	<ss:instructorBreadCrumb/>

	<div class="sectionTitle">
		<h1><c:out value="${project.fullTitle}"/> </h1>

		<p class="sectionDescription"><c:out value="${project.description}"/></p>
	</div>

    <form class="form" name="inputForm" action="<c:url value="/action/instructor/AssignPoints"/>" method="POST">
    <h2>Testing setup</h2>
    <ul>
    <li> Testing setup
     tested  <fmt:formatDate value="${testSetup.datePosted}" pattern="E',' dd MMM 'at' hh:mm a"/>
     <c:set var="comment">
     <c:out value="${testSetup.comment}" />
     </c:set>
    <li> Test-setup comment: <input type="text" name="comment" value="${comment}" size="30"/>
    <li> Solution submitted by 
    <c:out value="${studentRegistration.classAccount}"/>
    at <fmt:formatDate value="${submission.submissionTimestamp}" pattern="E',' dd MMM 'at' hh:mm a"/>
    <c:if test="${testSetup.status == 'FAILED'}">
					<c:url var="canonicalRunLink"
						value="/view/instructor/submission.jsp">
						<c:param name="testRunPK" value="${testSetup.testRunPK}" />
					</c:url>
					<li><b>Warning:</b> The <a href="${canonicalRunLink}">canonical
							execution</a> of this test setup failed one or more tests.
				</c:if></ul>

<p>
<c:choose>
<c:when test="${not empty canonicalTestRun}">
<c:url var="testRunLink" value="/view/instructor/submission.jsp">
<c:param name="testRunPK" value="${canonicalTestRun.testRunPK}"/>
</c:url>

Starting with point totals <a href="$testRunLink}"> from 
test setup  <c:out value="${canonicalTestSetup.description}"/>.
posted
<fmt:formatDate value="${canonicalTestSetup.datePosted}" pattern="E',' dd MMM 'at' hh:mm a"/>
</a>.

</c:when>
<c:otherwise>
No previous activated test setup
</c:otherwise>
</c:choose>
    <p>
	<input type="hidden" name="testRunPK" value="${testRun.testRunPK}">
  	<table>
  		<tr>
  			<th> test # </th>
  			<th> type </th>
  			<th> name </th>
  			<th> point value </th>
  		</tr>
		<c:forEach var="outcome" items="${testOutcomeCollection.allTestOutcomes}" varStatus="counter">
			<c:if test="${outcome.testType != 'BUILD'}">
				<tr class="$r{counter.index % 2 == 1 ? 'odd' : 'even'}">
				<td> ${outcome.testNumber} </td>
				<td> ${outcome.testType} </td>
				<td class="label"> ${outcome.testName} </td>
				<td> <input type="text" name="${outcome.testName}" class="pointValue" size="3" value="${canonicalTestOutcomeMap[outcome.testName].pointValue}" onchange="change()"/></td>
				</tr>
			</c:if>
		</c:forEach>
		<tr>
			<td colspan=3>total</td>
			<td><input type="text" name="total" size="3" readonly="readonly"></td>
		</tr>
		<tr class="submit">
		<td class="label" colspan=4>
		<input type=submit value="Assign points, edit test-setup comment and activate test setup"></td>
		</tr>

  	</table>
  	</form>

<ss:footer/>
  </body>
</html>

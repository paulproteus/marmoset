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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>


<!DOCTYPE HTML>
<html>
<head>
<ss:head title="Code review for Project ${project.projectNumber} : ${project.title}" />
</head>

<body>
<ss:header />
<ss:instructorBreadCrumb />

<div class="sectionTitle">
	<h2> Code Review for project  ${project.projectNumber} : ${project.title}</h2>
	<p> <c:out value="${codeReviewAssignment.description}" />
	<p> Due <fmt:formatDate value="${codeReviewAssignment.deadline}" pattern="dd MMM, hh:mm a" />

</div>

<c:choose>
<c:when test="${! empty studentCodeReviewersForAssignment }">
<c:choose>
<c:when test="${fn:length(submissionsUnderReview) == 1}">
<p>Student review of single submission
</p>
</c:when>
<c:otherwise>
<p>Student peer review 
</p></c:otherwise>
</c:choose>


<p>Student identities are anonymous: ${codeReviewAssignment.anonymous} 
<p>Reviewers can see comments from other reviewers: ${codeReviewAssignment.otherReviewsVisible} 

</p>
</c:when>
<c:otherwise>
<p>Review of ${fn:length(submissionsUnderReview)} submission by  ${fn:length(codeReviewersForAssignment)} reviewers
</c:otherwise>
</c:choose>


<c:url var="PrintRubricEvaluationsForDatabase" value="/data/instructor/PrintRubricEvaluationsForDatabase">
        <c:param name="codeReviewAssignmentPK" value="${codeReviewAssignment.codeReviewAssignmentPK}" />
        </c:url>
    <c:url var="PrintRubricsForDatabase" value="/data/instructor/PrintRubricsForDatabase">
        <c:param name="codeReviewAssignmentPK" value="${codeReviewAssignment.codeReviewAssignmentPK}" />
        </c:url>    

<c:url var="updateAssignment" value="/view/instructor/updateCodeReviewAssignment.jsp">
        <c:param name="codeReviewAssignmentPK" value="${codeReviewAssignment.codeReviewAssignmentPK}" />
        </c:url>
<p><a href="${PrintRubricsForDatabase}">List rubrics in CSV format for upload to grades server</a>
<p><a href="${PrintRubricEvaluationsForDatabase}">List rubric evaluations in CSV format for upload to grades server</a>
<p><a href="${updateAssignment}">Update code review assignment</a>

<c:if test="${! empty rubrics }">
<h2>Rubrics</h2>
<ul>
 <c:forEach var="rubric" items="${rubrics}" >
 <li>
 <c:choose>
 <c:when test="${rubric.presentation == 'NUMERIC' }">
 <input type="number" size=4  readonly="readonly">
 </c:when>
 <c:when test="${rubric.presentation == 'CHECKBOX' }">
<input type="checkbox" checked="checked"  readonly="readonly">
</c:when>
 <c:when test="${rubric.presentation == 'DROPDOWN' }">
 <select name="r" disabled="disabled">
  <c:forEach var="e" items="${rubric.dataAsMap}" >
  <option> <c:out value="${e}"/></option>
  </c:forEach>
 </select>
 </c:when>
 </c:choose>
 <c:out value="${rubric.name}"/> :
  <c:out value="${rubric.description}"/> 
</c:forEach>
</ul>
</c:if>


<h2>Submissions being reviewed</h2>


<c:set var="evaluations" value=""/>
<table>
<tr>
<th colspan="4">reviews</th>
</tr>
<c:forEach var="submission" items="${submissionsUnderReview}" varStatus="counter">

<c:url var="viewCodeReview" value="/view/codeReview/index.jsp">
		<c:param name="submissionPK" value="${submission.submissionPK}" />
</c:url>

	<c:choose>
		<c:when test="${project.tested}">
			<c:url var="submissionLink" value="/view/instructor/submission.jsp">
				<c:param name="submissionPK" value="${submission.submissionPK}" />
			</c:url>
		</c:when>
		<c:otherwise>
			<c:url var="submissionLink" value="/view/allSourceCode.jsp">
				<c:param name="submissionPK" value="${submission.submissionPK}" />
			</c:url>
		</c:otherwise>
	</c:choose>

	<c:set var="studentRegistration"  value="${studentRegistrationMap[submission.studentRegistrationPK]}"/>
	
<tr class="r${counter.index % 2}">
<td colspan="3">
<a href="${viewCodeReview}" target="codeReview" title="code review">
<c:out value="${studentRegistration.fullname}"/>
</a>
</td>



<td class="description">
<a href="${submissionLink}" title="test results"><c:out value="${submission.testSummary}"/></a>
</td>

</tr>
 <c:forEach var="codeReviewer" items="${codeReviewersForAssignment}" >
 <c:if test="${codeReviewer.submissionPK == submission.submissionPK 
    &&  ( codeReviewer.numComments > 0 || !codeReviewer.author) }">
  <tr class="r${counter.index % 2}">
  <td>
    <c:out value="${codeReviewer.name}" />
  </td>
 <c:if test="${! empty rubrics}">
 <c:set var="evaluations" value="${ss:evaluationsForReviewer(codeReviewer, connection)}"/>
 </c:if>
 
  <c:choose>
  <c:when test="${codeReviewer.numComments > 0 || ! empty evaluations}">
  <td><c:out value="${codeReviewer.numComments}" /></td>
  <td><fmt:formatDate value="${codeReviewer.lastUpdate}" pattern="dd MMM, hh:mm a" /></td>
  <td class="description">
  
  <c:forEach var="e" items="${evaluations}">
  <c:if test="${e.status == 'LIVE' }">
  <c:set var="r" value="${rubricMap[e.rubricPK]}"/>
<c:out value="${e.value}"/> <c:out value="${r.name}"/>.
   <c:out value="${e.explanation}"/><br>
   </c:if>
  </c:forEach>
  
  </c:when>
  <c:otherwise>
  <td colspan="3"/>
  </c:otherwise>
  </c:choose>
  </tr>
  </c:if>
  </c:forEach>

</c:forEach>
</table>


<ss:footer />
</body>
</html>

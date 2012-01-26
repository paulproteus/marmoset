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
	title="Starter/baseline files for project ${project.projectNumber} for ${course.courseName} in ${course.semester}" />

<body>
<ss:header />
<ss:instructorBreadCrumb />

<ss:projectTitle />
<ss:projectMenu />

<h2>Starter/baseline files for Project <c:out value="${project.projectNumber}"/></h2>

    <p>The baseline is used for two purposes:
        <ul>
        <li>Students can download it from the web server as a starting point for their projects.
        <li>It is used to compute a source diff for projects. Code that is identical to code in the baseline submission is dimmed in source views
        and code reviews. Also, in summary listing of projects we list the number of lines of changed code in each submission.
        </ul>


<c:choose>
	<c:when test="${project.archivePK == null  || project.archivePK == 0}">
		<p>This project doesn't currently have any baseline/starting code.

		<p>A "starter file archive" is a collection of resources students
		will use to start their project. For makefile-based projects (in C,
		Ruby, OCaml, etc), starter files usually include a Makefile, public
		test cases and possibly some partially-written source files that need
		to be completed.
		<p>You can select a submission from the canonical account, or
		directly upload a zip/jar archive of the starter files for the
		project. 
	</c:when>
	<c:otherwise>
		<p>You can update/replace the starter file by choosing a a
		submission from the canonical account, or directly upload a zip/jar
		archive of the starter files for the project.
	</c:otherwise>
</c:choose>



<table class="form">
<c:if test="${not empty candidateBaselines }">
<tr><th colspan="6"> candidate baselines </th>
<tr>
<th>by<th>#<th>when<th>test results<th>source<th>Use
</th></tr>
<c:forEach var="submissionPair" items="${candidateBaselines}"
	varStatus="counter">
	<c:set var="submission" value="${submissionPair.key}"/>
	<c:set var="sr" value="${submissionPair.value}"/>
    
	<c:url var="submissionLink" value="/view/instructor/submission.jsp">
		<c:param name="submissionPK" value="${submission.submissionPK}" />
	</c:url>
	   <c:url var="selectBaselineLink" value="/action/instructor/SelectProjectStarterFiles"/>
		<tr class="r${counter.index % 2}">
		    <td><c:out value="${sr.fullname}"/>

			<td>${submission.submissionNumber}</td>

			<td><a href="${submissionLink}"><fmt:formatDate
				value="${submission.submissionTimestamp}" pattern="dd MMM hh:mm a" /></a></td>

			<td><a href="${submissionLink}">${submission.testSummary}</a></td>

			<td>
            <c:choose>
			<c:when test="${submission.archivePK != project.archivePK}">

			<c:url var="viewSourceLink" value="/view/allSourceCode.jsp">
				<c:param name="submissionPK" value="${submission.submissionPK}" />
				<c:param name="testType" value="cardinal" />
				<c:param name="testNumber" value="all" />
			</c:url> <a href="${viewSourceLink}"> view</a>
			</c:when>
            <c:otherwise><a href="#current">below</a></c:otherwise>
            </c:choose>
			</td>
			<td>
			<c:choose>
			<c:when test="${submission.archivePK == project.archivePK}">
		    <form METHOD="POST" action="${selectBaselineLink}">
            <input type="hidden" name="projectPK"  value="${project.projectPK}" /> 
            <input type="submit" value="Remove"/>
            </form>
			</c:when>
			<c:when test="${testSetup != null 
			&& submission.buildStatus=='COMPLETE'
			&& testSetup.valueTotalTests > 0
			 && submission.valuePassedOverall > testSetup.valueTotalTests/2 }">
			<a title="This submission passed more than half the test cases; you don't want to use it as a baseline to distribute to students">
			full implementation</a>
			</c:when>
			<c:otherwise>
			<form METHOD="POST" action="${selectBaselineLink}">
			<input type="hidden" name="submissionPK" value="${submission.submissionPK}"/>
			<input type="submit" value="Use"/>
			</form>
			</c:otherwise>
			</c:choose>
			</td>


		</tr>

</c:forEach>
</c:if>

		<tr class="submit">
	<form name="submitform" enctype="multipart/form-data"
    action="<c:url value="/action/instructor/UploadProjectStarterFiles"/>"
    method="POST">
    <input type="hidden" name="projectPK"  value="${project.projectPK}" /> 
    <input type="hidden"  name="submitClientTool" value="web" />
        <td colspan="2">Web-base upload:
        <td class="input" colspan="2"><input type="file" name="file" size=40 />
    

		<td colspan=2"><input type="submit"
			value="Upload">
			</td>
			</form>
			</tr>
</table>


		<c:if test="${project.archivePK != null && project.archivePK  > 0}">
			<h2><a id="current">Current baseline source</a></h2>
   ${ss:displayBaselineSource(connection, project, tabWidth)}
   </c:if>


		<ss:footer />
</body>
</html>

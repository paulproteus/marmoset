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
<ss:head title="${course.courseName} Submit Server" />

<body>
<ss:header />
<ss:breadCrumb />

<div class="sectionTitle">
	<h1><a href="${course.url}"><c:out value="${course.fullDescription}"/></a></h1>

	<ss:hello/>
</div>

<ss:codeReviews title="Pending Code reviews"/>

<h2>Projects</h2>
<p>
<c:set  var="anyDownload" value="0"/>
<c:if test="${course.allowsBaselineDownload}">
<c:forEach var="project" items="${projectList}">
<c:if test="${project.archivePK != null && project.archivePK > 0}">
<c:set  var="anyDownload" value="1"/>
</c:if>
</c:forEach>
</c:if>


<table>
	<tr>
		<th>project</th>
		<th>submissions</th>
		<th>web<br>
			submission</th>
			<c:if test="${anyDownload > 0}">
		<th>download<br>
			starter<br>
			files
		</th>
		</c:if>
		<th>Due</th>
		<th class="description">Title</th>
	</tr>

	<c:set var="numDisplayed" value="0" />
	<c:forEach var="project" items="${projectList}" varStatus="counter">

		<c:if test="${project.visibleToStudents || instructorActionCapability || instructorCapability}">
		<c:url var="projectLink" value="/view/project.jsp">
					<c:param name="projectPK" value="${project.projectPK}" />
				</c:url>
			<tr class="r${numDisplayed % 2}">

				<td><c:choose>

					<c:when test="${project.url != null}">
						<a href="<c:url value="${project.url}"/>">
						${project.projectNumber} </a>
					</c:when>

					<c:otherwise>
						<a href="${projectLink}"><c:out value="${project.projectNumber}"/></a>
					</c:otherwise>

				</c:choose>
				<c:if test="${!project.visibleToStudents}">
				(invisible to students)</c:if>
				</td>

				<td><a href="${projectLink}"> view </a></td>

				<td><c:url var="submitProjectLink"
					value="/view/submitProject.jsp">
					<c:param name="projectPK" value="${project.projectPK}" />
					<c:param name="testSetupPK" value="${project.testSetupPK}" />
				</c:url> <a href="${submitProjectLink}"> submit </a></td>

				<c:if test="${anyDownload > 0}">

					<td><c:if
						test="${project.archivePK != null && project.archivePK > 0}">
						<c:url var="downloadStarterFilesLink"
							value="/data/DownloadProjectStarterFiles">
							<c:param name="projectPK" value="${project.projectPK}" />
						</c:url>
						<a href="${downloadStarterFilesLink}"> download </a>
					</c:if></td>
				</c:if>

				<td><fmt:formatDate value="${project.ontime}"
					pattern="dd MMM, hh:mm a" /></td>
				<td class="description"><c:out value="${project.title}"/></td>

			</tr>
			<c:set var="numDisplayed" value="${numDisplayed + 1}" />
		</c:if>
	</c:forEach>
</table>

<ss:footer/>
</body>
</html>

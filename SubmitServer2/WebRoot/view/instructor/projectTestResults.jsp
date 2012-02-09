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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>


<!DOCTYPE HTML>
<html>
<ss:head
	title="Test results from all students for project ${project.projectNumber}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<ss:projectTitle />


<c:choose>
	<c:when test="${!project.tested}">
		<h1>Upload only project</h1>
		<p>This project is set up to accept uploads only, and not perform any
		server based compilation and testing</p>
	</c:when>

	<c:otherwise>
		<ss:projectMenu />


<c:choose>
<c:when test="${not empty section}">
<c:url var="allSections">
    value="/view/instructor/project">
    <c:param name="projectPK" value="${project.projectPK}" />
    </c:url>
<p><a href="${allSections}">All Sections</a>
</p></c:when>

 <c:when test="${not empty sections && fn:length(sections) > 1}">
<c:url var="link"
    value="/view/instructor/projectTestResults.jsp"/>
<form method="post" action="${link}"><input
        type="hidden" name="projectPK" value="${project.projectPK}" />
        <p>Show just section:
        <select name="section">
        <c:forEach var="s" items="${sections}">
            <option><c:out value="${s}"></c:out>
             </c:forEach>
              </select>
            <input type="submit" value="go"/>
        </form>
</c:when>
</c:choose>

		<ss:projectTestResultsTable />
		
		<ss:projectLegend />
		
		<ss:studentsWithoutSubmissionsTable />
	</c:otherwise>

</c:choose>
<p>
<ss:footer />
</body>
</html>

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
<c:choose>
<c:when test="${empty studentRegistration}">
<ss:head title="${student.fullname} " />
</c:when>
<c:otherwise>
<ss:head title="${studentRegistration.fullname} -- ${course.courseName} " />
</c:otherwise>
</c:choose>

<body>
<ss:header />

<ss:instructorBreadCrumb />

<ss:studentPicture />
<c:choose>
<c:when test="${empty studentRegistration}">
<h1><c:out value="${student.fullname}"/></h1>
<p><ss:studentEmail/>
 <c:if test="${user.superUser}">
 <h2>Courses</h2>
 <ul>
   <c:forEach var="course" items="${studentCourseList}">
     <c:url var="courseLink" value="/view/instructor/student.jsp">
                    <c:param name="studentPK" value="${student.studentPK}" />
                    <c:param name="coursePK" value="${course.coursePK}" />
                    </c:url>
                     <li>
                        <a href="${courseLink}"><c:out value="${course.fullDescription}"/></a>
                        
                    </c:forEach>
                  
                    </ul>
                      </c:if>
</c:when>
<c:otherwise>
<h1><c:out value="${studentRegistration.fullname}"/></h1>
<c:if test="${not empty studentRegistration.section }">
<p>Section: <c:out value="${studentRegistration.section}"/>
</p></c:if>
<p><ss:studentEmail/>
<c:choose>
<c:when test="${studentRegistration.dropped}">
<p>Student has dropped course (as reported to grade server)
</c:when>
<c:when test="${!studentRegistration.active}">
<p>Student is inactive in course (as marked on grade server)
</c:when>
</c:choose>

<p>
<table>
    <tr>
        <th rowspan="2">project</th>
        <th colspan=2>submissions</th>
        <th rowspan="2">extension</th>
        <th class="description" rowspan="2">Title</th>
    </tr>
    <tr>
    <th>view</th>
    <th>#</th>
    </tr>

    <c:set var="numDisplayed" value="0" />
    <c:forEach var="project" items="${projectList}" varStatus="counter">
    <c:set var="submitStatus" value="${projectToStudentSubmitStatusMap[project.projectPK]}"/>
                
        <c:if test="${project.visibleToStudents || not empty submitStatus}}">
            <tr class="r${numDisplayed % 2}">

                <td><c:choose>
                    <c:when test="${project.url != null}">
                        <a href="<c:url value="${project.url}"/>">
                        ${project.projectNumber} </a>
                    </c:when>
                    <c:otherwise>
                    ${project.projectNumber}
                    </c:otherwise>
                </c:choose></td>

                <c:choose>
                <c:when test="${not empty submitStatus}">
                <td><c:url var="projectLink" value="/view/instructor/studentProject.jsp">
                    <c:param name="studentPK" value="${studentRegistration.studentPK}" />
                    <c:param name="projectPK" value="${project.projectPK}" />
                </c:url> <a href="${projectLink}"> view </a></td>

                <td><c:out  value="${submitStatus.numberSubmissions }"/>
                <td>
                <c:url var="grantExtensionLink" value="/view/instructor/grantExtension.jsp">
                    <c:param name="studentRegistrationPK" value="${studentRegistration.studentRegistrationPK}"/>
                    <c:param name="projectPK" value="${project.projectPK}"/>
                </c:url>
                    <a href="${grantExtensionLink}">
                    ${projectToStudentSubmitStatusMap[project.projectPK].extension}
                    </a>
                </td>
                </c:when>
                <c:otherwise>
                <td colspan="3">No submissions</td>
                </c:otherwise>
                </c:choose>

                <td class="description"><c:out value="${project.title}"/></td>

            </tr>
            <c:set var="numDisplayed" value="${numDisplayed + 1}" />
        </c:if>
    </c:forEach>
</table>

</c:otherwise>
</c:choose>



<ss:footer />
</body>
</html>

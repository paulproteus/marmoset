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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions'%>
<!DOCTYPE HTML>
<html>

<ss:head title="Superuser Update person" />
<body>
    <ss:header />
    <ss:instructorBreadCrumb />
    <h1>Superuser update person</h1>

    <c:url var="registerPerson" value="/view/instructor/registerPerson.jsp" />

    <p>
        <a href="${registerPerson}">Register person</a>
    </p>

    <c:url var="editStudentUrl" value="/view/admin/editStudent.jsp" />
    <c:url var="authenticateAsLink" value="/action/AuthenticateAs" />
    <c:url var="allowCourseCreationLink" value="/action/admin/AllowCourseCreation" />
    <c:url var="makeSuperuserLink" value="/action/admin/MakeSuperuser" />

    <table>
        <c:forEach var="student" items="${allStudents}" varStatus="counter">
            <tr class="r${counter.index % 2}">
                <td class="description"><c:out value="${student.fullname}" /></td>
                
                <td><form method="GET" action="${editStudentUrl}">
                        <input type="hidden" name="studentPK" value="${student.studentPK}" />
                        <button type="submit">Edit</button>
                    </form></td>
                <td><form method="post" action="${authenticateAsLink}">
                        <input type="hidden" name="studentPK" value="${student.studentPK}" />
                        <button type="submit">Become</button>
                    </form></td>
                <td><c:if test="${!student.canImportCourses}">
                        <form method="post" action="${allowCourseCreationLink}">
                            <input type="hidden" name="studentPK" value="${student.studentPK}" />
                            <button type="submit">Allow course creation</button>
                        </form>
                    </c:if></td>
                <td><c:if test="${student.canImportCourses && !student.superUser}">
                        <form method="post" action="${makeSuperuserLink}">
                            <input type="hidden" name="studentPK" value="${student.studentPK}" />
                            <button type="submit">Make superuser</button>
                        </form>
                    </c:if></td>
            </tr>
        </c:forEach>
    </table>

    <ss:footer />
</body>
</html>

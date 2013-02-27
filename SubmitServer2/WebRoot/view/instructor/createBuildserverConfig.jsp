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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${fn:length(instructorCourses) == 1}">
<jsp:forward page="/action/instructor/CreateBuildserverConfigFile">
<jsp:param name="buildserverCourse" value="${instructorCourses[0]}"/>
</jsp:forward>
</c:if>

<!DOCTYPE HTML>
<html>

<ss:head title="Create Buildserver config" />

<body>
    <ss:header />
    <ss:breadCrumb />

    <div class="sectionTitle">
        <h1>Create Buildserver config</h1>
        <ss:hello/>
    </div>

    <c:url var="url" value="/action/instructor/CreateBuildserverConfigFile" />
    <p></p>
    <form class="form" action="${url}" method="GET">
        <select name="buildserverCourse" multiple="multiple">
            <c:set var="statusMap" value="${userSession.instructorStatus}" />
            <c:forEach var="course" items="${courseList}">
                <c:if test="${user.superUser || statusMap[course.coursePK]}">
                    <option value="${course.coursePK}" SELECTED>
                        <c:out value="${course.fullDescription}" />
                    </option>
                </c:if>
            </c:forEach>
        </select>
        <p>
            <input type="submit" value="Create Buildserver config file">
        </p>
    </form>



    <ss:footer />
</body>
</html>

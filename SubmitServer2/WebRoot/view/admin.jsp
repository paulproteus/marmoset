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
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<c:if test="${!user.canImportCourses}">
      <c:redirect url="/view/index.jsp"/>
</c:if>

<!DOCTYPE HTML>
<html>
<head>
<ss:headContent title="Submit Server Administrative Actions" />
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.4/jquery.min.js" type="text/javascript"></script>
</head>
<body>
    <ss:header />
    <ss:breadCrumb />

    <div class="sectionTitle">
        <h1>Administrative actions</h1>
        <p class="sectionDescription">Welcome ${user.firstname}</p>
    </div>

        <ul>
            <c:if test="${user.superUser}">
                <li><a href="admin/">Administrator functions</a></li>
            </c:if>
            <c:choose>
                <c:when test="${grades.server}">
                    <c:url var="importCourseLink" value="/view/import/importCourse.jsp" />
                    <li><a href="${importCourseLink}">Import course from grade server</a></li>
                </c:when>
                <c:otherwise>
                    <c:url var="createCourseLink" value="/view/instructor/createCourse.jsp" />
                    <li><a href="${createCourseLink}">Create course</a></li>
                </c:otherwise>
            </c:choose>
            <c:url var="buildServerConfigLink" value="/view/instructor/createBuildserverConfig.jsp" />
            <li><a href="${buildServerConfigLink}">Generate buildserver config file</a></li>
            <c:if test="${not empty userSession.superuserPK}">
            <c:url var="authenticateAsLink" value="/action/AuthenticateAs" />     
               <li><form method="POST" action="${authenticateAsLink}">
                <input type="hidden" name="studentPK" value="${userSession.superuserPK}"/>
                <input type="submit" value="Become superuser" />
                </form>
           </li>
            </c:if>
        </ul>


    <ss:footer />

 
</body>
</html>

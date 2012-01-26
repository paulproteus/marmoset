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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions'%>
<!DOCTYPE HTML>
<html>

<ss:head title="Superuser menu" />
<body>
    <ss:header />
    <ss:instructorBreadCrumb />
    <h1>Superuser info/status</h1>

    <c:if test="${! empty recentErrors }">
        <h2>Recent errors</h2>

        <table>
            <tr>
                <th></th>
                <th>When</th>
                <th>Message <c:forEach var="error" items="${recentErrors}" varStatus="counter">
                        <tr class="$rowKind">
                            <td>${error.errorPK}</td>
                            <td><fmt:formatDate value="${error.when}" pattern="dd MMM, hh:mm a" /></td>
                            <td class="description"><c:out value="${error.message}" /></td>
                        </tr>
                    </c:forEach>
                </th>
            </tr>
        </table>
    </c:if>

    <c:if test="${! empty coursesThatNeedBuildServers }">
        <h2>Courses that need build servers</h2>
        <ul>
            <c:forEach var="course" items="${coursesThatNeedBuildServers}">
                <c:url var="courseLink" value="/view/instructor/course.jsp">
                    <c:param name="coursePK" value="${course.coursePK}" />
                </c:url>
                <li><a href="${courseLink}"> 
                <c:out value="${course.fullDescription}" /> 
                </a></li>
            </c:forEach>
        </ul>
    </c:if>
    <p>Server load: ${systemLoad}</p>
    <h2>BuildServers</h2>
    <table>
        <tr>
            <th>Host</th>
            <th>Last request</th>
            <th>now building</th>
            <th>Last job</th>
            <th>Last success</th>
            <th>Load</th>
        </tr>

        <c:forEach var="buildServer" items="${buildServers}" varStatus="counter">
            <tr class="$rowKind">
                <td><c:out value="${buildServer.name}" /></td>
                <td><fmt:formatDate value="${buildServer.lastRequest}" pattern="dd MMM, hh:mm a" /></td>
                <td><c:if test="${buildServer.lastRequestSubmissionPK > 0}">
                <c:url var="s" value="/view/instructor/submission.jsp">
                <c:param name="submissionPK" value="${buildServer.lastRequestSubmissionPK}" />
                </c:url>
                <a href="${s}">${buildServer.lastRequestSubmissionPK}</a>
                </c:if>
                <td><fmt:formatDate value="${buildServer.lastSuccess}" pattern="dd MMM, hh:mm a" /></td>

                <td><fmt:formatDate value="${buildServer.lastSuccess}" pattern="dd MMM, hh:mm a" /></td>
                <td class="description"><c:out value="${buildServer.load}" /></td>
            </tr>
        </c:forEach>
    </table>



    <h2>Upcoming project deadlines</h2>
    <table>
        <tr>
            <th rowspan="2">Course</th>
            <th rowspan="2">Project</th>
            <th colspan="2">Deadlines</th>
            <th colspan="5">build status</th>
        </tr>
        <tr>
            <th>ontime</th>
            <th>late</th>
            <th>new</th>
            <th>pending</th>
            <th>complete</th>
            <th>retest</th>
            <th>broken</th>
        </tr>

        <c:forEach var="project" items="${upcomingProjects}" varStatus="counter">
            <tr class="$rowKind">

                <td class="description"><c:out value="${courseMap[project.coursePK].courseName}" /></td>

                <td class="description"><c:url var="projectLink" value="/view/instructor/project.jsp">
                        <c:param name="projectPK" value="${project.projectPK}" />
                    </c:url> <a href="${projectLink}"> <c:out value="${project.fullTitle}"/> </a></td>
                <td><fmt:formatDate value="${project.ontime}" pattern="dd MMM, hh:mm a" /></td>
                <td><fmt:formatDate value="${project.late}" pattern="dd MMM, hh:mm a" /></td>
                <c:choose>
                    <c:when test="${project.tested}">
                        <td><c:out value="${buildStatusCount[project]['new']}" /></td>
                        <td><c:out value="${buildStatusCount[project]['pending']}" /></td>
                        <td><c:out value="${buildStatusCount[project]['complete']}" /></td>
                        <td><c:out value="${buildStatusCount[project]['retest']}" /></td>
                        <td><c:out value="${buildStatusCount[project]['broken']}" /></td>
                    </c:when>
                    <c:otherwise>
                        <td colspan="5"><c:out value="${buildStatusCount[project]['accepted']}" /> accepted</td>
                    </c:otherwise>
                </c:choose>
            </tr>

        </c:forEach>
    </table>


    <c:if test="${! empty slowSubmissions}">
        <h2>Submissions that took a long time to test</h2>
        <table>

            <tr>
                <th>Course</th>
                <th>Project</th>
                <th>sub pk</th>
                <th>delay</th>
                <th>build status</th>
                <th>submitted</th>

            </tr>
            <c:forEach var="submission" items="${slowSubmissions}" varStatus="counter">

                <c:set var="project" value="${projectMap[submission.projectPK]}" />
                <c:set var="course" value="${courseMap[project.coursePK]}" />

                <c:url var="submissionLink" value="/view/instructor/submission.jsp">
                    <c:param name="submissionPK" value="${submission.submissionPK}" />
                </c:url>
                <c:url var="projectLink" value="/view/instructor/project.jsp">
                    <c:param name="projectPK" value="${project.projectPK}" />
                </c:url>
                <c:url var="courseLink" value="/view/instructor/course.jsp">
                    <c:param name="coursePK" value="${course.coursePK}" />
                </c:url>

                <tr class="$rowKind">
                    <td class="description"><a href="${courseLink}"> <c:out value="${course.courseName}" />
                    </a></td>

                    <td class="description"><a href="${projectLink}"> <c:out value="${project.projectNumber}" />
                            <c:out value="${project.title}" />
                    </a></td>
                    <td><a href="${submissionLink}">${submission.submissionPK}</a></td>
                    <td>${testDelay[submission]}</td>
                    <td>${submission.buildStatus}</td>
                    <td><fmt:formatDate value="${submission.submissionTimestamp}" pattern="dd MMM h:mm a" /></td>



                </tr>
            </c:forEach>
        </table>
    </c:if>


    <h1>Administrative actions</h1>
    <c:if test="${gradesServer}">
        <h2>
            Synchronize with <a href="http://grades.cs.umd.edu">grades.cs.umd.edu</a>
        </h2>
        <c:url var="importInstructorsLink" value="/action/admin/ImportInstructors" />
        <c:url var="syncStudentsLink" value="/action/admin/SyncStudents" />

        <table class="form">
            <tbody>
                <form action="${importInstructorsLink}" method="post" name="importInstructorsForm">

                    <tr>
                        <td class="label">Semester:</td>
                        <td class="input"><input type="text" name="semester" value="${initParam['semester']}" /></td>
                    </tr>
                    <tr class="submit">
                        <td colspan="2"><input type="submit" value="Import all instructors"></td>
                    </tr>
                </form>
            </tbody>
            <tbody>

                <form action="${syncStudentsLink}" method="post" name="syncStudents">
                    <tr class="submit">
                        <td colspan="2"><input type="submit" value="Synchronize students"></td>
                    </tr>
                </form>
            </tbody>

        </table>
        <form action=""></form>
    </c:if>


    <h2>Create course</h2>
    <ss:createCourseForm/>
    
    <h2>User actions</h2>
    	<h3>Edit student account</h3>
    	<c:url var="editStudentUrl" value="/view/admin/editStudent.jsp" />
    	<form method="GET" action="${editStudentUrl}">
              <select name="studentPK">
                 <c:forEach var="student" items="${allStudents}" varStatus="counter">
                    <option value="${student.studentPK}">
                        <c:out value="${student.fullname}" />
                    </option>
                </c:forEach>
            </select> 
    		<button type="submit">Edit</button>
    	</form>

        <h3>Allow course creation</h3>
        <c:url var="allowCourseCreationLink" value="/action/admin/AllowCourseCreation" />
        <p>
        <form name="AllowCourseCreation" method="post" action="${allowCourseCreationLink}">
            <select name="studentPK">
                <c:forEach var="student" items="${allStudents}" varStatus="counter">
                  <c:if test="${!student.canImportCourses}">
                    <option value="${student.studentPK}">
                        <c:out value="${student.fullname}" />
                    </option>
                    </c:if>
                </c:forEach>
            </select> <input type="submit" value="Allow course creation" />
        </form>


    <h3>Authenticate as</h3>
    <p>This allows you to log in as any other user, and allow you to view the submit server as that user would. Once
        you have authenticated as another user, you will have to log out and log in as yourself in order to perform
        actions as yourself.</p>

    <c:url var="authenticateAsLink" value="/action/AuthenticateAs" />

    <p>
    <form name="AuthenticateAs" method="post" action="${authenticateAsLink}">
        <select name="studentPK">
            <c:forEach var="student" items="${allStudents}" varStatus="counter">
                <option value="${student.studentPK}">
                    <c:out value="${student.fullname}" />
                </option>
            </c:forEach>
        </select> <input type="submit" value="Authenticate as" />
    </form>

    <c:url var="makeSuperuserLink" value="/action/admin/MakeSuperuser" />

    <h3>Create superuser account</h3>
    <p>
    <form name="MakeSuperuser" method="post" action="${makeSuperuserLink}">
        <select name="studentPK">
            <c:forEach var="student" items="${allStudents}" varStatus="counter">
                <c:if test="${!student.superUser && student.canImportCourses }">
                    <option value="${student.studentPK}">
                        <c:out value="${student.fullname}" />
                    </option>
                </c:if>
            </c:forEach>
        </select> <input type="submit" value="Make superuser account for" />
    </form>

   <c:url var="registerPerson" value="/view/instructor/registerPerson.jsp" />

    <p><a href="${registerPerson}">Register person</a></p>
    <ss:footer />
</body>
</html>

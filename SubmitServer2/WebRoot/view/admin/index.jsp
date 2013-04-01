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

<ss:head title="Superuser menu" />
<body>
    <ss:header />
    <ss:instructorBreadCrumb />
    <h1>Superuser info/status</h1>

    <c:if test="${! empty recentExceptions }">
        <h2>  <a href="javascript:toggle('exceptions')">${fn:length(recentExceptions)} Recent Exceptions</a> </h2>
        <div id="exceptions" style="display: none">
        <table>
            <tr>
                <th>PK</th>
                <th>When</th>
                <th>User</th>
                <th>Message</th>
            </tr>
            <c:forEach var="error" items="${recentExceptions}" varStatus="counter">
                    <tr class="r${counter.index % 2}">
                    <td><c:url var="errorLink" value="error.jsp">
                            <c:param name="errorPK" value="${error.errorPK}" />
                        </c:url> <a href="${errorLink}">${error.errorPK}</a></td>
                    <td><fmt:formatDate value="${error.when}" pattern="dd MMM, hh:mm a" /></td>
                     <td><c:if test="${not empty error.userPK}">
                                <c:url var="userLink" value="/view/instructor/student.jsp">
                                    <c:param name="studentPK" value="${error.userPK}" />
                                </c:url>
                             <a href="${userLink}">${error.userPK}</a>
                            </c:if></td>
                    <td class="description"><c:out value="${error.message}" /></td>
                </tr>
            </c:forEach>
        </table>
        </div>
    </c:if>

      <c:if test="${! empty recentErrors }">
        <h2> <a href="javascript:toggle('errorList')" title="Click to toggle display of errors" id="errors"> 
        ${fn:length(recentErrors)} Recent errors
        </a></h2>
        
        <div  id="errorList" style="display: none">
        <table>
                <tr>
                    <th>PK</th>
                    <th>User</th>
                    <th>When</th>
                    <th>Kind</th>
                    <th>Message</th>
                </tr>
                <c:forEach var="error" items="${recentErrors}" varStatus="counter">
                    <tr class="r${counter.index % 2}">
                        <td><c:url var="errorLink" value="error.jsp">
                                <c:param name="errorPK" value="${error.errorPK}" />
                            </c:url> <a href="${errorLink}">${error.errorPK}</a></td>
                        <td><c:if test="${not empty error.userPK}">
                                <c:url var="userLink" value="/view/instructor/student.jsp">
                                    <c:param name="studentPK" value="${error.userPK}" />
                                </c:url>
                             <a href="${userLink}">${error.userPK}</a>
                            </c:if></td>
                        <td><fmt:formatDate value="${error.when}" pattern="dd MMM, hh:mm a" /></td>
                        <td>${error.kind}
                        <td class="description"><c:out value="${error.message}" /></td>
                    </tr>
                </c:forEach>
            </table>
        </div>
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
            <tr class="r${counter.index % 2}">
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


  <h2><a href="javascript:toggle('upcomindCodeReviews')">Upcoming code review assignments</a></h2>
    <div id="upcomindCodeReviews" style="display: none">
    <table>
        <tr>
            <th >Course</th>
            <th >Project</th>
            <th >Deadline</th>
             <th >Kind</th>
               <th >Visible</th>
            <th >Description</th>
        </tr>
       

        <c:forEach var="codeReviewAssignment" items="${upcomingCodeReviewAssignments}" varStatus="counter">
            <c:set var="project" value="${projectMap[codeReviewAssignment.projectPK]}"/>
            <c:set var="course" value="${courseMap[project.coursePK]}"/>
            <c:url var="reviewLink" value="/view/instructor/codeReviewAssignment.jsp">
                        <c:param name="codeReviewAssignmentPK" value="${codeReviewAssignment.codeReviewAssignmentPK}" />
                    </c:url>
                <tr class="r${counter.index % 2}">

                    <td class="description"><c:out value="${course.courseName}" /></td>
                    <td class="description"><c:out value="${project.fullTitle}" /></td>
                    <td><fmt:formatDate value="${codeReviewAssignment.deadline}" pattern="dd MMM, hh:mm a" /></td>
                    <td><c:out value="${codeReviewAssignment.kind}" /></td>
                    <td><c:if test="${not empty codeReviewAssignment.visibleToStudents}">
                            <input type="checkbox" checked="checked" onclicked="return false;" />
                        </c:if></td>
                    <td class="description"><a href="${reviewLink}"> <c:out value="${codeReviewAssignment.description}" />
                    </a></td>
                </tr>
            </c:forEach>
    </table>
    </div>

    <h2><a href="javascript:toggle('upcomingProjects')">Upcoming project deadlines</a></h2>
    <div id="upcomingProjects" style="display: none">
    <table>
        <tr>
            <th rowspan="2">Course</th>
            <th rowspan="2">Project</th>
            <th rowspan="2">has baseline
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
            <tr class="r${counter.index % 2}">

                <td class="description"><c:out value="${courseMap[project.coursePK].courseName}" /></td>

                <td class="description"><c:url var="projectLink" value="/view/instructor/project.jsp">
                        <c:param name="projectPK" value="${project.projectPK}" />
                    </c:url> <a href="${projectLink}"> <c:out value="${project.fullTitle}"/> </a></td>
                <td><c:if test="${not empty project.archivePK}">
                <input type="checkbox" checked="checked" onclicked="return false;"/>
                </c:if></td>
                <td><fmt:formatDate value="${project.ontime}" pattern="dd MMM, hh:mm a" /></td>
                <td>
                <c:if test="${project.ontime != project.late}">
                <fmt:formatDate value="${project.late}" pattern="dd MMM, hh:mm a" /></c:if></td>
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
    </div>


    <c:if test="${! empty slowSubmissions}">
        <h2><a href="javascript:toggle('slowSubmissions')">${fn:length(slowSubmissions)} Submissions that took a long time to test</a></h2>
        <div id="slowSubmissions" style="display: none"><table>

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

                    <tr class="r${counter.index % 2}">
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
        </div>
    </c:if>

<c:if test="${not empty brokenSubmissions}">
<h2><a href="javascript:toggle('brokenSubmissions')">${fn:length(brokenSubmissions)} Broken submissions</a></h2>

   <div id="brokenSubmissions" style="display: none"><table>

            <tr>
                <th>Course</th>
                <th>Project</th>
                <th>sub pk</th>
                <th>build requests</th>
                <th>submitted</th>

            </tr>
            <c:forEach var="submission" items="${brokenSubmissions}" varStatus="counter">

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

                    <tr class="r${counter.index % 2}">
                    <td class="description"><a href="${courseLink}"> <c:out value="${course.courseName}" />
                    </a></td>

                    <td class="description"><a href="${projectLink}"> <c:out value="${project.projectNumber}" />
                            <c:out value="${project.title}" />
                    </a></td>
                    <td><a href="${submissionLink}">${submission.submissionPK}</a></td>
                    <td>${submission.numPendingBuildRequests}</td>
                    <td><fmt:formatDate value="${submission.submissionTimestamp}" pattern="dd MMM h:mm a" /></td>



                </tr>
            </c:forEach>
        </table>
        </div>
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
                        <td class="input"><input type="text" name="semester" value="${ss:webProperty('semester')}" /></td>
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
    <c:url var="registerPerson" value="/view/instructor/registerPerson.jsp" />

    <ul>
        <li><a href="${registerPerson}">Register person</a>
        <li><a href="person.jsp">Become/Update person</a>
    </ul>
    <ss:footer />
</body>
</html>

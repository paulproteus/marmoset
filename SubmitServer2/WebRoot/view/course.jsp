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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML>
<html>
<ss:head title="${course.courseName} Submit Server" />

<body>
    <ss:header />
    <ss:breadCrumb />

    <div class="sectionTitle">
        <h1>
            <c:choose>
        <c:when test="${not empty course.url}">
            <a href="${course.url}"><c:out value="${course.fullDescription}"/></a>
            </c:when>
            <c:otherwise>
            <c:out value="${course.fullDescription}"/>
            </c:otherwise>
            </c:choose>
        </h1>

        <ss:hello />
    </div>
    
    <c:if test="${studentRegistration.dropped}">
    <h2>You are listed as dropped in the course</h2>

      <p>The last time the submit server was synchronized with grades.cs.umd.edu,
      it reported that you have dropped the course. The grades server gets this information from UMEG. 
      If you do not have any submissions or code reviews, your registration on the submit server will be deleted.
      Please contact the instructor ASAP if you have not dropped the course.
      </p></c:if>
      
       <c:if test="${studentRegistration.inactive}">
    <h2>You are listed as inactive in the course</h2>

      <p>The last time the submit server was synchronized with grades.cs.umd.edu,
      it reported that are inactive in this course. The instructional staff marks you as inactive on grades.cs.umd.edu if they believe you have stopped participating in the course, so that they 
      know not to bug you about missing assignments. If you still believe yourself to be active, please contact the instructor ASAP.
       
      </p></c:if>

    <ss:codeReviews title="Pending Code reviews" />

    <h2>Projects</h2>
    <p>
        <c:set var="anyDownload" value="0" />
        <c:if test="${course.allowsBaselineDownload}">
            <c:forEach var="project" items="${projectList}">
                <c:if test="${project.archivePK != null && project.archivePK > 0}">
                    <c:set var="anyDownload" value="1" />
                </c:if>
            </c:forEach>
        </c:if>
    <table>
        <tr>
            <th rowspan="2">project</th>
            <th rowspan="2">submissions</th>
            <th colspan="2">last submission <br>test results
            <th rowspan="2">web<br> submission
            </th>
            <c:if test="${anyDownload > 0}">
                <th rowspan="2">download<br> starter<br> files
                </th>
            </c:if>
            <th rowspan="2">Due</th>
            <th rowspan="2" class="description">Title</th>
        </tr>
        <tr>
            <th>Public
            <th>Release <c:set var="numDisplayed" value="0" /> <c:forEach var="project" items="${projectList}"
                    varStatus="counter">

<c:url var="submitProjectLink"
                                            value="/view/submitProject.jsp">
                                            <c:param name="projectPK" value="${project.projectPK}" />
                                            <c:param name="testSetupPK" value="${project.testSetupPK}" />
                                        </c:url> 
                                        
                    <c:if test="${project.visibleToStudents || instructorActionCapability || instructorCapability}">
                        <c:url var="projectLink" value="/view/project.jsp">
                            <c:param name="projectPK" value="${project.projectPK}" />
                        </c:url>

                        <c:set var="projectURL">
                            <c:out value="${project.url}" />
                        </c:set>

                        <c:set var="submission" value="${myMostRecentSubmissions[project.projectPK]}" />
                        <c:set var="testSetup" value="${currentTestSetups[project.projectPK]}" />

                        <tr class="r${numDisplayed % 2}">

                            <td><c:choose>

                                    <c:when test="${not empty projectURL}">
                                        <a href="${projectURL}" title="Project description"><c:out
                                                value="${project.projectNumber}" /></a>
                                    </c:when>

                                    <c:otherwise>
                                        <a href="${projectLink}" title="Project page"><c:out
                                                value="${project.projectNumber}" /></a>
                                    </c:otherwise>

                                </c:choose> <c:if test="${!project.visibleToStudents}">
				(invisible to students)</c:if></td>

                            <c:choose>
                                <c:when test="${empty submission}">
                                    <td colspan="4"><a href="${submitProjectLink}"> submit </a></td>
                                </c:when>
                                <c:otherwise>

                                    <td title="Project page"><a href="${projectLink}"> view </a></td>

                                    <c:choose>
                                        <c:when test="${empty submission}">
                                            <td colspan="2" />
                                        </c:when>
                                        <c:when test="${submission.buildStatus != 'COMPLETE' }">
                                            <td colspan="2" />${submission.buildStatus}
                </c:when>
                                        <c:when test="${!submission.compileSuccessful }">
                                            <td colspan="2" />did not compile
                </c:when>
                                        <c:otherwise>
                                            <td><c:choose>
                                                    <c:when test="${testSetup.valuePublicTests > 0}">
                ${submission.valuePublicTestsPassed} / ${testSetup.valuePublicTests}
                </c:when>
                                                    <c:otherwise>compiled</c:otherwise>
                                                </c:choose></td>
                                            <td><c:choose>
                                                    <c:when test="${testSetup.valueReleaseTests == 0}">
                                                    </c:when>
                                                    <c:when test="${submission.releaseTestingRequested}">
                            ${submission.valueReleaseTestsPassed} /
                            ${testSetup.valueReleaseTests}
                            </c:when>
                                                    <c:when test="${submission.releaseEligible}">

                                                        <c:url var="releaseRequestLink"
                                                            value="/view/confirmReleaseRequest.jsp">
                                                            <c:param name="submissionPK"
                                                                value="${submission.submissionPK}" />
                                                        </c:url>
                                                        <a href="${releaseRequestLink}">?</a>
                                                    </c:when>
                                                    <c:otherwise>-</c:otherwise>
                                                </c:choose></td>
                                        </c:otherwise>
                                    </c:choose>
                                    <td title="Make submission for the project"><a href="${submitProjectLink}"> submit </a></td>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${anyDownload > 0}">

                                <td title="Download starter files for project"><c:if
                                        test="${project.archivePK != null && project.archivePK > 0}">
                                        <c:url var="downloadStarterFilesLink" value="/data/DownloadProjectStarterFiles">
                                            <c:param name="projectPK" value="${project.projectPK}" />
                                        </c:url>
                                        <a href="${downloadStarterFilesLink}"> download </a>
                                    </c:if></td>
                            </c:if>

                            <td><fmt:formatDate value="${project.ontime}" pattern="dd MMM, hh:mm a" /></td>
                            <td class="description"><c:choose>
                                    <c:when test="${not empty projectURL}">
                                        <a href="${projectURL}" title="Project description"><c:out
                                                value="${project.title}" /> </a>
                                    </c:when>

                                    <c:otherwise>
                                        <a href="${projectLink}" title="Project page"> <c:out
                                                value="${project.title}" /></a>
                                    </c:otherwise>
                                </c:choose>
                        </tr>
                        <c:set var="numDisplayed" value="${numDisplayed + 1}" />
                    </c:if>
                </c:forEach>
    </table>
    
    <h2>Feeds</h2>
          <c:url var="courseCalendarLink" value="/feed/CourseCalendar">
                            <c:param name="courseKey" value="${course.courseKey}" />
                        </c:url>
    <ul><li><a href="${courseCalendarLink}">Course calendar link</a> (iCalendar .ics format: 
    subscribe in iCal, Google calendar, Outlook)
    </ul>

    <ss:footer />
</body>
</html>

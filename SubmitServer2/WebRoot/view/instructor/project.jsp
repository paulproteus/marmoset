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
	title="Overview of ontime/late/very late results for all student submissions for project ${project.projectNumber}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<ss:projectTitle />
<ss:projectMenu />



    <c:choose>
        <c:when test="${not empty section}">
            <c:url var="allSections" value="/view/instructor/project.jsp">
                <c:param name="projectPK" value="${project.projectPK}" />
            </c:url>
            <p>
                Showing just Section <c:out value="${section}"/>.
                <a href="${allSections}">Show all Sections</a>
            </p>
        </c:when>
        <c:when test="${not empty sections && fn:length(sections) > 1}">
            <c:url var="link" value="/view/instructor/project.jsp" />
            <form method="GET" action="${link}">
                <input type="hidden" name="projectPK" value="${project.projectPK}" />
                <p>
                    Show just section: <select name="section">
                        <c:forEach var="s" items="${sections}">
                            <option>
                                <c:out value="${s}"></c:out>
                        </c:forEach>
                    </select> <input type="submit" value="go" />
            </form>
        </c:when>
    </c:choose>

    <c:if test="${empty studentRegistrationSet}">
	<c:url var="createDotSubmitFileLink" value="/data/instructor/CreateDotSubmitFile">
			<c:param name="projectPK" value="${project.projectPK}"/>
		</c:url>

<p>No submissions.</p>
<ul>
<li><p><a href="${createDotSubmitFileLink}">Get .submit file for this project </a></p>
<li><p>
                       The canonical account for this project
                        is <c:out value="${canonicalAccount.fullname}" />.
                        The canonical account can be changed via 
                        <a href="${updateProjectLink}"> the project update screen</a>. New
                        test setups are checked against the most recent submission from
                        the canonical account for the project.
                    </p>
                    </li>


                    </ul>

</c:if>

<c:if test="${canonicalAccount.studentPK == userSession.studentPK}">
<c:url var="submitProjectLink"
                    value="/view/submitProject.jsp">
                    <c:param name="projectPK" value="${project.projectPK}" />
                    <c:param name="testSetupPK" value="${project.testSetupPK}" />
                </c:url>
<p> <a href="${submitProjectLink}"> web submission of canonical submission</a></p>
</c:if>

<c:if test="${project.tested}">
<div class="projectvitalstats">
		<c:set var="waitingToBeTested"
		value="${ss:numToTest(project.projectPK, connection)}" />
	<c:if test="${waitingToBeTested > 0}">
		<p>${waitingToBeTested} submissions waiting to be tested
		</p>
	</c:if>

	<c:set var="waitingToBeRetested"
		value="${ss:numForRetest(project.projectPK, connection)}" />
	<c:if test="${waitingToBeRetested > 0}">
		<p>${waitingToBeRetested} submissions requiring retest (because of a new test
		setup activation) </p>
	</c:if>

	<c:set var="inconsistentResults"
		value="${fn:length(failedBackgroundRetestSubmissionList)}" />
	<c:if test="${inconsistentResults > 0}">
		<c:url var="smallfailedBackgroundRetestLink"
			value="/view/instructor/failedBackgroundRetests.jsp">
			<c:param name="projectPK" value="${project.projectPK}" />
		</c:url>

		<p><a href="${smallfailedBackgroundRetestLink}"> ${inconsistentResults} submissions with inconsistent
		results (from a background test)
		 </a></p>
	</c:if>

</div>
</c:if>

<c:if test="${!project.visibleToStudents}">
    <c:url var="makeVisibleLink"
        value="/action/instructor/MakeProjectVisible" />
    <form method="post" action="${makeVisibleLink}"><input
        type="hidden" name="projectPK" value="${project.projectPK}" />
    <p>Currently invisible to students.
     <input type="hidden" name="newValue" value="true"/>
    <input type="submit" value="Make Visible" style="color: #003399" /></form>
</c:if>

<c:url var="codeReviewAssignmentLink"
			value="/view/instructor/createCodeReviewAssignment.jsp">
			<c:param name="projectPK" value="${project.projectPK}" />
		</c:url>
<c:choose>

<c:when test="${empty studentRegistrationSet}">
</c:when>
<c:when test="${empty reviewAssignmentsForProject && project.afterLateDeadline}">
<p> <a href="${codeReviewAssignmentLink}">Create code review assignment</a>
</c:when>

<c:when test="${empty reviewAssignmentsForProject && !project.afterLateDeadline}">
<p> <a href="${codeReviewAssignmentLink}">Create early code review assignment</a>
</c:when>

<c:when test="${! empty reviewAssignmentsForProject  }">
	<h2>Code review assignments</h2>
	<ul>
		<c:forEach var="codeReviewAssignment"
			items="${reviewAssignmentsForProject}">
			<li><c:url var="codeReviewAssignmentLink"
				value="/view/instructor/codeReviewAssignment.jsp">
				<c:param name="codeReviewAssignmentPK"
					value="${codeReviewAssignment.codeReviewAssignmentPK}" />
			</c:url> <a href="${codeReviewAssignmentLink}">
			<c:out value="${codeReviewAssignment.description}"/>,
			Due <fmt:formatDate
				value="${codeReviewAssignment.deadline}" pattern="dd MMM, hh:mm a" />
			</a></li>
		</c:forEach>

        <c:if test="${project.afterLateDeadline}">
		<c:url var="createCodeReviewAssignmentLink"
				value="/view/instructor/createCodeReviewAssignment.jsp">
				<c:param name="projectPK"
					value="${project.projectPK}" />
					</c:url>


		<li><a href="${createCodeReviewAssignmentLink}">Create code review</a>
		</li>
        </c:if>
	</ul>
</c:when>
</c:choose>

<ss:codeReviews title="Code reviews"/>

<h1>Submissions</h1>
<c:if test="${project.tested}">
<c:choose>
<c:when test="${empty testSetup}">
<p>No test setup activated.
</c:when>
<c:when test="${waitingToBeTested == 0 &&  waitingToBeRetested == 0 && inconsistentResults == 0 && not empty studentRegistrationSet}">
	<p>All submissions tested; results consistent.</p>
	</c:when>
	</c:choose>


	<c:url var="projectUtilitiesLink"
		value="/view/instructor/projectUtilities.jsp">
		<c:param name="projectPK" value="${project.projectPK}" />
	</c:url>
	<c:choose>
		<c:when test="${empty allTestSetups}">
			<p> <c:url
				var="uploadTestSetupLink" value="/action/instructor/UploadTestSetup" />
			<form name="submitform" action="${uploadTestSetupLink}"
				enctype="multipart/form-data" method="POST"><input
				type="hidden" name="projectPK" value="${project.projectPK}">
		No test setups for this project. <input type="hidden" name="comment" value=""/>
		<a title="zip/jar file to upload"><input type="file" name="file"
				size=40></a> <input type="submit" value="Upload"></form>
		</c:when>
		<c:when test="${testSetup != null && testSetup.jarfileStatus == 'active'}">
			<c:url var="canonicalRunLink" value="/view/instructor/submission.jsp">
				<c:param name="testRunPK" value="${testSetup.testRunPK}" />
			</c:url>

			<p><a href="${canonicalRunLink}">Test setup
			${testSetup.version}</a>, posted <fmt:formatDate
				value="${testSetup.datePosted}" pattern="dd MMM, hh:mm a" /> :
			${testSetup.comment}
		</c:when>
		<c:otherwise>
			<p><a href="${projectUtilitiesLink}">View and/or activate
			test setup...</a>
		</c:otherwise>
	</c:choose>
</c:if>





<c:if test="${project.visibleToStudents && not empty studentsWithoutSubmissions}">
<p><a href="#studentsWithoutSubmissions">${fn:length(studentsWithoutSubmissions)} active students 
without submissions</a>
</c:if>

<c:url var="sortByTime"
	value="${ss:scrub(pageContext.request.pathTranslated)}">
	<c:param name="projectPK" value="${project.projectPK}" />
	<c:param name="sortKey" value="time" />
</c:url>
<c:url var="sortByName"
	value="${ss:scrub(pageContext.request.pathTranslated)}">
	<c:param name="projectPK" value="${project.projectPK}" />
</c:url>
<c:url var="sortByAcct"
	value="${ss:scrub(pageContext.request.pathTranslated)}">
	<c:param name="projectPK" value="${project.projectPK}" />
	<c:param name="sortKey" value="account" />
</c:url>
<c:url var="sortBySection"
    value="${ss:scrub(pageContext.request.pathTranslated)}">
    <c:param name="projectPK" value="${project.projectPK}" />
    <c:param name="sortKey" value="section" />
</c:url>


    
<c:set var="anyOutdated" value="false"/>
<p>
<table>
	<tr>
		<th>#</th>
		<th><a href="${sortByName}">Name</a></th>
		<c:if test="${not empty partnerMap}">
		<th>Partner</th>
		</c:if>
		<th><a href="${sortByAcct}">Acct</a></th>
        <c:if test="${not empty sections}">
        <th><a href="${sortBySection}">Section</a></th>
        </c:if>
		<th>#<br>
		subs</th>
		<!-- 
		<c:if test="${testProperties.language=='java'}">
		<th>#<br>
		runs</th>
		</c:if>
		 -->

		<c:if test="${project.tested && inconsistentResults > 0}">
		<th># inconsistent<br>
		background<br>
		retests</th>
		</c:if>
		<th><a href="${sortByTime}">last submission</a></th>
		<th># changed <br/>lines</th>
		<th>on time<br>
		<acronym title="Public Tests">P</acronym> | <acronym
			title="Release Tests">R</acronym> | <acronym title="Secret Tests">S</acronym>
		<c:if test="${testProperties.language=='java'}">
					 | <acronym title="Findbugs Warnings">F</acronym>
		</c:if></th>
		<th>late</th>
		<th>very late</th>
		<th>extension</th>
	</tr>

	<c:forEach var="studentRegistration" items="${studentRegistrationSet}"
		varStatus="counter">
		<c:set var="thisLastSubmission" value="${lastSubmission[studentRegistration.studentRegistrationPK]}"/>
		<c:set var="thisOntimeSubmission" value="${lastOnTime[studentRegistration.studentRegistrationPK]}"/>
		<c:set var="thisLateSubmission" value="${lastLate[studentRegistration.studentRegistrationPK]}"/>
		<c:set var="thisVLateSubmission" value="${lastVeryLate[studentRegistration.studentRegistrationPK]}"/>

		<c:url var="studentLink"
					value="/view/instructor/student.jsp">
				<c:param name="studentPK" value="${studentRegistration.studentPK}" />
				<c:param name="coursePK" value="${course.coursePK}" />
			</c:url>

		<tr class="r${counter.index % 2}">

			<td>${1+counter.index}</td>

			<td class="description"><c:if
				test="${studentRegistration.instructorLevel > 0}">* </c:if>
				<a href="${studentLink}" title="info on student">
				<c:out value="${studentRegistration.fullname}"/></a></td>
                
                
			<c:if test="${not empty partnerMap}">
			<c:set var="partner" value="${partnerMap[studentRegistration]}"/>
			<td>
			<c:if test="${not empty partner }">
			<c:out value="${partner.lastname}, ${partner.firstname}"/>
			</c:if>
			</td>
			</c:if>

			<td class="description"><c:url var="studentProjectLink"
				value="/view/instructor/studentProject.jsp">
				<c:param name="projectPK" value="${project.projectPK}" />
				<c:param name="studentPK" value="${studentRegistration.studentPK}" />
			</c:url> <a href="${studentProjectLink}" title="info on student's project submissions">
			<c:out value="${studentRegistration.classAccount}"/> </a></td>
            <c:if test="${not empty sections}">
            <td><c:out value="${studentRegistration.section}"/> </td></c:if>
      
			<td class="number">
			${studentSubmitStatusMap[studentRegistration.studentRegistrationPK].numberSubmissions}
			</td>
			<!-- 
			<c:if test="${testProperties.language=='java'}">
			<td class="number">
			${studentSubmitStatusMap[studentRegistration.studentRegistrationPK].numberRuns}
			</td>
			</c:if>
			 -->

			<c:if test="${project.tested && inconsistentResults > 0}">
			<td class="number">
			<c:if test="${thisLastSubmission.numFailedBackgroundRetests > 0}">
			${thisLastSubmission.numFailedBackgroundRetests}
			</c:if>
			</td>
			</c:if>

			<td><c:if
				test="${not empty thisLastSubmission}"><c:url var="submissionLink"
				value="${ss:instructorSubmissionViewLink(project,thisLastSubmission)}" />
			<a href="${submissionLink}"><fmt:formatDate
				value="${thisLastSubmission.submissionTimestamp}"
				pattern="E',' dd MMM 'at' hh:mm a" /></a></c:if></td>

			<td><c:out value="${ss:numberChangedLines(connection, thisLastSubmission, project)}"/>

			<td><c:if
				test="${not empty thisOntimeSubmission}">
				<c:url var="submissionLink"
					value="${ss:instructorSubmissionViewLink(project,thisOntimeSubmission)}" />
				<a href="${submissionLink}">${thisOntimeSubmission.testSummary}</a>
                
                <c:if test="${submissionsWithOutdatedTestResults.contains(thisOntimeSubmission.submissionPK)}">
                 (*)
                <c:set var="anyOutdated" value="true"/>
                </c:if>
                
                <c:if test="${submissionsWithReviews.contains(thisOntimeSubmission.submissionPK)}">
                <c:url var="reviewLink" value="/view/codeReview/index.jsp">
                    <c:param name="submissionPK" value="${thisOntimeSubmission.submissionPK}" />
                    </c:url>
                (<a href="${reviewLink}">R</a>)</c:if> 
			</c:if>
			<td><c:if
				test="${not empty thisLateSubmission}">
				<c:url var="submissionLink"
					value="${ss:instructorSubmissionViewLink(project,thisLateSubmission)}" />
				<a href="${submissionLink}">${thisLateSubmission.testSummary}</a>
                <c:if test="${submissionsWithOutdatedTestResults.contains(thisLateSubmission.submissionPK)}">
                 (*)
                <c:set var="anyOutdated" value="true"/>
                </c:if>
                
                          <c:if test="${submissionsWithReviews.contains(thisLateSubmission.submissionPK)}">
                <c:url var="reviewLink" value="/view/codeReview/index.jsp">
                    <c:param name="submissionPK" value="${thisLateSubmission.submissionPK}" />
                    </c:url>
                (<a href="${reviewLink}">R</a>)</c:if> 
    
			</c:if>
			<td><c:if
				test="${not empty thisVLateSubmission}">
				<c:url var="submissionLink"
					value="${ss:instructorSubmissionViewLink(project,thisVLateSubmission)}" />
				<a href="${submissionLink}">${thisVLateSubmission.testSummary}</a>
                <c:if test="${submissionsWithOutdatedTestResults.contains(thisVLateSubmission.submissionPK)}">
                 (*)
                <c:set var="anyOutdated" value="true"/>
                </c:if>
                
                          <c:if test="${submissionsWithReviews.contains(thisVLateSubmission.submissionPK)}">
                <c:url var="reviewLink" value="/view/codeReview/index.jsp">
                    <c:param name="submissionPK" value="${thisVLateSubmission.submissionPK}" />
                    </c:url>
                (<a href="${reviewLink}">R</a>)</c:if> 
    
			</c:if>
			<td><c:url var="grantExtensionLink"
				value="/view/instructor/grantExtension.jsp">
				<c:param name="studentRegistrationPK"
					value="${studentRegistration.studentRegistrationPK}" />
				<c:param name="projectPK" value="${project.projectPK}" />
			</c:url> <a href="${grantExtensionLink}">
			${studentSubmitStatusMap[studentRegistration.studentRegistrationPK].extension}
			</a></td>
		</tr>
	</c:forEach>
</table>
<c:if test="${anyOutdated}">
<p>(*) - not tested with the current test setup
</p>
</c:if>

<ss:studentsWithoutSubmissionsTable />

<ss:footer />
</body>
</html>

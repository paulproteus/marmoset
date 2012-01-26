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
<ss:head
	title="All submissions for ${course.courseName} project ${project.projectNumber}" />

<body>

 <script type="text/javascript">
                    function toggle(item) {
                        obj = document.getElementById(item);
                        if (obj.style.display == "none") {
                            obj.style.display = "block";
                        } else {
                            obj.style.display = "none";
                        }
                    }
                </script>
                
<ss:header />
<ss:breadCrumb />

<ss:projectTitle />

<c:choose>
<c:when test="${!project.pair}">
</c:when>
<c:when test="${projectPartner != null}">
<p>Partnered with
	<c:out value="${projectPartner.fullname}"/>)
	</p>

</c:when>
<c:otherwise>
<c:url var="choosePartnerLink"
			value="/action/SelectPartner">
		</c:url>

<form method="POST" action="${choosePartnerLink}">
<input type="hidden" name="projectPK" value="${project.projectPK}"/>
<p>Select partner:
<select name="partnerPK">
<c:forEach var="partner" items="${potentialPartners}">
<option value="${partner.studentRegistrationPK}"><c:out value="${partner.fullname}"/></option>
</c:forEach>
</select>
<input type="submit" value="Choose partner" />
</p>
</form>
</c:otherwise>
</c:choose>

<ss:codeReviews title="Code reviews"/>

<c:if test="${!project.tested}">
		<p>Project is upload only</p>
		</c:if>
<h2>Submissions</h2>
<c:set var="testCols" value="0" />

<table>
	<tr>
		<th rowspan="2">#</th>
		<th rowspan="2">submitted</th>
		<%--
			Only display anything about automated testing if this project supports
			automated testing.  Many people use the server simply to store submissions.
		--%>
		<c:if test="${project.tested}">
		    <c:set var="testCols" value="1" />
			<c:if test="${testSetup.valuePublicTests > 0}">
				<th rowspan="2" >public tests score
				<c:set var="testCols" value="2" />
			</c:if>

			<c:if test="${testSetup.valueReleaseTests > 0}">
				<th colspan="2">release tests</th>
				<c:set var="testCols" value="${2+testCols}" />
			</c:if>
			<c:if test="${testProperties.language=='java' or testProperties.language=='Java'}">
			<th rowspan="2">FindBugs warnings</th>
			<c:set var="testCols" value="${1+testCols}" />
			</c:if>

			<th rowspan="2"># inconsistent<br>
			background<br>
			retests</th>
		</c:if>
		<th colspan=2>submission</th>
		      <c:if test="${project.tested}">
		      ${ss:formattedTestHeaderTop(canonicalTestOutcomeCollection, false)}
		      </c:if>

	</tr>
	<tr>
	<c:if test="${project.tested && testSetup.valueReleaseTests > 0}">
			<th>score</th>
			<th>when</th>
				</c:if>
	<th>Details</th><th>Download</th>
	 <c:if test="${project.tested}">
	 ${ss:formattedTestHeader(canonicalTestOutcomeCollection, false)}
	 </c:if>
	</tr>

	<c:forEach var="submission" items="${submissionList}"
		varStatus="counter">
		<c:url var="submissionLink"
			value="${ss:submissionViewLink(project,submission)}" />
			<c:set var="testOutcomes" value="${testOutcomesMap[submission.submissionPK]}"/>

		<c:url var="submissionAllTestsLink"
			value="/view/submissionAllTests.jsp">
			<c:param name="submissionPK" value="${submission.submissionPK}" />
		</c:url>

		<tr class="r${counter.index % 2}">
			<td>${submission.submissionNumber}</td>

			<td><a href="${submissionLink}"><fmt:formatDate
				value="${submission.submissionTimestamp}"
				pattern="E',' dd MMM 'at' hh:mm a" /></a></td>
			<c:choose>
				<c:when test="${!project.tested}">
				</c:when>

				<c:when
					test="${submission.buildStatus == 'COMPLETE' && submission.compileSuccessful}">

					<c:if test="${testSetup.valuePublicTests > 0}">
				<td>${submission.valuePublicTestsPassed} / ${testSetup.valuePublicTests}</td>
				</c:if>

					<c:choose>
						<c:when test="${testSetup.valueReleaseTests == 0}">
						</c:when>

						<c:when test="${submission.releaseTestingRequested
						         || ss:showPostDeadlineDetails(project, studentSubmitStatus)}">
							<td>${submission.valueReleaseTestsPassed} /
							${testOutcomes.valueReleaseTests}</td>
							<td><fmt:formatDate value="${submission.releaseRequest}"
								pattern="E 'at' hh:mm a" /></td>
						</c:when>
						<c:otherwise>
							<td colspan="2">?</td>
						</c:otherwise>
					</c:choose>
					<c:if test="${testProperties.language=='java' or testProperties.language=='Java'}">
					<td><c:if test="${submission.numFindBugsWarnings > 0}">
							${submission.numFindBugsWarnings}</c:if>
							</td></c:if>
					<td><c:if
						test="${submission.numFailedBackgroundRetests > 0 && (publicInconsistencies[submission.submissionPK] || releaseInconsistencies[submission.submissionPK]) && !secretInconsistencies[submission.submissionPK]}">
						<a href="${submissionAllTestsLink}">
						${submission.numFailedBackgroundRetests} </a>
					</c:if></td>
				</c:when>

				<c:when test="${submission.buildStatus == 'PENDING' }">
					<td colspan="${testCols}">testing started at <fmt:formatDate
						value="${submission.buildRequestTimestamp}"
						pattern="dd MMM hh:mm a" /></td>

				</c:when>
				<c:when test="${submission.buildStatus == 'COMPLETE'}">
					<td colspan="${testCols}">did not compile</td>
				</c:when>
				<c:when test="${submission.buildStatus == 'BROKEN'}">
						<td colspan="${testCols}">marked as broken; attempts to build failed</td>
					</c:when>
				<c:otherwise>
					<td colspan="${testCols}">not tested yet</td>
				</c:otherwise>

			</c:choose>
			<td><a href="${submissionLink}">view</a></td>

			<td><c:url var="downloadLink" value="/data/DownloadSubmission">
				<c:param name="submissionPK" value="${submission.submissionPK}" />
			</c:url> <a href="${downloadLink}">download</a></td>
			
			<c:if test="${project.tested}">
			${ss:formattedStudentTestResults(canonicalTestOutcomeCollection,testOutcomes, submission.releaseTestingRequested
                                 || ss:showPostDeadlineDetails(project, studentSubmitStatus))}
			</c:if>
		</tr>
	</c:forEach>
</table>

    <c:url var="submitProjectLink"
                    value="/view/submitProject.jsp">
                    <c:param name="projectPK" value="${project.projectPK}" />
                </c:url>

    <p>
        <a href="${submitProjectLink}"> web submission </a>
    </p>
    <c:if test="${project.tested}">
        <ss:inconsistentBackgroundRetestDescription />
    </c:if>


    <p>
        <a href="javascript:toggle('submitFiles')" title="Click to toggle display of contents" id="submitFilesSecton">
            Files for automatic submission tools (toggle)</a>
    </p>


    <div id="submitFiles" style="display: none">
        <p>These files are used by tools that submit projects (such as the Eclipse course project manager and the
            command line submission tool). Normally, the .submit file is provided by the instructor, and the .submitUser
            file is created the first time a project is submitted, after the tool authentications you. But direct links
            are provided here as an alternative way to obtain these files.
        <p>
            Both of these files should be in the top level directory for the project.

            <c:url var="dotSubmitLink" value="/data/GetDotSubmitFile">
                <c:param name="projectPK" value="${project.projectPK}" />
            </c:url>
        <p>
            <a href="${dotSubmitLink}">Get .submit file (identifies project)</a>
            <c:url var="dotSubmitLink" value="/data/GetDotSubmitUserFile">
                <c:param name="projectPK" value="${project.projectPK}" />
            </c:url>
        <p>
            <a href="${dotSubmitLink}">Get .submitUser file (identifies a submission as being from <c:out
                    value="${studentRegistration.fullname}" />; do not share with other students)
            </a>
        </p>
    </div>

    <ss:footer />
</body>
</html>

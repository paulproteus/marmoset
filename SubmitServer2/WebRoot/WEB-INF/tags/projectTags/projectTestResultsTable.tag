<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<h1>Results for each test case for each students' most recent submission</h1>

		<c:url var="sortByTime" value="/view/instructor/projectTestResults.jsp">
				<c:param name="projectPK" value="${project.projectPK}" />
				<c:param name="sortKey" value="time" />
			</c:url>
<c:url var="sortByName" value="/view/instructor/projectTestResults.jsp">
				<c:param name="projectPK" value="${project.projectPK}" />
			</c:url>
<c:url var="sortByAcct" value="/view/instructor/projectTestResults.jsp">
				<c:param name="projectPK" value="${project.projectPK}" />
				<c:param name="sortKey" value="account" />
			</c:url>
			<c:url var="sortByScore" value="/view/instructor/projectTestResults.jsp">
				<c:param name="projectPK" value="${project.projectPK}" />
				<c:param name="sortKey" value="score" />
			</c:url>
<c:set var="inconsistentResults"
        value="${fn:length(failedBackgroundRetestSubmissionList)}" />
    
<c:set var="cols" value="7" />
<c:set var="showCodeCoverage"
    value="${testProperties.performCodeCoverage and instructorCapability and ! empty coverageScore}" />
<c:if test="${testProperties.language=='java'}">
    <c:set var="cols" value="9" />
</c:if>
<c:if test="${showCodeCoverage}">
    <c:set var="cols" value="${1+cols}" />
</c:if>
<c:if test="${inconsistentResults == 0}">
   <c:set var="cols" value="${cols-1}" />
   </c:if>
<c:set var="anyOutdated" value="false"/>
<p>
		<table>

            
		${ss:formattedColumnHeaders(cols, canonicalTestOutcomeCollection)}

		<tr>
		<th class="number" rowspan="2"><a title="position in list">#</a></th>
		<th rowspan="2"><a href="${sortByName}" title="sort by name">Name</a></th>
		<th rowspan="2"><a href="${sortByAcct}" title="sort by account">Acct</a></th>
		<th rowspan="2" class="number"><a title="# of submissions for this project">#<br>subs</a></th>
		<th rowspan="2"><a href="${sortByTime}" title="sort by time of last submission">submitted at</a></th>
		<th class="number" rowspan="2"><a href="${sortByScore}" title="sort by score">Score</a>
		 <c:if test="${showCodeCoverage}">
         <th rowspan="2">Code<br>coverage<br>score</c:if>
        <c:if test="${inconsistentResults > 0}">
         <th rowspan="2"># inconsistent<br>background<br>retests</th>
         </c:if>
		<c:if test="${testProperties.language=='java'}">
		<th rowspan="2"># FindBugs<br>warnings</th>
		<th rowspan="2"># student<br>written tests</th>
		</c:if>
			${ss:formattedTestHeaderTop(canonicalTestOutcomeCollection, true)}

			</tr>
			<tr>
				${ss:formattedTestHeader(canonicalTestOutcomeCollection, true)}
			</tr>
			<c:forEach var="studentRegistration"
				items="${studentRegistrationSet}" varStatus="counter">
                <c:set var="submission" value="${lastSubmission[studentRegistration.studentRegistrationPK]}"/>
				<tr class="r${counter.index % 2}">
				<td class="number">${1+counter.index}
					<td class="description"><c:if
						test="${studentRegistration.instructorLevel > 0}">* </c:if>
                        <c:out value="${studentRegistration.getFullname}"/>
					</td>
					<td class="description"><c:url var="studentProjectLink"
						value="/view/instructor/studentProject.jsp">
						<c:param name="projectPK" value="${project.projectPK}" />
						<c:param name="studentPK" value="${studentRegistration.studentPK}" />
					</c:url> <a href="${studentProjectLink}" title="view all submissions by ${studentRegistration.classAccount}">
					${studentRegistration.classAccount} </a></td>
					<td class="number">${studentSubmitStatusMap[studentRegistration.studentRegistrationPK].numberSubmissions}
					<td><c:url var="submissionLink"
						value="/view/instructor/submission.jsp">
						<c:param name="submissionPK"
							value="${submission.submissionPK}" />
					</c:url> <a href="${submissionLink}" title="view this submission"> <fmt:formatDate
						value="${submission.submissionTimestamp}"
						pattern="E',' dd MMM 'at' hh:mm a" /></a></td>

                    <td>${submission.valuePassedOverall} 
                <c:if test="${submissionsWithOutdatedTestResults.contains(submission.submissionPK)}">
                 (*)
                <c:set var="anyOutdated" value="true"/>
                </c:if>
              
                    <c:if
                            test="${submissionsWithReviews.contains(submission.submissionPK)}">
                            <c:url var="reviewLink" value="/view/codeReview/index.jsp">
                                <c:param name="submissionPK" value="${submission.submissionPK}" />
                            </c:url>
                        (<a href="${reviewLink}">R</a>)</c:if></td>
        
         <c:if test="${showCodeCoverage}">
         <td class="number">${coverageScore[submission.submissionPK]}
         </c:if>
         
                 <c:if test="${inconsistentResults > 0}">
                    <td >
						<c:if test="${submission.numFailedBackgroundRetests > 0 }">
						<c:url var="submissionAllTestsLink" value="/view/instructor/submissionAllTests.jsp">
							<c:param name="submissionPK" value="${submission.submissionPK}"/>
						</c:url>
						
						<a href="${submissionAllTestsLink}">
						${submission.numFailedBackgroundRetests}
						</a>
						</c:if>
						</td>
                        </c:if>

						<c:if test="${testProperties.language=='java'}">
							<td ><c:if test="${submission.numFindBugsWarnings > 0 }">
							${submission.numFindBugsWarnings}
							</c:if></td>

							<td ><c:if test="${lastOutcomeCollection[studentRegistration.studentRegistrationPK].numStudentWrittenTests > 0 }">
							${lastOutcomeCollection[studentRegistration.studentRegistrationPK].numStudentWrittenTests}
							</c:if>
							</td>
						</c:if>

				${ss:formattedTestResults(canonicalTestOutcomeCollection,lastOutcomeCollection[studentRegistration.studentRegistrationPK])}


				</tr>
			</c:forEach>

		</table>
        
<c:if test="${anyOutdated}">
<p>(*) - not tested with the current test setup
</p>
</c:if>
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
	title="Submission ${submission.submissionNumber} for project ${project.projectNumber}" />
<body>
<ss:header />
<ss:breadCrumb />

<ss:projectTitle />


<c:url var="viewSourceLink" value="/view/sourceCode.jsp"/>

<c:url var="viewSubmissionSourceLink" value="/view/allSourceCode.jsp">
	<c:param name="submissionPK" value="${submission.submissionPK}" />
</c:url>


<h2>${studentRegistration.fullname}</h2>
<h2>Submission #${submission.submissionNumber}, submitted at <fmt:formatDate
	value="${submission.submissionTimestamp}"
	pattern="E',' dd MMM 'at' hh:mm a" /></h2>
<c:if
	test="${submission.currentTestRunPK != testRun.testRunPK}">
	<p>Results from previous testing against version
	${testSetup.version} of the test setup
</c:if>

<p><a href="${viewSubmissionSourceLink}">Source</a>

    <p>
        build status: ${submission.buildStatus}
        <c:if test="${submission.buildStatus == 'PENDING' or submission.buildStatus == 'RETEST'}">
    (being tested)
    </c:if>
        <c:if test="${submission.numPendingBuildRequests  > 0}">
            <p>
                Submission has ${submission.numPendingBuildRequests} outstanding build requests, most recent at
                <fmt:formatDate value="${submission.buildRequestTimestamp}" pattern="dd MMM, hh:mm a" />
        </c:if>
        <c:if test="${testRun != null && project.testSetupPK != testRun.testSetupPK}">
            <p>This is not the current test setup.</p>
        </c:if>

        <c:choose>

            <c:when test="${testRun == null}">
                <p>No test results available
                    <c:if test="${empty testSetup}">
                       (No test setup)
                    </c:if>
                

            </c:when>

            <c:when test="${!testOutcomeCollection.compileSuccessful}">
		<h2>Compile/Build unsuccessful</h2>
		<p>

		<pre><c:out
			value="${testOutcomeCollection.buildOutcome.longTestResult}" /></pre>
	</c:when>
	<c:otherwise>
	<ss:findBugsTable />
	<c:choose>

<c:when test="${ss:showPostDeadlineDetails(project, studentSubmitStatus)}">
	<ss:allOutcomesTable />
</c:when>

<c:when test="${not empty testOutcomeCollection.publicOutcomes ||
not empty testOutcomeCollection.releaseOutcomes}">
		<h2>Test Results</h2>
		<p>
		<table class="testResults">

			<c:set var="numDisplayed" value="0" />
			<tr>
				<th>type</th>
				<th>test #</th>
				<th>outcome</th>
				<c:if test="${testProperties.language=='java' and testProperties.performCodeCoverage}">

				</c:if>
				<th>points</th>
				<th>name</th>
				<th>short result</th>
				<th>long result</th>
			</tr>
			<c:forEach var="test" items="${testOutcomeCollection.publicOutcomes}">

				<tr class="r${numDisplayed % 2}">
					<c:set var="numDisplayed" value="${numDisplayed + 1}" />
					<td>${test.testType}</td>
					<td>${test.testNumber}</td>
					<td>${test.studentOutcome}</td>
					<td>${test.pointValue}</td>

					<td>${test.shortTestName}</td>
					<td class="description">
						<c:out value="${test.shortTestResult}" />
					</td>
					<td class="description">
					<c:choose>
					<c:when test="${testProperties.performCodeCoverage and testProperties.language=='java'}">
						<c:out value="${ss:hotlink(test, viewSourceLink)}" />
					</c:when>
					<c:otherwise>
						<pre><c:out	value="${test.longTestResult}" /></pre>
					</c:otherwise>
					</c:choose>
					</td>
				</tr>
			</c:forEach>

<%--
 or project.releasePolicy == 'anytime'
--%>
			<c:if
				test="${submission.releaseTestingRequested && submission.releaseEligible}">

				<c:set var="failedTests" value="0" />

				<c:forEach var="release"
					items="${testOutcomeCollection.releaseOutcomes}">

					<tr class="r${numDisplayed % 2}">

					<c:choose>
					<c:when test="${release.passed}">
						<td>${release.testType}</td>
						<td>${release.testNumber}</td>
						<td>${release.studentOutcome}</td>
						<td>${release.pointValue}</td>
						<td colspan="3"></td>
					</c:when>
					<c:when test="${release.failed}">
						<c:set var="numDisplayed" value="${numDisplayed + 1}" />
						<c:set var="failedTests" value="${failedTests + 1}" />
						<c:choose>
							<c:when test="${failedTests <= project.numReleaseTestsRevealed}">
								<td>${release.testType}</td>
								<td>${release.testNumber}</td>
								<td>${release.studentOutcome}</td>
								<td>${release.pointValue}</td>
								<td>${release.shortTestName}</td>
								<td></td>
								<c:choose>
									<c:when test="${project.stackTracePolicy == 'full_stack_trace'}">
										<td><c:out value="${ss:hotlink(release, viewSourceLink)}" escapeXml="false"/></td>
									</c:when>
									<c:when test="${release.error and project.stackTracePolicy != 'test_name_only'}">
										<c:if test="${project.stackTracePolicy == 'exception_location'}">
											<td><c:out value="${ss:exceptionLocation(release, viewSourceLink)}" escapeXml="false"/></td>
										</c:if>
										<c:if test="${project.stackTracePolicy == 'restricted_exception_location'}">
											<c:choose>
												<c:when test="${ss:isApproximatelyCovered(testOutcomeCollection,release)}">
												<td><c:out value="${ss:exceptionLocation(release, viewSourceLink)}" escapeXml="false"/></td>
										</c:when>
												<c:otherwise>
												<td>This test generates an exception in your code,
													but no public or student-written tests cover the line of
													code that throws the exception.  Write more/better test cases and
													we will reveal more information about why your code fails this test case.
												</td>
												</c:otherwise>
											</c:choose>
										</c:if>

										<c:if test="${project.stackTracePolicy == 'full_stack_trace'}">
											<td><c:out value="${ss:hotlink(release, viewSourceLink)}" escapeXml="false"/></td>
										</c:if>
									</c:when>
									<c:otherwise>
										<td></td>
									</c:otherwise>
								</c:choose>
							</c:when>
							<c:otherwise>
								<td>${release.testType}</td>
								<td>?</td>
								<td>${release.studentOutcome}</td>
								<td>?</td>
								<td>?</td>
								<td></td>
								<td></td>
							</c:otherwise>
						</c:choose>
					</c:when>
					</c:choose>
					</tr>
				</c:forEach>
			</c:if>
			<%-- end if release test requested --%>
		</table>

		<p>You received ${testOutcomeCollection.valuePublicTestsPassed}/${
		testOutcomeCollection.valuePublicTests} points for public test cases.


        <c:if test="${not empty testOutcomeCollection.releaseOutcomes}">

		<c:choose>
		    <c:when
				test="${submission.releaseTestingRequested && submission.releaseEligible}">
				<p>
				<p>You received
				${testOutcomeCollection.valueReleaseTestsPassed}/${testOutcomeCollection.valueReleaseTests}
				points for release tests.
			</c:when>
			<%-- end if !release test requested --%>

				<c:when
					test="${submission.releaseEligible && submission.currentTestRunPK == testRun.testRunPK}">
					<p>This submission is eligible for release testing.
					<c:choose>
					<c:when test="${releaseInformation.releaseRequestOK}">
						<c:url var="releaseRequestLink"
							value="/view/confirmReleaseRequest.jsp">
							<c:param name="submissionPK" value="${submission.submissionPK}" />
						</c:url>
						<p>
						<h3>
						<a href="${releaseRequestLink}"> Click here to release test this
						submission </a>
						</h3>
					</c:when>
					<c:otherwise>
						<p>
						You may not release test at this time because
						you do not have sufficient release tokens.
						</p>
					</c:otherwise>
					</c:choose>
				</c:when>
				<c:otherwise>
					<c:if test="${releaseInformation.afterPublic}">
						<p>
						You may not release test at this time.  Note that
						this project is set to allow release testing only
						for submissions that pass all public tests.
						</p>
					</c:if>
				</c:otherwise>
		</c:choose>
        <ss:releaseTokens/>
		</c:if>
		</c:when>
		<c:otherwise><p>Submission compiled</c:otherwise>
	</c:choose>


	<ss:studentWrittenTestOutcomesTable />



<ss:uncoveredMethodsTable />


<c:if test="${testProperties.language == 'java' and testProperties.performCodeCoverage}">
	<h2>
		Code Coverage Results
	</h2>
	<table>
		<tr>
		<th>Test type</th>
		<th>statements</th>
		<th>conditionals</th>
		<th>methods</th>
	    </tr>
    	<c:if test="${studentCoverageStats != null}">
	    	<tr>
<c:url var="studentLink" value="/view/sourceFiles.jsp">
	<c:param name="testType" value="student"/>
	<c:param name="testNumber" value="all"/>
	<c:param name="testRunPK" value="${testRun.testRunPK}"/>
</c:url>
			<td> <a href="${studentLink}">  Student tests </a></td>
			${studentCoverageStats.HTMLTableRow}
	    	</tr>
	    </c:if>
		<c:if test="${publicCoverageStats != null}">
	    	<tr>
<c:url var="publicLink" value="/view/sourceFiles.jsp">
	<c:param name="testType" value="public"/>
	<c:param name="testNumber" value="all"/>
	<c:param name="testRunPK" value="${testRun.testRunPK}"/>
</c:url>
			<td> <a href="${publicLink}"> Public tests </a> </td>
			${publicCoverageStats.HTMLTableRow}
    		</tr>
    	</c:if>
		<c:if test="${publicAndStudentCoverageStats != null}">
	    	<tr>
<c:url var="publicAndStudentLink" value="/view/sourceFiles.jsp">
	<c:param name="testType" value="public-student"/>
	<c:param name="testNumber" value="all"/>
	<c:param name="testRunPK" value="${testRun.testRunPK}"/>
</c:url>
    		<td> <a href="${publicAndStudentLink}"> Public/Student tests </a> </td>
			${publicAndStudentCoverageStats.HTMLTableRow}
	    	</tr>
	    </c:if>
	    <%--
	    TODO: Code covered only by release/secret tests!
	    TODO: Methods covered/uncovered
	    --%>
	</table>
</c:if>






	</c:otherwise>

</c:choose> <c:if test="${testRunList != null}">
	<c:url value="/view/submissionAllTests.jsp"
		var="submissionAllTestsLink">
		<c:param name="submissionPK" value="${submission.submissionPK}" />
	</c:url>
	<p><a href="${submissionAllTestsLink}"> See all test results for this
	submission </a>

<ss:submissionDetails />

</c:if>
<ss:footer />
</body>
</html>

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

<c:url var="viewSourceLink" value="../allSourceCode.jsp">
	<c:param name="submissionPK" value="${submission.submissionPK}" />
</c:url>
    <c:url var="gwtCodeReviewLink" value="/view/codeReview/index.jsp">
	<c:param name="submissionPK" value="${submission.submissionPK}" />
   </c:url>

<!DOCTYPE HTML>
<html>
<ss:head
	title="Instructor view of submission ${submission.submissionPK}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<ss:projectTitle />
<ss:projectMenu />

<ss:studentPicture />
<h2>
${student.fullname},
Submission # ${submission.submissionNumber}, <fmt:formatDate
	value="${submission.submissionTimestamp}" pattern="dd MMM, hh:mm a" /></h2>


	<p>
	<ss:studentEmail/>
	&nbsp;|&nbsp;
	<a href="${viewSourceLink}">Source</a>

&nbsp;|&nbsp;
<a href="${gwtCodeReviewLink}">code review</a>

&nbsp;|&nbsp;

	<c:url var="downloadLink" value="/data/DownloadSubmission">
		<c:param name="submissionPK" value="${submission.submissionPK}" />
	</c:url>
	<a href="${downloadLink}"> Download</a>

&nbsp;|&nbsp;

	<c:url var="studentViewLink" value="/view/submission.jsp">
		<c:param name="submissionPK" value="${submission.submissionPK}" />
	</c:url>
	<a href="${studentViewLink}">Student view</a>
&nbsp;|&nbsp;
	<a href="${grantExtensionLink}"> Grant  extension </a>
	<c:if test="${studentSubmitStatus.extension != 0}">
	(Currently: ${studentSubmitStatus.extension} hours)
	</c:if>
&nbsp;|&nbsp;

<c:if test="${testRunList != null}">
    <c:url value="submissionAllTests.jsp"
        var="submissionAllTestsLink">
        <c:param name="submissionPK" value="${submission.submissionPK}" />
    </c:url>
    <a href="${submissionAllTestsLink}">all test runs</a>&nbsp;|&nbsp;
</c:if>


<c:choose>
	<c:when test="${submission.buildStatus == 'PENDING' or submission.buildStatus == 'RETEST'}">
	being retested
	</c:when>
	<c:otherwise>

	<c:url var="reTestSubmissionLink" value="/view/instructor/confirmChangeSubmissionBuildStatus.jsp">
		<c:param name="submissionPK" value="${submission.submissionPK}" />
		<c:param name="title" value="Are you sure you want to mark submission ${submission.submissionPK} for retest?"/>
		<c:param name="buildStatus" value="retest"/>
	</c:url>
	<a href="${reTestSubmissionLink}">Retest</a>
	</c:otherwise>
</c:choose>

<c:choose>
	<c:when test="${submission.buildStatus != 'BROKEN'}">
	&nbsp;|&nbsp;
	<c:url var="markBrokenLink" value="/view/instructor/confirmChangeSubmissionBuildStatus.jsp">
		<c:param name="submissionPK" value="${submission.submissionPK}" />
		<c:param name="title" value="Are you sure you want to mark submission ${submission.submissionPK} broken?"/>
		<c:param name="subtitle" value="Broken submissions are not tested unless you specifically mark them for retest."/>
        <c:param name="buildStatus" value="broken"/>
	</c:url>
	<a href="${markBrokenLink}">mark broken</a>
	</c:when>
	<c:otherwise>
	<p>
	This submission has been marked broken.  It will never be tested by a buildServer unless it
	is specifically marked for retest by the 'retest' option.
    <c:if test="${submission.numPendingBuildRequests > 3}">
This is likely due to the ${submission.numPendingBuildRequests}  pending build requests. 
If a submission is sent out for testing multiple times and the build server does not report a
result, the submission is flagged as broken.
</c:if>
</p>
	</c:otherwise>
</c:choose>


<c:if test="${submission.numPendingBuildRequests > 0}">
<p>Submission has ${submission.numPendingBuildRequests} outstanding build requests,
most recent at  <fmt:formatDate
    value="${submission.buildRequestTimestamp}" pattern="dd MMM, hh:mm a" />
</c:if>

 <c:if test="${testRun != null && project.testSetupPK != testRun.testSetupPK}">
 <p>This is not the current test setup.
 </p></c:if>
 
<c:choose>
<c:when test="${requestScope.testRun == null}">
	<h2>No test results available</h2>
	<ul>
	<c:if test="${empty testSetup}">
	<li>No test setup</li>
	</c:if>
	<li> build status: ${submission.buildStatus}
	   <c:if test="${submission.buildStatus == 'PENDING' or submission.buildStatus == 'RETEST'}">
    (being tested)
    </c:if>
    <c:if test="${submission.numPendingBuildRequests  > 0}">
    (${submission.numPendingBuildRequests} pending build requests)
    </c:if>
    
    </li>
    </ul>
</c:when>

	<c:when test="${!testOutcomeCollection.compileSuccessful}">
		<h2>Compile/Build unsuccessful</h2>
		<p>
		<pre>
			<c:out value="${testOutcomeCollection.buildOutcome.longTestResult}" />
		</pre>
	</c:when>
	<c:otherwise>

<ss:findBugsTable />

<ul>
<c:if test="${testOutcomeCollection.valuePublicTests > 0}">
<li>${testOutcomeCollection.valuePublicTestsPassed}/${
		testOutcomeCollection.valuePublicTests} points for public test cases.
		</c:if>
<c:if test="${testOutcomeCollection.valueReleaseTests > 0}">
<li>${testOutcomeCollection.valueReleaseTestsPassed}/${
		testOutcomeCollection.valueReleaseTests} points for release test cases.
		</c:if>
<c:if test="${testOutcomeCollection.valueSecretTests > 0}">
<li>${testOutcomeCollection.valueSecretTestsPassed}/${
		testOutcomeCollection.valueSecretTests} points for secret test cases.
		</c:if>
</ul>

<ss:allOutcomesTable />


<ss:studentWrittenTestOutcomesTable />

	<c:if test="${hasCodeCoverageResults}">
	<h2>
		Code Coverage Results
	</h2>
	<ss:uncoveredMethodsTable />
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
	    <c:if test="${cardinalCoverageStats != null}">
	    	<tr>
<c:url var="cardinalLink" value="/view/sourceFiles.jsp">
	<c:param name="testType" value="cardinal"/>
	<c:param name="testNumber" value="all"/>
	<c:param name="testRunPK" value="${testRun.testRunPK}"/>
</c:url>
    		<td> <a href="${cardinalLink}"> Public/Release/Secret tests </a> </td>
			${cardinalCoverageStats.HTMLTableRow}
	    	</tr>
	    </c:if>
	    <c:if test="${releaseCoverageStats.HTMLTableRow!=null}">
	    	<tr>
<c:url var="releaseLink" value="/view/sourceFiles.jsp">
	<c:param name="testType" value="release"/>
	<c:param name="testNumber" value="all"/>
	<c:param name="testRunPK" value="${testRun.testRunPK}"/>
</c:url>
    		<td> <a href="${releaseLink}"> Release tests </a> </td>
			${releaseCoverageStats.HTMLTableRow}
	    	</tr>
	    </c:if>
	    <c:if test="${releaseUniqueStats != null}">
	    	<tr>
<c:url var="releaseUniqueLink" value="/view/sourceFiles.jsp">
	<c:param name="testType" value="release-unique"/>
	<c:param name="testNumber" value="all"/>
	<c:param name="testRunPK" value="${testRun.testRunPK}"/>
</c:url>
	    	<td> <a href="${releaseUniqueLink}" title="covered by a release test but not by any public or student test"> Unique Release  </a><br>

	    	 </td>
	    	${releaseUniqueStats.HTMLTableRow}
	    	</tr>
	    </c:if>
		<c:if test="${intersectionCoverageStats != null}">
	    	<tr>
<c:url var="intersectionLink" value="/view/sourceFiles.jsp">
	<c:param name="testType" value="intersection"/>
	<c:param name="testNumber" value="all"/>
	<c:param name="testRunPK" value="${testRun.testRunPK}"/>
</c:url>
    		<td> <b> (Public U Release) <br>intersect<br> (Public U Student) </b> </td>
			${intersectionCoverageStats.HTMLTableRow}
	    	</tr>
	    </c:if>
	</table>
</c:if>




</c:otherwise>
</c:choose>


<c:if test="${testRunList != null}">
	<c:url value="submissionAllTests.jsp"
		var="submissionAllTestsLink">
		<c:param name="submissionPK" value="${submission.submissionPK}" />
	</c:url>
	<p><a href="${submissionAllTestsLink}"> See other test results for this
	submission </a>
</c:if>

<ss:submissionDetails />


<ss:footer />
</body>
</html>

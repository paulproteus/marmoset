<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<h3>
    <a href="javascript:toggle('submissionDetails')" title="Click to toggle display of submission details">Submission
        details</a>
</h3>

<div id="submissionDetails" style="display: none">
    <p>
        submissionPk = <c:out value="${submission.submissionPK}"/>, client was
        <c:out value="${submission.submitClient}"/>,
        <c:if test="${testRun != null}">
Tested against test-setup #<c:out value="${testRun.testSetupPK}" />,  
on <c:out value="${testRun.testTimestamp}" /> by 
<c:out value="${testRun.testMachine}" />
 testRun&nbsp;#<c:out value="${testRun.testRunPK}" />.
 <c:if test="${testRun.testDurationMinutes > 0}">
 Required <c:out value="${testRun.testDurationMinutes}" />
 minutes to build and test submission.
 </c:if>

            <c:if test="${project.testSetupPK != testRun.testSetupPK}">
                <p>This is not the current test setup.</p>
            </c:if>
            <c:if test="${!peerReview && testRunList != null}">
                <c:url value="/view/submissionAllTests.jsp" var="submissionAllTestsLink">
                    <c:param name="submissionPK" value="${submission.submissionPK}" />
                </c:url>
                <p>
                    <a href="${submissionAllTestsLink}"> See all test results for this submission </a>
                </p>
            </c:if>
        </c:if>
</div>
<div id="submissionDetails2" style="display: none">
Required <c:out value="${testRun.testDurationMillis}" />
 milliseconds to build and test submission.
</div>
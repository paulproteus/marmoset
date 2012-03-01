<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<h3>
    <a href="javascript:toggle('submissionDetails')" title="Click to toggle display of submission details">Submission
        details</a>
</h3>
<div id="submissionDetails" style="display: none">
    <p>submissionPk = ${submission.submissionPK}, client was ${submission.submitClient},
        <c:if test="${testRun != null}">
Tested against test-setup #${testRun.testSetupPK},  
on ${testRun.testTimestamp} by ${testRun.testMachine}
 testRun&nbsp;#${testRun.testRunPK}
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
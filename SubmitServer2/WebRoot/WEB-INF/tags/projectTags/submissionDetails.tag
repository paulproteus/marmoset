<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<h3>Submission details</h3>
<p> submissionPk = ${submission.submissionPK}, client was ${submission.submitClient},
<c:if test="${testRun != null}">
Tested against test-setup #${testRun.testSetupPK},  
on ${testRun.testTimestamp} by ${testRun.testMachine}
 testRun&nbsp;#${testRun.testRunPK}
  <c:if test="${project.testSetupPK != testRun.testSetupPK}">
 <p>This is not the current test setup.
 </p></c:if>
</c:if>
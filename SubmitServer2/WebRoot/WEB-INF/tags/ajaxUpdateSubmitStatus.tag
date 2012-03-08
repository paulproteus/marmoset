<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:url var="submissionStatusLink" value="/view/submissionStatus.jsp">
	<c:param name="submissionPK" value="${submission.submissionPK}" />
</c:url>
<script>
$(document).ready(function() {
	function update(attempts,count) {
		if (count > 0) {
			$("#refresh").html("(will automatically refresh in " + count + " seconds)");
			delayedUpdate(attempts, count-1);
		} else 
		$.ajax({url: "${submissionStatusLink}",
			dataType: "json",
			success: function(status) {
				$("#buildStatus").html(status.buildStatus);
				console.log(status.numPendingBuildRequests);
				console.log(status.buildRequestTimestamp);
				if (status.numPendingBuildRequests ==  0)
					$("#pendingBuildRequests").html("");
				else if (status.numPendingBuildRequests ==  1)
					$("#pendingBuildRequests").html("<br>A build server started work to build and test the submission. ");
				else 
					$("#pendingBuildRequests").html("<br>There are " + status.numPendingBuildRequests
							+" outstanding build attempts. ");
				if (status.buildRequestTimestamp)
					$("#lastBuildRequest").html("Most recent build attempt started at " + status.buildRequestTimestamp +".");
				else
					$("#lastBuildRequest").html("");
				
			switch(status.buildStatus) {
			case 'NEW':
			case 'PENDING':
			case 'RETEST':
				if (attempts > 0)
					delayedUpdate(attempts-1, 15);
				else
					$("#refresh").html("(automatic refresh timed out)");
				break;
			default:
				location.reload();
			}
			}});
	}
	function delayedUpdate(attempts, count) {
		setTimeout(function() {
			update(attempts, count);
		}, 1000)
	};
	delayedUpdate(15, 15);
	
});
</script>
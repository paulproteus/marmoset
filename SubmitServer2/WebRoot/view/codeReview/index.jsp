
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<!DOCTYPE html>
<html>
<head>


<c:set var="title">Project <c:out value="${project.projectNumber}" /> submission, written by  <c:out value="${reviewDao.authorName}" /></c:set>
<c:choose>
    <c:when test="${not empty nextCodeReview}">
        <c:set var="reviewBacklinkText" value="next" />
        <c:url var="reviewBacklinkUrl" value="/view/codeReview/">
            <c:param name="codeReviewerPK">
                <c:out value="${nextCodeReview}" />
            </c:param>
        </c:url>
    </c:when>

    <c:when test="${not empty codeReviewAssignment}">
        <c:set var="reviewBacklinkText" value="assignment" />
        <c:url var="reviewBacklinkUrl" value="/view/codeReviews.jsp">
            <c:param name="codeReviewAssignmentPK" value="${codeReviewAssignment.codeReviewAssignmentPK}" />
            <c:param name="projectPK" value="${project.projectPK}" />
            <c:param name="coursePK" value="${course.coursePK}" />
        </c:url>
    </c:when>
    <c:otherwise>
        <!-- ad hoc; should be instructor -->
        <c:set var="reviewBacklinkText" value="submission" />
        <c:url var="reviewBacklinkUrl" value="/view/instructor/submission">
            <c:param name="submissionPK" value="${submission.submissionPK}" />
            <c:param name="projectPK" value="${project.projectPK}" />
            <c:param name="coursePK" value="${course.coursePK}" />
        </c:url>
    </c:otherwise>
</c:choose>

<script>
	var reviewSummary = {
		"daoKey" : '<c:out value="${reviewDaoKey}"/>',
		"title" : '${title}',
		"backlinkText" : '${reviewBacklinkText}',
		"backlinkUrl" : '${reviewBacklinkUrl}',
	};
</script>

<title>"Code review"</title>
<c:url var="gwtLink" value="/codereview/codereview.nocache.js" />
<script type="text/javascript" src="${gwtLink}"></script>
<c:url var="cssLink" value="/codereview.css" />
<link rel="stylesheet" href="${cssLink}" />
<link href='http://fonts.googleapis.com/css?family=Ubuntu+Mono' rel='stylesheet' type='text/css'>
</head>
<body>

    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1'
        style="position: absolute; width: 0; height: 0; border: 0"></iframe>


</body>
</html>

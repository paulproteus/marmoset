<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="projectMenu">
<c:url var="overviewLink" value="/view/instructor/project.jsp">
	<c:param name="projectPK" value="${project.projectPK}"/>
</c:url>
<a href="${overviewLink}">Overview</a>

<c:url var="utilitiesLink" value="/view/instructor/projectUtilities.jsp">
	<c:param name="projectPK" value="${project.projectPK}"/>
</c:url>
&nbsp;|&nbsp;
<a href="${utilitiesLink}">Utilities</a>

<c:url var="baselineLink" value="/view/instructor/uploadProjectStarterFiles.jsp">
	<c:param name="projectPK" value="${project.projectPK}"/>
</c:url>
&nbsp;|&nbsp;
<a href="${baselineLink}">Baseline</a>

<c:url var="historicalViewLink" value="/view/instructor/projectTestHistory.jsp">
	<c:param name="projectPK" value="${project.projectPK}"/>
</c:url>
&nbsp;|&nbsp;
<a href="${historicalViewLink}">History</a>

<c:url var="testResultsLink" value="/view/instructor/projectTestResults.jsp">
	<c:param name="projectPK" value="${project.projectPK}"/>
</c:url>
&nbsp;|&nbsp;
<a href="${testResultsLink}">Test details</a>

<c:url var="failedBackgroundRetestsLink" value="/view/instructor/failedBackgroundRetests.jsp">
	<c:param name="projectPK" value="${project.projectPK}"/>
</c:url>
&nbsp;|&nbsp;
<a href="${failedBackgroundRetestsLink}">Inconsistencies</a>

</div>

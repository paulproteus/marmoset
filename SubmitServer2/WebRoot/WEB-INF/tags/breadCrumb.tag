<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<div class="breadcrumb">
<div class="logout">
<p><a href="<c:url value='/authenticate/Logout'/>">Logout</a></p>
</div>
<p><span id="breadcrumbUserID">${user.loginName}:</span>

<c:if test="${user.superUser}">
 <a href="<c:url value='/view/admin/index.jsp'/>">SuperUser</a> |
</c:if>

<c:if test="${!singleCourse}">
 <a href="<c:url value='/view/index.jsp'/>"
 	title="All courses for which you are registered">All Courses</a>
			<c:if test="${course != null}">
			|
			</c:if>
</c:if>
<c:if test="${course != null}">
	<c:url var="courseLink" value="/view/course.jsp">
		<c:param name="coursePK" value="${course.coursePK}"/>
	</c:url>
	<a href="${courseLink}"
	   title="overview of ${course.courseName}">${course.courseName}</a>
<c:if test="${instructorCapability}">
	<c:url var="instructorLink" value="/view/instructor/course.jsp">
		<c:param name="coursePK" value="${course.coursePK}"/>
	</c:url>
	| <a href="${instructorLink}" title="Instructor overview of the course">Instructor view</a>
</c:if>

<c:url var="codeReviewsLink" value="/view/codeReviews.jsp">
		<c:param name="coursePK" value="${course.coursePK}"/>
	</c:url>
	| <a href="${codeReviewsLink}" title="All code reviews">code reviews</a>

<c:if test="${project != null}">
	<c:url var="projectLink" value="/view/project.jsp">
		<c:param name="projectPK" value="${project.projectPK}"/>
	</c:url>
	| <c:set var="title"><c:out value="Overview of project ${project.projectNumber}"/></c:set>
    <a href="${projectLink}"
		 title="${title}"><c:out value="${project.projectNumber}"/></a>

	<c:if test="${submission != null}">
		<c:url var="submissionLink" value="/view/submission.jsp">
			<c:param name="submissionPK" value="${submission.submissionPK}"/>
		</c:url>
		| <a href="${submissionLink}" title="details of this submission"><fmt:formatDate
			 value="${submission.submissionTimestamp}" pattern="dd MMM, hh:mm a"/></a>
	</c:if>

</c:if>

</c:if>

</div>






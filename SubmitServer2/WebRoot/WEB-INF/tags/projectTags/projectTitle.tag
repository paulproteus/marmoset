<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="sectionTitle">
<c:set var="projectURL" value="${projectLink}"/>
<c:choose>
<c:when test="${not empty project.url}">
<c:set var="projectURL"><c:out value="${project.url}"/></c:set>
<h1><a href="${projectURL}">
  <c:out value="${project.fullTitle}"/></a></h1>
<p class="sectionDescription"><a href="${projectURL}"><c:out value="${project.description}"/></a></p>
</c:when>
<c:otherwise>
<h1><c:out value="${project.fullTitle}"/></h1>
<p class="sectionDescription"><c:out value="${project.description}"/></p>
</c:otherwise>
</c:choose>
<c:if test="${not empty section}">
<p class="sectionDescription">Section: <c:out value="${section}"/></p>
</c:if>
<c:if test="${!project.visibleToStudents}">
    <c:url var="makeVisibleLink"
        value="/action/instructor/MakeProjectVisible" />
    <form method="post" action="${makeVisibleLink}"><input
        type="hidden" name="projectPK" value="${project.projectPK}" />
    <p  class="projectDeadline">Not visible to students.
    <input type="hidden" name="newValue" value="true"/>
    <input type="submit" value="Make Visible" style="color: #003399" /></form>
</c:if>

<p class="projectDeadline"><b>Deadline:</b>&nbsp;
	<span><fmt:formatDate value="${project.ontime}" pattern="E',' dd MMM 'at' hh:mm a" /></span>
<c:if test="${project.ontime != project.late}">
&nbsp;(Late:
	<span><fmt:formatDate value="${project.late}" pattern="E',' dd MMM 'at' hh:mm a" /></span>)
</c:if>	
</p>

<c:if test="${project.pair}">
<p class="projectDeadline">Pair project</p>
</c:if>
</div>

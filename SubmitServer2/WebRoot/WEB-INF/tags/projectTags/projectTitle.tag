<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="sectionTitle">
<h1>Project ${project.projectNumber}: ${project.title}</h1>
<p class="sectionDescription">${project.description}</p>

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

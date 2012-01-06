<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:choose>
<c:when test="${not empty student.email}">
<a href="mailto:${student.email}">
<c:out value="${student.email}"/></a>
</c:when>
<c:otherwise>no email</c:otherwise>
</c:choose>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${student.hasPicture}">
<c:url var="pictureLink" value="/view/instructor/ViewPicture">
		<c:param name="studentRegistrationPK" value="${studentRegistration.studentRegistrationPK}" />
        <c:param name="studentPK" value="${student.studentPK}" />
	</c:url>
<p><img src="${pictureLink}"/>
</c:if>

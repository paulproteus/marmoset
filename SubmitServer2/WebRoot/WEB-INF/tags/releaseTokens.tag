
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<p>
    You currently have ${releaseInformation.tokensRemaining} release tokens available.
    
    <c:if test="${not empty releaseInformation.regenerationSchedule}">
        <p>Release token(s) will regenerate at:
        <ul>
            <c:forEach var="timestamp" items="${releaseInformation.regenerationSchedule}">
                <li><fmt:formatDate value="${timestamp}" pattern="E',' dd MMM 'at' hh:mm a" /><br>
            </c:forEach>
        </ul>
    </c:if>
</p>
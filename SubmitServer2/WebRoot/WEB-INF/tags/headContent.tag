<%@ attribute name="title" required="true" rtexprvalue="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

 <title><c:out value="${title}"/></title>
 <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/styles.css">

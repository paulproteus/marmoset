<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ attribute name="path" required="true" rtexprvalue="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:url var="url" value="/images/${path}" />
<c:out value="${url}" />

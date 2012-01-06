<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ attribute name="file" required="true" rtexprvalue="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:url var="url" value="/js/${file}" />
<script type="text/javascript" src="${url}"></script>

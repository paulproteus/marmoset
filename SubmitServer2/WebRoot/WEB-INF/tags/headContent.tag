<%@ attribute name="title" required="true" rtexprvalue="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
                
 <title><c:out value="${title}"/></title>
 <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/styles.css">
<script type="text/javascript">
 function toggle(item) {
     obj = document.getElementById(item);
     if (obj.style.display == "none") {
         obj.style.display = "block";
     } else {
         obj.style.display = "none";
     }
 }
</script>

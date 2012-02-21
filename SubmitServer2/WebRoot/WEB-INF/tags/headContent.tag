<%@ attribute name="title" required="true" rtexprvalue="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
                
 <title><c:out value="${title}"/></title>
 <c:url var="css" value="/styles.css"/>
 <link rel="stylesheet" type="text/css" href="${css}">
 <script
	src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.4/jquery.min.js"
	type="text/javascript"></script>
    <script type="text/javascript">
 function toggle(item) {
     $(document.getElementById(item)).slideToggle("slow");
 }
 function showItem(item) {
     $(document.getElementById(item)).slideDown("slow");
 }
 function hideItem(item) {
     $(document.getElementById(item)).slideUp("slow");
 }
</script>
 <ss:brandingProperty key="branding.analytics" safeHtml="true" />


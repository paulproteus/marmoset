<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss" %>
 <c:set var="serviceURL"><ss:brandingProperty key="branding.service.url" safeHtml="true" /></c:set>
   
<div class="sectionTitle">
  <h1>${ss:webProperty('semesterName')}</h1>
    
    <p class="sectionDescription">Welcome to <a href="${serviceURL}"><ss:brandingProperty key="branding.service.fullname" safeHtml="true" /></a>
 
</div>

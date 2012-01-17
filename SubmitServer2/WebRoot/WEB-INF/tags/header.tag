<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss" %>
<div class="header">
	<!-- TODO(rwsims): Fix img tag to use branding properties. -->
	<c:set var="brandLogoImage">
		<ss:brandingProperty key="branding.image.logo" />
	</c:set>
	<c:set var="brandLogoImageUrl">
		<c:url value="${brandLogoImage}" />
	</c:set>
	<div id="headerImage"><img src="${brandLogoImageUrl}" 
							   alt="University of Maryland" title="University of Maryland" 
							   width="281" height="69"/></div> 
<div id="headerTitle">CS Project Submission Server</div> 	
<div id="headerContact">&nbsp;</div>    
</div>


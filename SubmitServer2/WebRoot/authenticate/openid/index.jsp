<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<c:set var="serviceName">
<ss:brandingProperty key="branding.service.fullname" safeHtml="true" />
</c:set>

<!DOCTYPE html>
<html>
<head>
  <ss:headContent title="OpenID Login to ${serviceName}" />
  <!-- Simple OpenID Selector -->
  <c:url var="openIdStyle" value="/openid.css" />
  <link type="text/css" rel="stylesheet" href="${openIdStyle}" />

  <ss:script file="openid-jquery.js" />
  <ss:script file="openid-en.js" />
  <c:set var="imageBase" value="${pageContext.request.contextPath}/images/" />

  <!-- /Simple OpenID Selector -->
</head>
<body>
  <ss:header/>
  <ss:loginBreadCrumb/>
  <ss:loginTitle/>
    <c:url var="verifyOpenId" value="/authenticate/openid/verify" />
  
      <c:set var="target">
        <c:out value="${param.target}" />
    </c:set>
     <c:set var="serviceURL"><ss:brandingProperty key="branding.service.url" safeHtml="true" /></c:set>
   
    <p class="notemessage">Welcome to <a href="${serviceURL}">${serviceName}</a>. Cookies must be enabled to use Marmoset.
    
    <c:if test="${not empty demoAccounts}">
    <h2>Demonstration accounts</h2>
     <p class="notemessage">This is a demonstration server, and has been configured with several demo accounts. 
    These demo accounts have already been setup with submissions, test results and code reviews, so you can see what those look like.
   <p class="notemessage">Other visitors will use the same demo accounts, so will see any changes or new submissions made by those before you.
    The demo accounts may be reset from time to time if previous changes by others leave them too cluttered or confusing.
    
       
      <form action="${verifyOpenId}" method="GET">
      <input type="hidden" name="marmoset.target" value="${target}" />
        
       <table class="form">
    
        <c:forEach var="student" items="${demoAccounts}">
        <c:set var="loginName"><c:out value="${student.loginName}"/></c:set>
        <tr><td class="input"><input type="radio" name="login_name" value="${loginName}" />
        <c:out value="${student.fullname}"/>
      </td></tr> 
        </c:forEach>
        
        <tr class="submit"><td>
        <input type="submit"  value="Log in as"/>
        </table>
    </form>

<h2>Normal authentication via Open-ID</h2>
    </c:if>
      <p class="notemessage">Authentication is via OpenID, a service that allows you to log-on to many different websites using a single identity. 
    If you have an account with any of these
          providers, you can use that account to identify yourself. 
     Find out <a href="http://openid.net/what/">more about OpenID</a> and <a href="http://openid.net/get/">how to get an OpenID enabled account</a>.</p>
     
   
  <!-- Simple Open ID Selector -->
  <c:url var="initiateUrl" value="/authenticate/openid/initiate" />
  <form method="POST" id="openid_form" action="${initiateUrl}">
    <input type="hidden" name="action" value="verify" />
    <input type="hidden" name="marmoset.target" value="${target}" />
    <fieldset>
      <legend>Sign-in or Create New Account</legend>
      <div id="openid_choice">
        <p>Please click your account provider:</p>
        <div id="openid_btns"></div>
      </div>
      <div id="openid_input_area">
        <input id="openid_identifier" name="openid_identifier" type="text" value="http://" />
        <input id="openid_submit" type="submit" value="Sign-In"/>
      </div>
      <noscript>
      <p class="notemessage">You must have Javascript enabled for open-id login to work.
        </noscript>
    </fieldset>
  </form>
  <!-- /Simple OpenID Selector -->
  <c:if test="${skipAuthentication}">
  	<h2>Skip authentication</h2>
		<p>The submit server is set to skip authentication. Making OpenID requests 
        from localhost can have strange semantics, and
			some providers may refuse to authenticate at all. Authentication this way
            allows you to avoid this issue, and to easily create fake identities for
            testing purposes</p>
            <p>Enter Open id identity:
            
		<form action="${verifyOpenId}" method="GET">
  		<input type="hidden" name="marmoset.target" value="${target}" />
  		<input type="text" name="uid" placeholder="Enter a fake OpenID identity" size="30"/>
  		<input type="submit" />
  	</form>
    <p>Enter login_name:
    <form action="${verifyOpenId}" method="GET">
        <input type="hidden" name="marmoset.target" value="${target}" />
        <input type="text" name="login_name" placeholder="Enter existing username" />
        <input type="submit" />
    </form>
  </c:if>
  
    <p class="notemessage"><ss:brandingProperty key="branding.login.termsOfUse" safeHtml="true" />
  
    <ss:footer/>
      <script type="text/javascript">
    $(document).ready(function() {
      openid.img_path = '<c:out value="${imageBase}" />';
      openid.init('openid_identifier');
      // openid.setDemoMode(true); //Stops form submission for client javascript-only test purposes
    });
  </script>
</body>
</html>

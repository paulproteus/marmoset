<%@ page language="java" contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script
  src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.4/jquery.min.js"
  type="text/javascript"></script>
  <!-- Simple OpenID Selector -->
  <c:url var="openIdStyle" value="/openid.css" />
  <link type="text/css" rel="stylesheet" href="${openIdStyle}" />
  <ss:headContent title="OpenID Login" />
  <ss:script file="openid-jquery.js" />
  <ss:script file="openid-en.js" />
  <c:url var="imageBase" value="/images/" />
  <script type="text/javascript">
    $(document).ready(function() {
      openid.img_path = '<c:out value="${imageBase}" />';
      openid.init('openid_identifier');
      // openid.setDemoMode(true); //Stops form submission for client javascript-only test purposes
    });
  </script>
  <!-- /Simple OpenID Selector -->
</head>
<body>
  <ss:header/>
  <ss:loginBreadCrumb/>
  <ss:loginTitle/>
  
  <!-- Simple Open ID Selector -->
  <c:url var="initiateUrl" value="/authenticate/openid/initiate" />
  <form method="POST" id="openid_form" action="${initiateUrl}">
    <input type="hidden" name="action" value="verify" />
    <c:set var="target">
    	<c:out value="${param.target}" />
    </c:set>
    <input type="hidden" name="target" value="${target}" />
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
        <p>OpenID is service that allows you to log-on to many different websites using a single indentity.
        Find out <a href="http://openid.net/what/">more about OpenID</a> and <a href="http://openid.net/get/">how to get an OpenID enabled account</a>.</p>
      </noscript>
    </fieldset>
  </form>
  <!-- /Simple OpenID Selector -->
  <c:if test="${skipAuthentication}">
  	<c:url var="verifyOpenId" value="/authenticate/openid/verify" />
  	<h2>Skip authentication</h2>
		<p>The submit server is set to skip authentication. Note that
			making OpenID requests from localhost can have strange semantics, and
			some providers may refuse to authenticate at all.</p>
		<form action="${verifyOpenId}" method="GET">
  		<input type="hidden" name="target" value="${target}" />
  		<input type="text" name="uid" placeholder="Enter a fake OpenID identity" />
  		<input type="submit" />
  	</form>
    <form action="${verifyOpenId}" method="GET">
        <input type="hidden" name="target" value="${target}" />
        <input type="text" name="login_name" placeholder="Enter existing username" />
        <input type="submit" />
    </form>
  </c:if>
    <ss:footer/>
</body>
</html>

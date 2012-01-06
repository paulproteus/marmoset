<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<!DOCTYPE html>
<!-- Form for entering registration info after an OpenID login we haven't seen before. -->
<html>
<head>
<c:if test="${empty param.uid}">
     <!-- If a UID parameter is not given, this is an invalid registration flow, so redirect to the beginning.
          Note that this discards any "target" parameter, but that's unlikely to matter.-->
	<c:redirect url="/authenticate/openid/login.jsp" />
</c:if>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<ss:headContent title="OpenID Registration" />
<script
	src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.4/jquery.min.js"
	type="text/javascript"></script>
<script
	src="http://ajax.aspnetcdn.com/ajax/jquery.validate/1.9/jquery.validate.min.js"
	type="text/javascript"></script>
<script>
	$(document).ready(function() {
		$("#registration-form").validate();
	});
</script>

<style>
form ul {list-style-type: none;}
form ul label {display: block;}
form ul li {margin-bottom: 1em;}
form li.required label {font-weight: bold;}
</style>

</head>
<body>
	<ss:header />
	<h1>OpenID Registration</h1>
	<p>This is the first time you have logged in with this OpenID. Please provide your account details below; bold fields are required.</p>
	<c:url var="finishUrl" value="/authenticate/openid/register" />
	<form method="POST" action="${finishUrl}" id="registration-form">
	<c:set var="escapedUid">
		<c:out value="${param.uid}" />
	</c:set>
		<input type="hidden" name="uid" value="${escapedUid}" />
		<c:set var="target">
			<c:out value="${param.target}" />
		</c:set>
		<input type="hidden" name="target" value="${target}" />
		<ul>
			<li class="required">
				<label for="login-input">Username:</label>
				<input type="text" name="login" id="login-input" class="required"/>
			</li>
			<li class="required">
				<label for="email-input">Email:</label>
				<input type="text" name="email" id="email-input" class="email required"/>
			</li>
			<li class="required">
				<label for="firstname-input">First Name:</label>
				<input type="text" name="firstname" id="firstname-input" class="required"/>
			</li>
			<li class="required">
				<label for="lastname-input">Last Name:</label>
				<input type="text" name="lastname" id="lastname-input" class="required"/>
			</li>
			</ul>
		    <input type="submit" value="Register" />
	</form>
	<ss:footer />
</body>
</html>
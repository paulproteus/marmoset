<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<!DOCTYPE html>
<!-- Form for entering registration info after an OpenID login we haven't seen before. -->
<html>
<head>
<c:if test="${empty param.uid}">
     <!-- If a UID parameter is not given, this is an invalid registration flow, so redirect to the beginning.
          This discards any "target" parameter, but that's unlikely to matter.-->
	<c:redirect url="/authenticate/openid/login.jsp" />
</c:if>
<ss:headContent title="OpenID Registration" />
<script
	src="http://ajax.aspnetcdn.com/ajax/jquery.validate/1.9/jquery.validate.min.js"
	type="text/javascript"></script>

<c:url var="checkRegistrationUrl" value="/authenticate/CheckRegistration" />
<style>
form ul {list-style-type: none;}
form ul label {display: block;}
form ul li {margin-bottom: 1em;}
form li.required label {font-weight: bold;}
</style>
	<script type="text/javascript">
	$.validator.addMethod("regex", function(value, element, regexpString){
		var regexp = new RegExp(regexpString);
		return this.optional(element) || regexp.test(value);
	});
	$(document).ready(function() {
		$("#registration-form").validate({
			rules: {
				login: {
					required: true,
					remote: "${checkRegistrationUrl}",
					maxlength: 20,
					minlength: 2,
					regex: "^[a-zA-Z][a-zA-Z0-9]+$"
				},
				email: {
					required: true,
					email: true,
					remote: "${checkRegistrationUrl}"
				}
			},
			messages: {
				login: {
					required: "Enter a login name",
					minlength: $.format("Enter at least {0} characters"),
					maxlength: $.format("Enter at most {0} characters"),
					remote: "Login is already taken",
					regex: "Login name is invalid."
				},
				email: {
					remote: "Email is already in use"
				}
			}
		});
	});
</script>
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
                <label for="firstname-input">Name:</label>
                <input type="text" name="firstname" id="firstname-input" placeholder="First" class="required" required="required"
                value = "${openidAx.firstname}"/>
                <input type="text" name="lastname" id="lastname-input"  placeholder="Last" class="required" required="required"
                value = "${openidAx.lastname}"/>
            </li>
        <li class="required">
                <label for="email-input">Email:</label>
                <input type="text" name="email" id="email-input"  placeholder="email" class="email required" required="required"
                value = "${openidAx.email}"/>
            </li>
        
			<li class="required">
				<label for="login">Username:</label>
				<input type="text" name="login" id="login" placeholder="username" required="required" />
			</li>
							</ul>
		    <input type="submit" value="Register" />
	</form>

	<ss:footer />
</body>
</html>
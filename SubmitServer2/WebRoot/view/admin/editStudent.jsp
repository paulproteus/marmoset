<%--

 Marmoset: a student project snapshot, submission, testing and code review
 system developed by the Univ. of Maryland, College Park
 
 Developed as part of Jaime Spacco's Ph.D. thesis work, continuing effort led
 by William Pugh. See http://marmoset.cs.umd.edu/
 
 Copyright 2005 - 2011, Univ. of Maryland
 
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy of
 the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 License for the specific language governing permissions and limitations under
 the License.

--%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<c:if test="${!user.canImportCourses}">
      <c:redirect url="/view/index.jsp"/>
</c:if>
<!DOCTYPE html>
<html>
<head>
<ss:headContent title="Editing student record for ${ss:escapeHtml(student.fullname)}" />
<style>
form ul {list-style-type: none;}
form ul label {display: block;}
form ul li {margin-bottom: 1em;}
form li.required label {font-weight: bold;}
</style>
</head>
<body>
<ss:header />
<ss:instructorBreadCrumb />
<h1>Edit student account for <c:out value="${student.fullname}"/></h1>
<c:url value="/action/admin/EditStudentAccount" var="actionUrl" />
<form action="${actionUrl}" method="POST" id="edit-form">
<input type="hidden" name="studentPK" value="${student.studentPK}" />
		<ul>
			<li class="required">
				<label for="firstname-input">Name:</label>
				<input type="text" name="firstname" id="firstname-input" placeholder="First" class="required" required="required" value="${ss:escapeHtml(student.firstname)}"/>
				<input type="text" name="lastname" id="lastname-input" placeholder="Last" class="required" required="required" value="${ss:escapeHtml(student.lastname)}"/>
			</li>
			<li class="required">
				<label for="email-input">Email:</label>
				<input type="text" name="email" id="email-input" placeholder="email" class="email required" required="required" value="${ss:escapeHtml(student.email)}" />
			</li>
			<li class="required">
				<label for="login-input">Username:</label>
				<input type="text" name="login" id="login-input" placeholder="username" class="required" required="required" value="${ss:escapeHtml(student.loginName)}" />
			</li>
		</ul>
		<button name="action" value="UPDATE" type="submit">Update Student</button>
		<!-- <button name="action" value="DELETE" type="submit">Delete Student</button> -->
	</form>
<ss:footer />
<script
	src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.4/jquery.min.js"
	type="text/javascript"></script>
<script
	src="http://ajax.aspnetcdn.com/ajax/jquery.validate/1.9/jquery.validate.min.js"
	type="text/javascript"></script>
<script>
	$(document).ready(function() {
		$("#edit-form").validate();
	});
</script>
</body>
</html>
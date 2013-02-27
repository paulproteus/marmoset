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

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML>
<html>
	<ss:head title="Edit registration for ${student.fullname} in ${course.courseName}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<div class="sectionTitle">
	<h1>Edit Registration</h1>
<ss:studentPicture />
	<p class="sectionDescription">Edit registration for
	<c:out value="${student.fullname}"/>
     in ${course.courseName}</p>

	<c:if test="${not empty course.courseIDs}">
	<h2>WARNING: this course is synchronized to the grades server</h2>
	<p>Any changes made here will be overwritten the next time the course is
	synchronized to the grades server</p>
	</c:if>
</div>

<p>
<form id="editStudentRegistrationForm"
	action="<c:url value="/action/instructor/EditStudentRegistration"/>"
	method="post">
<input type="hidden" name="studentRegistrationPK" value="${studentRegistration.studentRegistrationPK}"/>
<table class="form">
	<tr>
	    <th> inactive </th>
	    <th> dropped </th>
		<th> First Name </th>
		<th> Last Name </th>
		<th> login name </th>
		<th> UID</th>
		<th> Class Account <br>(use login name for courses <br>without class accounts)</th>
	</tr>
	<tr>
	    <td> <input name="inactive" type="checkbox" ${ss:isChecked(studentRegistration.inactive)} />
	         </td>
	 	<td> <input name="dropped" type="checkbox" ${ss:isChecked(studentRegistration.dropped)} />
	         </td>
		<td> <input name="firstname" type="text" value="${student.firstname}"/> </td>
		<td> <input name="lastname" type="text" value="${student.lastname}"/> </td>
		<td> <input name="loginName" type="text" value="${student.loginName}"/> </td>
		<td> <input name="campusUID" type="text" value="${student.campusUID}"/> </td>
		<td> <input name="classAccount" type="text" value="${studentRegistration.classAccount}"/> </td>
		
	</tr>
	<tr>
		<td colspan="8"> <input type="submit" value="Update record!"/> </td>

	</tr>
</table>
</form>
<h3> ${ss:scrub(param.editStudentRegistrationMessage)} </h3>

</body>
</html>

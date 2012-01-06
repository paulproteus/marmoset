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


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<!DOCTYPE HTML>
<html>
<ss:head
	title="Confirm release request for submission ${ss:scrub(param.submissionPK)}" />

<body>
<ss:header />
<ss:breadCrumb />

<h2> Change password for <c:out value="${student.fullname}"/> </h2>

<c:url var="changePasswordLink" value="/action/ChangePassword"/>
<form method="POST" name="changePassword" action="${changePasswordLink}">
<input type="hidden" name="studentPK" value="${student.studentPK}"/>
<table>
<tr>
	<td> Current Password: </td>
	<td> <input type="password" name="currentPassword"/> </td>
</tr>
<tr>
	<td> New Password: </td>
	<td> <input type="password" name="newPassword"/> </td>
</tr>
<tr>
	<td> Confirm New Password: </td>
	<td> <input type="password" name="confirmNewPassword"/> </td>
</tr>
<tr>
	<td colspan="2"> <input type="submit" value="Change Password!"/>
</tr>
</table>

</form>
<ss:footer/>
</body>
</html>

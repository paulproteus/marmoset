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


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML>
<html>
	<ss:head title="Grant an extension to ${student.fullname}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />
<body>
<ss:studentPicture />
<p>
Grant an extension to 
<c:out value="${studentRegistration.fullname}"/> for project 
<c:out value="${project.projectNumber}"/>
<p>
<b><font color=red>NOTE:</font></b> Extension are given in hours
<form class="form" name="form" action="<c:url value="/action/instructor/GrantExtension"/>" method="POST">
<!--
	<select>
	<option> 6</option>
	<option> 12</option>
	<option> 18</option>
	<option> 24</option>
	<option> 36</option>
	</select>
-->
	<input type="text" name="extension">
	<input type="hidden" name="studentRegistrationPK" value="${studentRegistration.studentRegistrationPK}">
	<input type="hidden" name="projectPK" value="${project.projectPK}">
	<br>
	<input type="submit" value="Grant extension">
</form>
</body>
</html>

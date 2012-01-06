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
	title="Register students for ${course.courseName} in semester ${course.semester}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<div class="sectionTitle">
	<h1>Register Students</h1>
	
	<p class="sectionDescription">Register students for
	 ${course.courseName} in semester ${course.semester} by
		uploading a file containing comma-separated fields. </p>
</div>

<p>

<form action="<c:url value="/action/instructor/RegisterStudents"/>"
	method="POST" enctype="multipart/form-data">

	<input type="hidden" name="coursePK" value="${course.coursePK}" />

<table class="form">
	<tr>
		<td class="label">File containing student registrations:
		<td class="input"><input type="file" size="40" name="file" />
		
		<c:if test="${initParam['authentication.service']=='edu.umd.cs.submitServer.GenericStudentPasswordAuthenticationService'}">
		<input type="hidden" name="authenticateType" value="generic" />
		</c:if>

	<c:if test="${initParam['authentication.service']!='edu.umd.cs.submitServer.GenericStudentPasswordAuthenticationService'}">		
	<tr>
		<td colspan="5">
		<center>
		<table>
			<tr>
				<td>Use Default Authentication Service (directory ID and password)</td>
				<td><input type="radio" name="authenticateType" value="default" checked>
			</tr>
			<tr>
				<td>Use Marmoset for Authentication (Marmoset will generate passwords)</td>
				<td><input type="radio" name="authenticateType" value="generic">
			</tr>
		</table>		
		</center>
		</td>
	</tr>
	</c:if>
	
	<tr>
		<td colspan=2 class="submit"><input type="submit"
			value="Register students" />
	<tr>
		<td colspan=2 class="description">
		<ss:registerStudentsFileFormat />
</table>
</form>

<ss:footer />
</body>
</html>

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

<c:choose>
<c:when test="${!gradesServer}">
<jsp:forward page="registerPerson.jsp" >
  <jsp:param name="coursePK" value="course.coursePK" />
</jsp:forward>
</c:when>
<c:otherwise>
<div class="sectionTitle">
	<h1>Register Students</h1>
	
	<p class="sectionDescription">Register students for
	 ${course.courseName} in semester ${course.semester} by
		uploading a file containing comma-separated fields. </p>
</div>

<p>

<c:url var="registerStudents" value="/view/instructor/studentsRegistered.jsp"/>

<form action="${registerStudents}"
	method="POST" enctype="multipart/form-data">

	<input type="hidden" name="coursePK" value="${course.coursePK}" />

<table class="form">
	<tr>
		<td class="label">File containing student registrations:
		<td class="input"><input type="file" size="40" name="file" />
		<input type="hidden" name="authenticateType" value="default" />
        	
	<tr  class="submit">
		<td colspan=2 class="submit"><input type="submit"
			value="Register students" />
	

</table>
</form>

        <ss:registerStudentsFileFormat />

        </c:otherwise>
        </c:choose>
<ss:footer />
</body>
</html>

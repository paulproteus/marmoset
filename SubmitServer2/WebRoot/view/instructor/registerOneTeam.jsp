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
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML>
<html>
	<ss:head title="Register a team for ${course.courseName}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<div class="sectionTitle">
	<h1>New Team Registration</h1>
	
	<p class="sectionDescription">Use this form to create a team 
	for ${course.courseName} and add students to it. <br>
	Each team should be associated with exactly one project and have exactly two members. <br>
	Team name should be unique - e.g. begin with "${course.courseName}:".</p>
</div>

<div class="errorMessage">${ss:scrub(param.errorMessage)}</div>

<!-- Project Size: ${projectList} -->

<p>
<form name="RegisterOneTeamForm"
	action="<c:url value="/action/instructor/RegisterOneTeam"/>"
	method="post">
	<input type="hidden" name="coursePK" value="${course.coursePK}"/>
	<input type="hidden" name="campusUID" value="00000000"/> <!-- Employee number not used -->
	<input type="hidden" name="accountType" value="student">   <!-- Always a student account -->
	<input type="hidden" name="lastname" value=" ">   <!-- Last Name is calculated from projectNumber -->
<table>
	<tr>
		<th> Team ID <br /> (should be unique) </th>
		<th> Team Name </th>
		<th> Class Account <br>(use Team ID for courses <br> without class accounts) </th>
		<th> Project </th>
		<th> Team Members </th>
	</tr>
	<tr>
		<td> <input name="loginName" type="text" value="${course.courseName}:"/> </td>   <!-- Team ID -->
		<td> <input name="firstname" type="text" value=""/> </td>   <!-- Team Name -->
		<td> <input name="classAccount" type="text" value=""/> </td>  <!-- Class Account -->
		<td> <select name="projectPK">                              <!-- Project -->
			    <c:forEach var="project" items="${projectList}" varStatus="counter">
			    	<option value="${project.projectPK}">
			    		${project.projectNumber} - ${project.title}
			    	</option>
			    </c:forEach>
			 </select>
		</td>    
		<td> <p><select name="studentPK1">                               <!-- Team Members -->
			    <c:forEach var="studentRegistration" items="${studentRegistrationSet}" varStatus="counter">
			    	<option value="${studentRegistration.studentPK}">
			    		${studentRegistration.lastname}, ${studentRegistration.firstname}
			    	</option>
			    </c:forEach>
			 </select></p> 
			 <p><select name="studentPK2">                               <!-- Team Members -->
			    <c:forEach var="studentRegistration" items="${studentRegistrationSet}" varStatus="counter">
			    	<option value="${studentRegistration.studentPK}">
			    		${studentRegistration.lastname}, ${studentRegistration.firstname}
			    	</option>
			    </c:forEach>
			 </select></p>
		</td> 
	</tr>
	<tr>
		<td colspan="6"> <input type="submit" value="Register Team!"/> </td>
	</tr>
</table>
</form>
</body>
</html>

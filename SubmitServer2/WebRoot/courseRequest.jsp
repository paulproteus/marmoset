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
  <ss:head title="Course Request Form"/>
  <body>
  <ss:header/>
  <ss:loginBreadCrumb/>

	<div class="sectionTitle">
		<h1>Course Request Form</h1>
		<p class="sectionDescription">Use this form to email a course creation request to the Submit Server
		Administrator.</p>
	</div>

	<c:url var="createCourseLink" value="/nologin/RequestCourse"/>
	<form action="${createCourseLink}" method="post" name="requestCourseForm">
	<table class="form">
		<tr><th class="crSection" colspan=2>Enter Course Information</th></tr>
		<tr>
			<td class="label">Course Name (e.g. CMSC131):</td>
			<td class="input"><input type="text" name="courseName" value=""></td>
		</tr>
		<tr>
			<td class="label">Course Title (can be empty): </td>
			<td class="input">
				<textarea cols="40" rows="1" name="courseTitle"></textarea>
			</td>
		</tr>
		<tr>
			<td class="label">URL:</td>
			<td class="input"><input type="url" name="url" size="60" value=""></td>
		</tr>
		<tr>
			<td class="label">Semester:</td>
			<td class="input">
				<input type="text" name="semester" value="${ss:webProperty('semester')}"/>
			<%--
				<select name="semester">
				<option value="Spring2005"> Spring2005 </option>
				</select>
			--%>
			</td>
		</tr>
	</table>

	<table>
		<tr><th class="crSection" colspan=6>Enter Instructor and TA information</th></tr>
		<tr>
			<th> </th>
			<th> firstname </th>
			<th> lastname </th>
			<th> <ss:loginName /> </th>
			<th> <ss:campusUID /></th>
			<th> class account <br> (use <ss:loginName/> for courses <br> without class accounts)</th>
			
		</tr>
		<tr>
			<td> <em>required</em> <select name="type1">
					<option value="Instructor">Instructor</option>
					<option value="TA">TA</option>
			 	 </select>
			</td>
			<td> <input name="firstname1" type="text" value=""/> </td>
			<td> <input name="lastname1" type="text" value=""/> </td>
			<td> <input name="loginName1" type="text" value=""/> </td>
			<td> <input name="campusUID1" type="text" value=""/> </td>
			<td> <input name="classAccount1" type="text" value=""/> </td>
			
		</tr>

		<tr>
			<td> <em>optional</em> <select name="type2">
					<option value="Instructor">Instructor</option>
					<option value="TA">TA</option>
			 	 </select>
			</td>
			<td> <input name="firstname2" type="text" value=""/> </td>
			<td> <input name="lastname2" type="text" value=""/> </td>
			<td> <input name="loginName2" type="text" value=""/> </td>
			<td> <input name="campusUID2" type="text" value=""/> </td>
			<td> <input name="classAccount2" type="text" value=""/> </td>
			
		</tr>

		<tr>
			<td> <em>optional</em> <select name="type3">
					<option value="Instructor">Instructor</option>
					<option value="TA">TA</option>
			 	 </select>
			</td>
			<td> <input name="firstname3" type="text" value=""/> </td>
			<td> <input name="lastname3" type="text" value=""/> </td>
			<td> <input name="loginName3" type="text" value=""/> </td>
			<td> <input name="campusUID3" type="text" value=""/> </td>
			<td> <input name="classAccount3" type="text" value=""/> </td>
			<
		</tr>
	</table>


	<table class="form">
		<tr><th class="crSection" colspan=2>Other Information</th></tr>
		<tr>
			<td class="label">Your Email Address:</td>
			<td class="input"><input type="email" name="emailAddress"></td>
		</tr>
		<tr>
			<td class="label">Does course require a Build Server? </td>
			<td class="input">
				<SELECT NAME="requireBuildServer">
					<OPTION value="yes" SELECTED>Yes, at least one project will be compiled and/or tested on the server</OPTION>
					<OPTION value="no">No, student submissions will only be stored on the server</OPTION>
				</SELECT>
			</td>
		</tr>
		<tr>
			<td class="label">Comments: </td>
			<td class="input">
				<textarea cols="40" rows="6" name="comments"></textarea>
			</td>
		</tr>
	</table>

	<p class = "alertmessage">A confirmation message will be sent
		to your email address when the course is created. <br>
		<input type="submit" value="Send Course Request">
	</p>

	</form>

  <ss:footer/>
  </body>
</html>

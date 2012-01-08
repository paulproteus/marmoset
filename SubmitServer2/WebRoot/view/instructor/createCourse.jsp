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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions'%>
<!DOCTYPE HTML>
<html>

<ss:head
	title="Create course" />
<body>
<ss:header />
<ss:instructorBreadCrumb />


<h1>Create course</h1>
<p>
<c:url var="createCourseLink" value="/action/admin/CreateCourse"/>
	<form action="${createCourseLink}" method="post" name="createCourseForm">
	<table class="form">
		<tr><th colspan=2>Create new course</th></tr>
		<tr>
			<td class="label">Course Name:</td>
			<td class="input"><input type="text" name="courseName"></td>
		</tr>
		<tr>
			<td class="label">Semester:</td>
			<td class="input">
				<input type="text" name="semester" value="${initParam['semester']}"/>
			</td>
		</tr>
		<tr>
			<td class="label">Description <br>(can be empty): </td>
			<td class="input">
				<textarea cols="40" rows="6" name="description"></textarea>
			</td>
		</tr>
		<tr>
			<td class="label">URL:</td>
			<td class="input"><input type="url" name="url" size="60"></td>
		</tr>
		<tr>
            <td class="label">allows baseline/starter code download:</td>
            <td class="input"><input name="download" type="checkbox" checked  />
            </td>
        </tr>
		<tr  class="submit"><td colspan=2>
			<input type="submit" value="Create course">
	</table>


	</form>




	<ss:footer/>
  </body>
</html>

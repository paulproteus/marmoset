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
<ss:head title="Register Student for ${course.courseName}" />
<body>
	<ss:header />
	<ss:instructorBreadCrumb />

	<div class="sectionTitle">
		<h1>Student Registration</h1>

		<p class="sectionDescription">Use this form to add a person to ${course.courseName}</p>
	</div>

<div>
    <form name="registerExistingStudentForm"
        action='<c:url value="/action/instructor/RegisterOneStudent"/>'
        method="post">
        <input type="hidden" name="coursePK" value="${course.coursePK}" />
        <table class="form">
             <tfoot>
            <tr class="submit">
                <td class="submit" colspan="2"><input type="submit"
                    value="Register existing student" />
                </td>
            </tr>
            </tfoot>
        <tbody>
            <tr>
                <td class="label">Student</td>
                
                <td class="input"><select name="existingStudentPK">
                    <c:forEach var="student" items="${allStudents}">
                    <option value="${student.studentPK}">
                    <c:out value="${student.lastname}"/>,
                    <c:out value="${student.firstname}"/>
                    (<c:out value="${student.loginName}"/>)
                    </option>
                    </c:forEach>
                    </select>
            <tr>
                <td class="label">Class account</td>
                <td class="input"><input name="classAccount" type="text"
                    value="" autocorrect="off" autocapitalize="off"/>
                </td>
            </tr>
             <tr>
                <td class="label">Capability</td>
                <td class="input">
                    <select name="capability">
                                 <option value="">Student</option>
                                 <option value="modify">Instructor</option>
                                 <option value="read-only">Grader</option>
                                 <option value="pseudo-student" 
                                 title="does not participate in code reviews">Pseudo student </option>
                            </select>
                            
                </td>
            </tr>
            </tbody>
       
        </table>
    </form>

</div>
<div>
	<form name="registerOneStudentForm"
		action='<c:url value="/action/instructor/RegisterOneStudent"/>'
		method="post">
		<input type="hidden" name="coursePK" value="${course.coursePK}" />
		<table class="form">
		  <tfoot>
            <tr class="submit">
                <td class="submit" colspan="2"><input type="submit"
                    value="Register new student" />
                </td>
            </tr>
            </tfoot>
		<tbody>
			<tr>
				<td class="label">First name</td>
				<td class="input"><input name="firstname" type="text" value=""  required/>
				</td>
			</tr>
			<tr>
				<td class="label">Last name</td>
				<td class="input"><input name="lastname" type="text" value="" required />
				</td>
			</tr>
			<tr>
				<td class="label">Login name</td>
				<td class="input"><input name="loginName" type="text" value="" autocorrect="off" autocapitalize="off" required/>
				</td>
			</tr>
			<tr>
				<td class="label">Campus uid</td>
				<td class="input"><input name="campusUID" type="text" value="" autocorrect="off" autocapitalize="off" required/>
				</td>
			</tr>
			<tr>
				<td class="label">Class account</td>
				<td class="input"><input name="classAccount" type="text"
					value=""  autocorrect="off" autocapitalize="off"/>
				</td>
			</tr>
			 <tr>
                <td class="label">Capability</td>
                <td class="input">
                    <select name="capability" >
                                 <option value="">Student</option>
                                 <option value="modify">Instructor</option>
                                 <option value="read-only">Grader</option>
                                 <option value="pseudo-student" 
                                 title="does not participate in code reviews">Pseudo student </option>
                            </select>
                            
                </td>
            </tr>
			</tbody>
		
		</table>
	</form>

</div>
</body>
</html>

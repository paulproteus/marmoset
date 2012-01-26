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
<!DOCTYPE HTML>
<html>

<ss:head
	title="Bad Zip entries" />
<body>
<ss:header />
<ss:instructorBreadCrumb />


	<table>

<tr>
<th>#<th>Project<th>Student

		<c:forEach var="submission" items="${badSubmissions}" varStatus="counter">
		<c:set var="project" value="${badSubmissionsProject[submission]}" />
            <c:set var="studentRegistration"
                value="${badSubmissionStudentRegistration[submission]}" />
                <c:url var="projectLink"
                        value="/view/instructor/project.jsp">
                        <c:param name="projectPK" value="${project.projectPK}" />
                    </c:url> 
                    <c:url var="studentLink" value="/view/instructor/student.jsp">
                        <c:param name="studentRegistrationPK" value="${studentRegistration.studentRegistrationPK}" />
                    </c:url>
                            <c:url var="submissionLink"
                        value="/view/instructor/submission.jsp">
                        <c:param name="submissionPK" value="${submission.submissionPK}" />
                    </c:url>
                    
                <tr class="r${counter.index % 2}">
                <td><a href="${submissionLink}">${submission.submissionNumber}</a></td>
                <td><a href="${projectLink}"><c:out value="${project.fullDescription}"/></a></td>
                 <td><a href="${studentRegistrationLink}">
                 <c:out value="${studentRegistration.fullname}"/></a></td>
            </tr>
		</c:forEach>
			
		
	</table>


	<ss:footer/>
  </body>
</html>

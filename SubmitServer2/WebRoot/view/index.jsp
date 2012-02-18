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
<!-- Don't redirect when there's only a single course; we need to show the list of courses to register for. -->
<c:if test="${false}">
    <c:if test="${singleCourse && instructorCapability}">
        <c:redirect url="/view/instructor/course.jsp">
            <c:param name="coursePK" value="${courseList[0].coursePK}" />
        </c:redirect>
    </c:if>
    <c:if test="${singleCourse}">
        <c:redirect url="/view/course.jsp">
            <c:param name="coursePK" value="${courseList[0].coursePK}" />
        </c:redirect>
    </c:if>
</c:if>
<!DOCTYPE HTML>
<html>
<head>
<ss:headContent title="Submit Server Home Page" />
<style>
label.error {
	display: block;
	color: red;
	font-weight: bold;
}
</style>
</head>
<body>
    <ss:header />
    <ss:breadCrumb />

    <div class="sectionTitle">
        <h1>Home</h1>
        <ss:hello/>
    </div>

    <c:set var="statusMap" value="${userSession.instructorStatus}" />
    <div id="enrolled-list">
        <h2>Courses Enrolled</h2>
        <c:choose>
            <c:when test="${empty courseList}">
                <p>Not registered for any courses</p>
                 <c:if test="${user.canImportCourses}">
                 <ul>
                <c:if test="${gradesServer}">
                    <c:url var="importCourseLink" value="/view/import/importCourse.jsp" />
                    <li><a href="${importCourseLink}">Import course from grade server</a></li>
                </c:if>
                    <c:url var="createCourseLink" value="/view/instructor/createCourse.jsp" />
                    <li><a href="${createCourseLink}">Create course via web form</a></li>
            </ul>
            </c:if>
            </c:when>
            <c:otherwise>
                <ul>
                    <c:forEach var="course" items="${courseList}">
                        <c:choose>
                            <c:when test="${user.superUser || statusMap[course.coursePK]}">
                                <c:set var="courseURL" value="/view/instructor/course.jsp" />
                                <li style="list-style-type: circle">
                            </c:when>
                            <c:otherwise>
                                <li><c:set var="courseURL" value="/view/course.jsp" />
                            </c:otherwise>
                        </c:choose>

                        <c:url var="courseLink" value="${courseURL}">
                            <c:param name="coursePK" value="${course.coursePK}" />
                        </c:url>
                        <a href="${courseLink}"> <c:out value="${course.fullDescription}" /> 
                        </a>
                    </c:forEach>

                </ul>
            </c:otherwise>
        </c:choose>
    </div>

    <c:if test="${not empty pendingRequests}">
        <div id="pending-list">
            <h2>Pending requests</h2>
            <table>
                <tr>
                    <th>Course Name</th>
                    <th>Course Description</th>
                </tr>
                <c:forEach var="course" items="${pendingRequests}">
                    <tr>
                        <td><c:out value="${course.fullname}" /></td>
                        <td><c:out value="${course.description}" /></td>
                        <!-- TODO(rwsims): Allow students to cancel registration requests? -->
                    </tr>
                </c:forEach>
            </table>
        </div>
    </c:if>

    <c:if test="${not empty openCourses}">
        <div id="open-list">
            <h2>Courses open for enrollment</h2>
            <c:url var="registrationAction" value="/action/RequestRegistration" />
            <form id="request-registration-form" method="POST" action="${registrationAction}">
                <table id="open-course-table">
                    <tr>
                        <th>&nbsp;</th>
                        <th>Course Name</th>
                        <th>Description</th>
                    </tr>
                    <c:forEach var="course" items="${openCourses}">
                        <tr>
                            <c:set var="checkboxName" value="course-pk-${course.coursePK}" />
                            <td>
                            <c:choose>
                            	<c:when test="${not empty course.sections}">
                            		<select name="${checkboxName}" id="${checkboxName}-box" required="required">
                            			<option value="">--section--</option>
                            			<c:forEach var="section" items="${course.sections}">
                            				<option value="${section}">${section}</option>
                            			</c:forEach>
                            		</select>
                            	</c:when>
                            	<c:otherwise>
                            		<input type="checkbox" name="${checkboxName}" id="${checkboxName}-box" />
                            	</c:otherwise>
                            </c:choose>
                            </td>
                            <td><label for="${checkboxName}-box"><c:out value="${course.fullname}" /></label></td>
                            <td><c:out value="${course.description}" />
								
							</td>
                        </tr>
                    </c:forEach>
                    <tr>
                        <td colspan="3">
                    	<input type="submit" value="Request enrollment" />
                    	</td>
                    </tr>
                </table>
            </form>
        </div>
    </c:if>

    <ss:footer />

	<script type="text/javascript" src="http://ajax.aspnetcdn.com/ajax/jquery.validate/1.9/jquery.validate.min.js"></script>
    <script type="text/javascript">
					window.$marmoset = {
						toggleAll : $("#toggle-all"),
						openCourseTable : $("#open-course-table")
					};
					$("#request-registration-form").validate({
						errorPlacement: function(error, element) {
							error.insertBefore(element);
						}
					});
					$("select", "#request-registration-form").rules("add", {
						required: true,
						messages: {
							required: "Please select a section."
						}
					});
				</script>
</body>
</html>

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

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions'%>
<!DOCTYPE HTML>
<html>

<ss:head title="Details of error ${error.errorPK}" />

<body>
    <ss:header />
    <ss:instructorBreadCrumb />

    <h1>Details of error ${error.errorPK}</h1>

    <p>
        When:
        <fmt:formatDate value="${error.when}" pattern="dd MMM, hh:mm a" />
        <br> kind:
        <c:out value="${errorField['kind']}" />

         <c:if test="${not empty error.userPK}">
         <br>
         <c:url var="userLink" value="/view/instructor/student.jsp">
            <c:param name="studentPK" value="${error.userPK}" />
        </c:url>
       <a href="${userLink}">User: ${error.userPK}</a>
       </c:if>
       
        <c:if test="${not empty errorField['student_pk']  && errorField['student_pk'] != 0 && errorField['user_pk'] != errorField['student_pk'] }">
            <c:url var="studentLink" value="/view/instructor/student.jsp">
            <c:param name="studentPK" value="${errorField['student_pk']}" />
        </c:url>
      <br><a href="${studentLink}">Student: ${errorField['student_pk']}</a>
    </c:if>
    <c:if test="${errorField['project_pk'] != 0}">
     <c:url var="projectLink" value="/view/instructor/project.jsp">
            <c:param name="projectPK" value="${errorField['project_pk']}" />
        </c:url>
        <br><a href="${projectLink}">Project: ${errorField['project_pk']}</a>
        </c:if>
        <c:if test="${not empty errorField['submission_pk']}}">
         <c:url var="submissionLink" value="/view/instructor/submission.jsp">
            <c:param name="submissionPK" value="${errorField['submission_pk']}" />
        </c:url>
            <br><a href="${submissionLink}">Submission: ${errorField['submission_pk']}</a>
    </c:if>
        <br> Code:
        <c:out value="${errorField['code']}" />
        <br> Message:
        <c:out value="${errorField['message']}" />
        <br> Type:
        <c:out value="${errorField['type']}" />
        <br> Servlet:
        <c:out value="${errorField['servlet']}" />
        <br> URI:
        <c:out value="${errorField['uri']}" />
        <c:if test="${not empty errorField['query_string'] }">
        <br> Query string:
        <c:out value="${errorField['query_string']}" />
        </c:if>
        <br> remote_host:
        <c:out value="${errorField['remote_host']}" />
        <c:if test="${not empty errorField['referer'] }">
        <br> referer:
        <c:out value="${errorField['referer']}" />
        </c:if>
         <c:if test="${not empty errorField['user_agent'] }">
        <br> User-Agent:
        <c:out value="${errorField['user_agent']}" />
        </c:if>
        <c:if test="${not empty errorField['throwable_as_string'] }">
        <br> throwable:
    <blockquote>
        <pre><c:out value="${errorField['throwable_as_string']}" /></pre>
    </blockquote>
    </c:if>



    <ss:footer />
</body>
</html>

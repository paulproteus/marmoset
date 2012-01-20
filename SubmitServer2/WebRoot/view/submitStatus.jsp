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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML>
<html>
<ss:head
	title="Submission information for ${course.courseName} project ${project.projectNumber}" />

<body>
<ss:header />
    <ss:breadCrumb />

<ss:projectTitle />

    <div class="sectionTitle">
        <h1>Submission info</h1>
        <p class="sectionDescription">Submission info for ${studentRegistration.fullname}</p>
    </div>

<c:set var="code">
<c:out value="${submitStatusCode}"/>
</c:set>
<p>Please copy the following information into your project submission tool where it asks 
for your submission verification information.
<p><input type="text" autofocus="autofocus" value="${code}"/>


<ss:footer />
</body>
</html>

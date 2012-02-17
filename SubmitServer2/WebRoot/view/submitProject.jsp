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
	title="Submit project ${project.projectNumber} for ${course.courseName} in ${course.semester}" />

<body>
  <c:url var="jsBase" value="/js" />
  <script src="${jsBase}/jquery.MultiFile.js" type="text/javascript"></script>
  
<ss:header />
<ss:breadCrumb />

<c:if test="${project.testSetupPK == 0 or testSetup.jarfileStatus != 'active'}">
<h2>
<font color=red><b>NOTE:</b></font></h2>
<p>This project is not yet active for automated testing of submissions.  This probably means
that the instructor has not yet uploaded a working reference implementation and assigned
point values to each test case.
<p>
You can still submit your implementation, but the server will not test your
submission until the project has been activated by the instructor.
</c:if>

<div class="sectionTitle">
	<h1>File Upload for Project Submission</h1>

	<p class="sectionDescription">Submit Project 
    <c:out value="${project.fullTitle}"/>
			for <c:out value="${course.fullname}"/></p>
</div>

    <c:if test="${course.allowsBaselineDownload && project.archivePK != null && project.archivePK > 0}">
                        <c:url var="downloadStarterFilesLink"
                            value="/data/DownloadProjectStarterFiles">
                            <c:param name="projectPK" value="${project.projectPK}" />
                        </c:url>
                        <a href="${downloadStarterFilesLink}">download baseline submission</a>
                    </c:if>
                    
<ss:submitProject/>

<ss:footer />
</body>
</html>

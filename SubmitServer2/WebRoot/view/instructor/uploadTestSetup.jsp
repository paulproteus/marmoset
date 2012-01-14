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
	title="Upload a new testing setup for projectPK ${project.projectPK} " />
<body>
<ss:header />
<ss:instructorBreadCrumb />

	<ss:projectTitle/>
	<ss:projectMenu/>

<h2>Upload a New Test Setup</h2>

<c:url var="uploadTestSetupLink"
	value="/action/instructor/UploadTestSetup" />

<form name="submitform" action="${uploadTestSetupLink}"
	enctype="multipart/form-data" method="POST"><input type="hidden"
	name="projectPK" value="${project.projectPK}">
<p>Comment:<br>
<textarea cols="40" rows="8" name="comment"></textarea>
<p>Zip/Jar file: <input type="file" name="file" size=40>
<p><input type="submit" value="Upload Project Jarfile">
</form>

<ss:footer />
</body>
</html>

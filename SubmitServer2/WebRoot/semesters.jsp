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
  <ss:head title="Submit Server Semester Index"/>
  <body>
  <ss:header/>
  <div class="sectionTitle">
  <h1>Submit Server Index</h1>
    
    <p class="sectionDescription">Welcome to <a href="${serviceURL}"><ss:brandingProperty key="branding.service.fullname" safeHtml="true" /></a>
 
</div>
  
 
 <h1>Pick a semester for the submit server you wish to access</h1>
   <c:url var="mainLink" value="/" />
     <c:url var="spring2012" value="/spring2012" />
      <c:url var="summer" value="/summer" />
 
                  
 <p>The submit server stores results for several semesters. Please select the semester that you want to access.
 You may wish to bookmark the page to get quicker access. 
 During the spring and fall semester, <c:out value="${mainLink}"/>
 shows the web page for the current semester.</p>
 <ul>
<li><a href="${spring2012}"><c:out value="${spring2012}"/></a> - Spring 2012
<li><a href="${summer}"><c:out value="${summer}"/></a> - current summer semester (2012)
 </ul>
 
  <ss:footer/>
  </body>
</html>
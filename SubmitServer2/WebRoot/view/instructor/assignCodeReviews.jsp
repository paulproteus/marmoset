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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions'%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<!DOCTYPE html>
<html>
<head>
<ss:headContent title="Assign code review ${codeReviewAssignment.codeReviewAssignmentPK}" />
<c:url var="jsBase" value="/js" />
<link href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.7/themes/base/jquery-ui.css" rel="stylesheet"
    type="text/css" />
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js" type="text/javascript"></script>
<script src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.7/jquery-ui.min.js" type="text/javascript"></script>
<link href="${jsBase}/timePicker.css" rel="stylesheet" type="text/css" />
<script src="${jsBase}/jquery.timePicker.js" type="text/javascript"></script>
<style type="text/css">
form {
    margin-right: 5em;
}
ul.form-fields > li > label {
    display: block;
}
ul.form-fields > li {
    margin-top: 1em;
}
ul.form-fields li:first-child {
    margin-top: 0px;
}
ul.form-fields ul.reviewer-list {
    margin-left: 2em;
}
form ul {
    list-style-type: none;
    padding: 0px;
    margin: 0px;
}
fieldset {
    border: 1px solid black;
    margin-bottom: 1em;
}
fieldset:first-child {
    margin-top: 1em;
}
fieldset h3 {
    margin: 0px;
}
</style>
</head>
<body>
    <ss:header />
    <ss:instructorBreadCrumb />

<!--
who does reviews
what they're reviewing 
-->    
    <c:url var="assignAction" value="/action/instructor/AssignCodeReviews" />
    <form method="POST" action="${assignAction}">
        <input type="hidden" name="codeReviewAssignmentPK" value="${codeReviewAssignment.codeReviewAssignmentPK}" />
        <fieldset>
        <h3>Reviewers</h3>
        <ul class="form-list">
        <c:choose>
        <c:when test="${codeReviewAssignment.type == 'instructionalPrototype'}" >
            <li>
                <input type="radio" name="sectional" id="yes-sectional" value="true">
                <label for="yes-sectional">Assign instructors by section:</label>
                <ul class="reviewer-list" id="sectional-reviewer-list">
                    <c:forEach var="section" items="${course.sections}">
                    <li>
                        <label for="section-reviewer-${section}">
                            <c:out value="${section}" />
                        </label>
                        <select name="section-reviewer-${section}" id="section-reviewer-${section}">
                            <c:forEach var="studentRegistration" items="${courseInstructors}" >
                            <c:if test="${empty studentRegistration.section || studentRegistration.section == section }">
                            <option value="${studentRegistration.studentPK}">
                                <c:out value="${studentRegistration.fullname}" />
                            </option>
                            </c:if>
                            </c:forEach>
                        </select>
                    </li>
                    </c:forEach>
                </ul>
            </li>
            <li>
                <input type="radio" name="sectional" id="no-sectional" value="false">
                <label for="no-sectional">Assign instructors by round-robin:</label>
                <ul class="reviewer-list" id="global-reviewer-list">
                    <c:forEach var="studentRegistration" items="${courseInstructors}">
                    <li>
                        <input type="checkbox" id="instructional-reviewer-${studentRegistration.studentPK}" name="reviewer-${studentRegistration.studentPK}">
                        <label for="instructional-reviewer-${studentRegistration.studentPK}"><c:out value="${studentRegistration.fullname}" /></label>
                    </li>
                    </c:forEach>
                </ul>
            </li>
            </c:when>
            <c:otherwise>
                <li>
                <label>Assign peer reviews:</label>
                    <ul>
                        <li>
                            <input type="radio" name="peerBySection" value="true" id="yes-by-section" checked="checked"/>
                            <label for="yes-by-section">Only within a section</label>
                        </li>
                        <li>
                            <input type="radio" name="peerBySection" value="false" id="not-by-section"/>
                            <label for="not-by-section">Across sections</label>
                        </li>
                    </ul>
                </li>
                <li>
                <label for="reviews-per-submission">Reviews per submission:</label>
                <select name="numReviewers" id="reviews-per-submission">
                    <option>1</option>
                    <option>2</option>
                    <option>3</option>
                </select>
            </li>
            </c:otherwise>
            </c:choose>
        </ul>
        <h3>Code to review</h3>
        </fieldset>
        <fieldset>
        <h3>Warning!</h3>
        <p>Assigning a review is irreversible, and the review cannot be edited once it is assigned.</p>
        <div style="text-align: right">
        <button id="assign-reviews">Assign</button>
        </div>
        </fieldset>
    </form>
    
    <script type="text/javascript">
    var $sectionalDropdowns = $("#sectional-reviewer-list select");
    var $globalDropdowns = $("#global-reviewer-list select");
    $("#yes-sectional").click(function() {
    	$sectionalDropdowns.removeAttr('disabled');
    	$globalDropdowns.attr('disabled', 'disabled');
    });
    $("#no-sectional").click(function() {
    	$sectionalDropdowns.attr('disabled', 'disabled');
    	$globalDropdowns.removeAttr('disabled');
    });
    
    $(document).ready(function() {
    	$("#assign-reviews").button();
    });
    </script>
</body>
</html>
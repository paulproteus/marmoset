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
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions'%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>


<!DOCTYPE HTML>
<html>
<head>

<ss:headContent title="Create code review for Project ${project.fullTitle}" />

<c:url var="jsBase" value="/js" />

<link href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.7/themes/base/jquery-ui.css" rel="stylesheet"
    type="text/css" />
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js" type="text/javascript"></script>
<script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.7/jquery-ui.min.js" type="text/javascript"></script>
<link href="${jsBase}/timePicker.css" rel="stylesheet" type="text/css" />
<script src="${jsBase}/jquery.timePicker.js" type="text/javascript"></script>

<script type="text/javascript">
	$(function() {

	});
</script>

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
ul#rubric-list > li {
    margin-top: 1em;
    border: 1px solid black;
    padding: 0.5em;
    overflow: auto;
    width: 90%;
}
div.rubric-editing {
    float: left;
}
div.rubric-row-controls {
    float: right;
}
div.rubric-editing label:first-child {
    width: 5em;
    display: inline-block;
}
li.deleted-rubric {
    background-color: #e66;
}
</style>

</head>

<body>
    <ss:header />
    <ss:instructorBreadCrumb />

    <div class="sectionTitle">
        <h1>
            New Code Review for project
            <c:out value="${project.projectNumber}" />
        </h1>





        <p class="sectionDescription">
            Fill out the following form to create a new prototype code review for project
            <c:out value="${project.fullTitle}" />.
        </p>
    </div>
    
    <c:choose>
<c:when test="${user.superUser}">
<p>You can't create a code review as a superuser; you need to do so using an instructor account
</p></c:when>
<c:otherwise>

<c:if test="${empty codeReviewAssignment}">
<p>
    Code reviews are initially created as prototypes. For an instructional code review, you will be the
    only code reviewer. For an peer code review, your pseudo-student account will be assigned to perform
    the code review. In either case, you pick a submission from one non-student account (either an
    instructional or pseudo-student account) to review. The reviewer and the author must be
    different.
    
<c:if test="${!hasOtherStaffSubmissions}"> 
    <c:url var="studentAccountForInstructorLink" value="/action/instructor/StudentAccountForInstructor" />
<form action="${studentAccountForInstructorLink}" method="post" name="studentAccountForInstructorForm">
<p>
    To create a prototype instructional code review, there
    must be a submission by some non-student account other than your own instructional account.
    No such submission exists. 
        <input type="submit" value="Become your pseudo-student">
    </p>

</form>
  </c:if>

<p>While the code review is in prototype form, you can try actually doing the code review. You can
    also add, edit and remove rubrics. When you are happy with the code review, you can assign it, which
    does the actually assignment of reviewers to submissions. Once a review has been assigned, you can't
    edit the rubrics or assignments.</p>

</c:if>
            <c:url value="/action/instructor/CreateCodeReviewAssignment" var="createAssignmentAction" />
<form action="${createAssignmentAction}" method="POST" id="code-review-creation">
    <input type="hidden" name="coursePK" value="${course.coursePK}" />
    <input type="hidden" name="projectPK" value="${project.projectPK}" />
    <c:if test="${not empty codeReviewAssignment}" >
    <input type="hidden" name="codeReviewAssignmentPK" value="${codeReviewAssignment.codeReviewAssignmentPK}" />
    </c:if>
    <fieldset id="basic-review-info">
    <h3>Review Information</h3>
        <ul class="form-fields">
        <c:if test="${empty codeReviewAssignment}">
            <li>
                <label>Review kind:</label>
                  
                <ul>
                  <c:choose>
                    <c:when test="${hasOtherStaffSubmissions}">
                    
                    <li><input type="radio" name="kind" id="instructional-kind" value="instructionalPrototype"
                        ${ss:checkedOrNull(codeReviewAssignment.type, "instructionalPrototype") } 
                        />
                        <label for="instructional-kind">Instructional</label>
                    </li>
                    <li>
                        <input type="radio" name="kind" id="peer-kind" value="peerPrototype" 
                        ${ss:checked(codeReviewAssignment.type, "peerPrototype") }
                        />
                        <label for="peer-kind">Peer</label>
                    </li>
                    </c:when>
                    <c:otherwise>
                    <li><input type="radio" name="kind" id="instructional-kind" value="instructionalPrototype"
                        disabled="disabled"
                        />
                        <label for="instructional-kind">Instructional review not available due to lack of appropriate submission to review
              
                </label>
                    </li>
                    <li>
                        <input type="radio" name="kind" id="peer-kind" value="peerPrototype" 
                        checked="checked"
                        />
                        <label for="peer-kind">Peer</label>
                    </li>
                    </c:otherwise>
                    </c:choose>
                </ul>
            </li>
	    </c:if>
	        <li>
	           <label for="code-review-description">Description:</label>
	           <c:set var="assignmentDescription">
	               <c:out value="${codeReviewAssignment.description}" />
	           </c:set>
	           <input type="text" size="60" required="required" name="description" id="code-review-description" 
	           value="${codeReviewAssignment.description}"/>
	        </li>
	        <li>
	           <label for="codereview-deadline-date">Deadline:</label>
	           <c:set var="deadlineDate">
	               <fmt:formatDate value="${codeReviewAssignment.deadline}" pattern="yyyy-MM-dd"/>
	           </c:set>
	           <input type="date" id="codereview-deadline-date" name="deadline-date" placeholder="yyyy-mm-dd" size="12" required="required" 
	           value="${deadlineDate}"/>
	           
	           <c:set var="deadlineTime">
	               <fmt:formatDate value="${codeReviewAssignment.deadline}" pattern="hh:mm aa" />
	           </c:set>
	           <input type="time" id="codereview-deadline-time" name="deadline-time" placeholder="hh:mm aa" size="12" title="leave blank for 1 second before midnight"
	            value="${deadlineTime}"/>
	        </li>
	        <c:if test="${empty codeReviewAssignment || codeReviewAssignment.kind == 'PEER_PROTOTYPE'}">
            <li id="anonymity-info">
                <label>Anonymous:</label>
                    <ul>
                        <li>
                            <input type="radio" name="anonymous" value="true" id="yes-anonymous"/>
                            <label for="yes-anonymous">Yes</label>
                        </li>
                        <li>
                            <input type="radio" name="anonymous" value="false" id="no-anonymous" checked="checked"/>
                            <label for="no-anonymous">No</label>
                        </li>
                    </ul>
            </li>
            <li id="visibility-info">
                <label>Other reviewers' comments:</label>
                     <ul>
                        <li>
                            <input type="radio" name="canSeeOthers" value="true" id="can-see-others"/>
                            <label for="can-see-others">Visible</label>
                        </li>
                        <li>
                            <input type="radio" name="canSeeOthers" value="false" id="cant-see-others" checked="checked"/>
                            <label for="cant-see-others">Hidden</label>
                        </li>
                    </ul>
            </li>
            </c:if>
        </ul>
    </fieldset>
    <c:if test="${empty codeReviewAssignment}">
    <fieldset id="code-for-instructional-review">
    <h3>Submission for Prototype Instructional Review</h3>
        <ul class="form-fields">
            <li>
            <c:choose>
            <c:when test="${hasOtherStaffSubmissions}">
            
                <label for="exemplar-submission">Code for prototype instructional review:</label>
                <select name="of-instructional" id="exemplar-submission-instructional">
                    <c:forEach var="studentRegistration" items="${staffStudentSubmissions}">
                    <c:if test="${studentRegistration.studentPK != user.studentPK || studentRegistration.pseudoStudent }">
                    <option value="${lastSubmission[studentRegistration.studentRegistrationPK].submissionPK}">
                        <c:out value="${studentRegistration.fullname}" />
                    </option>
                    </c:if>
                    </c:forEach>
                </select>
                <p>
                The code review will initially be created as a prototype, and you will be assigned to review the
                 submission from the account selected above.
                
                </p>
                </c:when>
                <c:otherwise>
                 <p>
             
                </p>
                </c:otherwise>
                </c:choose>
            </li>
        </ul>
    </fieldset>
     <fieldset id="code-for-peer-review">
    <h3>Submission to Review</h3>
        <ul class="form-fields">
            <li>
                <label for="exemplar-submission">Code for prototype peer review:</label>
                <select name="of-peer" id="exemplar-submission-peer">
                    <c:forEach var="studentRegistration" items="${staffStudentSubmissions}">
                     <c:if test="${studentRegistration.studentPK != user.studentPK || !studentRegistration.pseudoStudent}">
                   
                    <option value="${lastSubmission[studentRegistration.studentRegistrationPK].submissionPK}">
                        <c:out value="${studentRegistration.fullname}" />
                    </option>
                    </c:if>
                    </c:forEach>
                </select>
                <p>
                  The code review will initially be created as a prototype, and your pseudo-student account will be assigned to review the
                 submission from the account selected above.
                </p>
            </li>
        </ul>
    </fieldset>
    
    </c:if>
    <fieldset id="review-rubrics">
    <h3>Rubrics</h3>
    <div id="rubric-controls">
        <button id="add-numeric-rubric" type="button">Add Numeric</button>
        <button id="add-dropdown-rubric" type="button">Add Dropdown</button>
        <button id="add-checkbox-rubric" type="button">Add Checkbox</button>
    </div>
    <input type="hidden" name="rubric-count" id="rubric-count" value="${fn:length(rubrics)}" />
    <ul id="rubric-list">
            <c:forEach var="rubric" items="${rubrics}" varStatus="rubricStatus">
            <c:set var="presentationName">
                    <c:choose>
                    <c:when test="${rubric.presentation == 'NUMERIC'}">
                    Numeric
                    </c:when>
                    
                    <c:when test="${rubric.presentation == 'DROPDOWN'}">
                    Dropdown
                    </c:when>
                    
                    <c:when test="${rubric.presentation == 'CHECKBOX'}">
                    Checkbox
                    </c:when>
                </c:choose>
            </c:set>
                <c:set var="prefix" value="rubric-${rubricStatus.count}" />
                <li id="${prefix}">
                <input type="hidden" name="${prefix}-presentation" value="${rubric.presentation}" />
                <input type="hidden" name="${prefix}-pk" value="${rubric.rubricPK}" />
                <div class="rubric-editing">
                <div>
                <label for="${prefix}-name">${presentationName}: </label>
                <input type="text" id="${prefix}-name" name="${prefix}-name" size="20" required="required" placeholder="Name of rubric item"
                        value="${rubric.name}"/>
                <input type="text" name="${prefix}-description" size="50" placeholder="a longer description of this rubric item" value="${rubric.description}"/>
                </div>
                <div>
                <label>Edit points:</label>
                <c:choose>
                    <c:when test="${rubric.presentation == 'NUMERIC'}">
                    <label for="${prefix}-min-input">min:</label>
                    <input type="number" name="${prefix}-min" id="${prefix}-in-input" placeholder="min"  size="4" value="${not empty rubric.dataAsMap['min'] ? rubric.dataAsMap['min'] : 0}">

		            <label for="${prefix}-max-input">max:</label>
		            <input type="number" name="${prefix}-max" id="${prefix}-max-input" placeholder="max"  size="4" value="${rubric.dataAsMap['max']}">

		            <label for="${prefix}-default-input">default:</label>
		            <input type="number" name="${prefix}-default" id="${prefix}-default-input" placeholder="default"  size="4" value="${rubric.dataAsMap['default']}">
                    </c:when>
                    
                    <c:when test="${rubric.presentation == 'DROPDOWN'}">
                    <input type="hidden" name="${prefix}-value" id="${prefix}-hidden" value="${rubric.data}"/>
                    <select id="${prefix}-select">
                    <c:forEach var="data" items="${rubric.dataAsMap}">
                        <option>${data.key} [${data.value}]</option>
                    </c:forEach>
                    </select>
                    <button id="${prefix}-edit-button" type="button">edit dropdown</button>
                    </c:when>
                    
                    <c:when test="${rubric.presentation == 'CHECKBOX'}">
                    <input type="checkbox" value="ignore" onclick="return false;"  >
		            <input type="number" name="${prefix}-false" title="value if not checked" size="4" required="required"
		              value="${not empty rubric.dataAsMap['false'] ? rubric.dataAsMap['false'] : 0}">
		            &nbsp;
		            <input type="checkbox" value="ignore"   onclick="return false;"  CHECKED >
		            <input type="number" name="${prefix}-true" title="value if checked"  size="4" required="required"
		              value="${rubric.dataAsMap['true']}">
                    </c:when>
                </c:choose>
                </div>
                </div>
                <div class="rubric-row-controls">
                    <input type="checkbox" id="${prefix}-delete" name="${prefix}-delete" value="true" class="delete-toggle"/>
                    <label for="${prefix}-delete">mark for deletion</label>
                </div>
            </li>
        </c:forEach>
    </ul>
    </fieldset>
    <div style="text-align: right">
        <c:choose>
        <c:when test="${empty codeReviewAssignment}" >
        <button id="create-code-review">Create Prototype</button>
        </c:when>
        <c:otherwise>
        <button id="create-code-review">Update Prototype</button>
        </c:otherwise>
        </c:choose>
    </div>
</form>






            <script type="text/javascript">
	function updateForm(kind) {
		console.log('code review kind: "' + kind + '"');
		switch (kind) {
		case "instructionalPrototype":
			hideItem("anonymity-info");
			hideItem("visibility-info");
			hideItem("code-for-peer-review");
			showItem("code-for-instructional-review");
            break;
		case "peerPrototype":
			showItem("anonymity-info");
			showItem("visibility-info");
			showItem("code-for-peer-review");
			hideItem("code-for-instructional-review");
            break;
		default:
			console.log("Unknown kind");
		}
	}
	
	var $kind = $("input:radio[name=kind]");
	$kind.click(function(event) {
		updateForm(this.value);
	});

	$(document).ready(function() {
		$('#codereview-deadline-date').datepicker({
			minDate : -1,
			defaultDate : +7,
			dateFormat : "yy-mm-dd",
			onSelect : function(selectedDate) {
				$("#codereview-deadline-time").focus();
			}
		});

		$("#codereview-deadline-time").timePicker({
			show24Hours : false,
			step : 60,
			defaultTime : "06:00 PM"
		});
		
		$("#rubric-controls").buttonset();
		$("#create-code-review").button();
		updateForm($kind.filter(":checked").val());
		
		$("input.delete-toggle")
		      .button()
		      .change(function() {
		    	    $(this).parents("li").toggleClass('deleted-rubric');
		       });
	});
</script>
    <ss:script file="jsrender.js" />
    <script id="rubricTemplate" type="text/x-jquery-tmpl">
            <li id="rubric-{{=count}}">
                <input type="hidden" name="{{=prefix}}-presentation" value="{{=presentation}}" />
                <div class="rubric-editing">
                <div>
                <label for="{{=prefix}}-name">{{=header}}: </label>
                <input type="text" id="{{=prefix}}-name" name="{{=prefix}}-name" size="20" required="required" placeholder="Name of rubric item"/>
                <input type="text" name="{{=prefix}}-description" size="50" placeholder="a longer description of this rubric item"/>
                </div>
                <div>
                <label>Edit points:</label>
                {{=editWidgets!}}
                </div>
                </div>
                <div class="rubric-row-controls">
                   <button type="button" id="{{=prefix}}-delete" value="rubric-{{=count}}">delete</button>
                </div>
            </li>
        </script>

    <script id="dropdownTemplate" type="text/x-jquery-tmpl">
            <input type="hidden" name="{{=prefix}}-value" id="{{=prefix}}-hidden"/>
            <select id="{{=prefix}}-select"></select>
            <button id="{{=prefix}}-edit-button">edit dropdown</button>
        </script>
    <script id="numericTemplate" type="text/x-jquery-tmpl">
            <label for="{{=prefix}}-min-input">min:</label>
            <input type="number" name="{{=prefix}}-min" id="{{=prefix}}-in-input" placeholder="min"  size="4">

            <label for="{{=prefix}}-max-input">max:</label>
            <input type="number" name="{{=prefix}}-max" id="{{=prefix}}-max-input" placeholder="max"  size="4">

            <label for="{{=prefix}}-default-input">default:</label>
            <input type="number" name="{{=prefix}}-default" id="{{=prefix}}-default-input" placeholder="default"  size="4">
        </script>
    <script id="checkboxTemplate" type="text/x-jquery-tmpl">
            <input type="checkbox" value="ignore" onclick="return false;"  >
            <input type="number" name="{{=prefix}}-false" title="value if not checked" size="4" value="0" required="required">
            &nbsp;
            <input type="checkbox" value="ignore"   onclick="return false;"  CHECKED >
            <input type="number" name="{{=prefix}}-true" title="value if checked"  size="4" required="required">
        </script>

    <div id="edit-dialog" style="display: none">
        <select id="edit-dialog-dropdown-select"></select>
        <div>
            <input type="text" id="edit-dialog-value-input" placeholder="name" /> <input type="text"
                id="edit-dialog-score-input" placeholder="score" />
        </div>
        <div>
            <span id="edit-dialog-controls">
                <button id="edit-dialog-add">add</button>
                <button id="edit-dialog-delete">delete</button>
                <button id="edit-dialog-clear-all">clear all</button>
            </span>
        </div>
    </div>

    <ss:script file="rubrics.js" />
    <script type="text/javascript">
		var dropdownEditor = new marmoset.DropdownEditor("#edit-dialog");
		var manager = new marmoset.RubricManager(parseInt($("#rubric-count").val()),
				"#rubric-list",
				dropdownEditor);
		manager.setAddDropdownButton("#add-dropdown-rubric");
		manager.setAddNumericButton("#add-numeric-rubric");
		manager.setAddCheckboxButton("#add-checkbox-rubric");
		$(manager).one('change', function(event) {
			$("#rubricTable").show();
		});
		$(manager).change(function(event) {
			$("#rubric-count").val(manager.rubricCount);
		});
	</script>
    
  </c:otherwise>
  </c:choose>

    <ss:footer />
</body>
</html>

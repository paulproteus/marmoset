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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

    <c:set var="qChar">"</c:set>
    <c:set var="qSymb">&quot;</c:set>
     
<!DOCTYPE HTML>
<html>
<head>
<c:url var="jsBase" value="/js" />

<link
    href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.7/themes/base/jquery-ui.css"
    rel="stylesheet" type="text/css" />
<script
    src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.7/jquery-ui.min.js"
    type="text/javascript"></script>
 <link href="${jsBase}/timePicker.css"  rel="stylesheet" type="text/css" />
<script src="${jsBase}/jquery.timePicker.js" type="text/javascript"></script>

<script type="text/javascript">
$(function() {
    $( "#ontime-date" ).datepicker({
        defaultDate: +20,
        dateFormat: "yy-mm-dd",
        onSelect: function( selectedDate ) {
            var instance = $( this ).data( "datepicker" ),
                date = $.datepicker.parseDate(
                    instance.settings.dateFormat ||
                    $.datepicker._defaults.dateFormat,
                    selectedDate, instance.settings );
            $( "#late-date" ).datepicker( "option", "minDate", date );
            $( "#ontime-time").focus();
        }
    });
   $( "#late-date" ).datepicker({
       defaultDate: +20,
       dateFormat: "yy-mm-dd",
       onSelect: function( selectedDate ) {
           var instance = $( this ).data( "datepicker" ),
               date = $.datepicker.parseDate(
                   instance.settings.dateFormat ||
                   $.datepicker._defaults.dateFormat,
                   selectedDate, instance.settings );
           $( "#ontime-date" ).datepicker( "option", "maxDate", date );
           $( "#late-time").focus();
       }
   });
});


function changedIsTested(isTested)
{
    if (isTested) {
        document.getElementById("tested").style.display = "block";
    } else {
        document.getElementById("tested").style.display = "none";
    }
}

  $(function() {
    
    $("#ontime-time").timePicker({
          show24Hours: false,
          step: 60 });
    $("#late-time").timePicker({
          show24Hours: false,
          step: 60 });
    
  });
</script>
<ss:headContent title="Update project ${project.projectNumber} for ${course.courseName}" />
</head>
<body>
<ss:header />
<ss:instructorBreadCrumb />

<div class="sectionTitle">
	<h1>Update Project</h1>

	<p class="sectionDescription">Fill out the following form to
	update project 
    <c:out value="${project.projectNumber}"/> for 
    <c:out value="${course.fullname}"/></p>
</div>

<p>

<form class="form"
	action="<c:url value="/action/instructor/UpdateProject"/>"
	method="POST" name="updateProjectForm">

	<input type="hidden" name="coursePK" value="${course.coursePK}"/>
	<input type="hidden" name="projectPK" value="${project.projectPK}"/>
	<input type="hidden" name="visibleToStudents" value="${project.visibleToStudents}"/>
	<input type="hidden" name="testSetupPK" value="${project.testSetupPK}"/>
	<input type="hidden" name="previousInitialBuildStatus" value="${project.initialBuildStatus}"/>
	<input type="hidden" name="archivePK" value="${project.archivePK}"/>

<c:set var="testItemStyle" value="${project.tested ? 'display: block' : 'display: none' }"/>
<table class="form">
 <tfoot>
       <tr class="submit">
        <td colspan=2 align=center><input type=submit value="Update Project"></td>
    </tr>
    </tfoot>
<tbody>
	<tr title="should be very short; doesn't have to be a number, probably shouldn't have spaces">
		<td class="label">project Number</td>
		<td class="input"><INPUT TYPE="text" NAME="projectNumber" value="${project.projectNumber}" size="8" REQUIRED></td>
	</tr>
	<tr>
		<td class="label">on-time deadline</td>
		 <c:set var="ontimeDate"><fmt:formatDate value="${project.ontime}" pattern="dd MMM" />
	</c:set>
	    <c:set var="ontimeTime"><fmt:formatDate value="${project.ontime}" pattern="hh:mm a" />
    </c:set>
    <c:if test="${ontimeTime == '11:59 PM'}">
     <c:set var="ontimeTime" value=""/>
     </c:if>
    <td>  <INPUT TYPE="text" id="ontime-date" NAME="ontime-date" 
	    PLACEHOLDER="yyyy-mm-dd" size="12" required value="${ontimeDate}" />
    <INPUT TYPE="text" id="ontime-time" NAME="ontime-time" PLACEHOLDER="hh:mm aa" size="12"
     value="${ontimeTime}" title="blank for one second before midnight" > </td>
	
	</tr>
	<tr title="leave both blank for no late deadline">
        <td class="label">late deadline</td>
         <c:set var="lateDate"><fmt:formatDate value="${project.late}" pattern="dd MMM" />
    </c:set>
        <c:set var="lateTime"><fmt:formatDate value="${project.late}" pattern="hh:mm a" />
    </c:set>
    <c:if test="${lateTime == '11:59 PM'}">
     <c:set var="lateTime" value=""/>
     </c:if>
     <c:if test="${project.ontime == project.late }">
     <c:set var="lateDate" value=""/>
     <c:set var="lateTime" value=""/>
     </c:if>
     
    <td>  <INPUT TYPE="text" id="late-date" NAME="late-date" 
        PLACEHOLDER="yyyy-mm-dd" size="12" required value="${lateDate}" />
    <INPUT TYPE="text" id="late-time" NAME="late-time" PLACEHOLDER="hh:mm aa" size="12"
     value="${lateTime}"  title="blank for one second before midnight">
     </td>
    
    </tr>
    

		<tr>
		<td class="label"><b>project title</b></td>
		<td class="input"><INPUT TYPE="text" NAME="title" VALUE="${fn:replace(project.title,qChar,qSymb)}" size="60"></td>
	</tr>
	<tr>
		<td class="label"><b>URL</b></td>
		<td class="input"><INPUT TYPE="url" NAME="url" VALUE="${fn:replace(project.url,qChar,qSymb)}" size="60"></td>
	</tr>
	<tr>
		<td class="label"><b>description</B></td>
		<td class="input"><INPUT TYPE="text" NAME="description" VALUE="${fn:replace(project.description,qChar,qSymb)}" size="60"></td>
	</tr>
        <tr>
    <td class="label">Submission editing in browser</td>
    <td class="input">
         <select name="browserEditing">
            <option value="prohibited"  ${ss:selected(project.browserEditing, 'PROHIBITED')}>Prohibited</option>
            <option value="discouraged"  ${ss:selected(project.browserEditing, 'DISCOURAGED')}>Discouraged</option>
            <option value="allowed"  ${ss:selected(project.browserEditing, 'ALLOWED')}>Allowed</option>
            </select>
            </td>
    </tr>
	<tr 
    title="Submissions from the canoonical account are used to check test setups and to provide baseline/starter snapshots of projects">
    
        <td class="label">canonical account</td>
        <td><select name="canonicalStudentRegistrationPK">
            <c:forEach var="studentRegistration" items="${courseInstructors}">
                <c:choose>
                    <c:when test="${studentRegistration.studentRegistrationPK == project.canonicalStudentRegistrationPK}">
                    <option value="${studentRegistration.studentRegistrationPK}" selected="selected">
                    <c:out value="${studentRegistration.fullname}"/> </option>
                    </c:when>
                    <c:otherwise>
                    <option value="${studentRegistration.studentRegistrationPK}">
                    <c:out value="${studentRegistration.fullname}"/> </option>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </select>
        </td>
    </tr>
        <tr title="Student submissions are compared against either the baseline submission for this project, or against the students last submission from another project. The comparison is used to highlight changes text, and elide long sections of unchanged files. ">
    <td class="label">Diff against
        </td>
        <td>
        <select>
        <option value="0" ${ss:selected(project.diffAgainst,0)} >Baseline submission (if any)</option>
        <c:forEach var="p" items="${projectList}">
        <c:if test="${p.visibleToStudents and p.projectPK != project.projectPK}">
        <option value="${p.projectPK}"  ${ss:selected(project.diffAgainst,p.projectPK)} ><c:out value="${p.projectNumber}"/>:
<c:out value="${p.title}"/></option>
        </c:if>
        </c:forEach>
        </select>
 	<tr>
		<td class="label">Pair project<br>
		</td>
		<td class="input"><INPUT TYPE="CHECKBOX" NAME="pair" ${ss:isChecked(project.pair)} ></td>
	</tr>

    <tr>
        <td class="label">Tested on server<br>
        </td>
        <td><INPUT TYPE="CHECKBOX" NAME="tested" ${ss:isChecked(project.tested)} 
          ONCHANGE="changedIsTested(this.checked)"
        ></td>
    </tr>
    </tbody>
   
    
    </table>

<div id="tested" display="${testItemStyle}"> 
<table class="form">
<thead>
  <tr  class="submit">
        <th colspan=2>Test options</th>
    </tr>
    </thead>
	<tr title="Set to 'everything' if you want  students to see all outcomes (public, release and secret)  after the deadline passes.">
    	<td class="label">
			Post-Deadline Outcome Visibility
		</td>
		
		<td class="input">
		<input type="radio" name="postDeadlineOutcomeVisibility" value="everything" ${ss:checkedOrNull(project.postDeadlineOutcomeVisibility,'everything')}> Everything<br>
		<input type="radio" name="postDeadlineOutcomeVisibility" value="nothing" ${ss:checked(project.postDeadlineOutcomeVisibility,'nothing')}> Nothing<br>
		</td>
	</tr>

    <tr title="How much information to reveal for a release test">
		<td  class="label">stack trace policy
		</td>
		<td  class="input">
			<input type="radio" name="stackTracePolicy" value="test_name_only" ${ss:checkedOrNull(project.stackTracePolicy,'test_name_only')} >
			name of test only (default)<br>
			<input type="radio" name="stackTracePolicy" value="exception_location" ${ss:checked(project.stackTracePolicy,'exception_location')}>
			the line number in student's code where a runtime exception happens<br>
			<input type="radio" name="stackTracePolicy" value="restricted_exception_location" ${ss:checked(project.stackTracePolicy,'restricted_exception_location')}>
			(Java-only) the line number in student's code where a runtime exception happens, if it's covered by a student or public test<br>
			<input type="radio" name="stackTracePolicy" value="full_stack_trace" ${ss:checked(project.stackTracePolicy,'full_stack_trace')}>
			the entire stack trace for Java or full output printed to stdout for C<br>
		</td>
	</tr>

<tr title="which submissions can be release tested">
        <td class="label">release test policy
        </td>
		<td  class="input">
		  	<input type="radio" name="releasePolicy" value="after_public"
			${ss:checkedOrNull(project.releasePolicy,'after_public')}>
			submissions that pass all public tests<br>
			<input type="radio" name="releasePolicy" value="anytime"
			${ss:checked(project.releasePolicy,'anytime')}>
			any submission
	</tr>

	<tr>
		<td  class="label"><b>graded submission policy</b></td>
		<td  class="input">
			<input type="radio" name="bestSubmissionPolicy" value="edu.umd.cs.submitServer.policy.ChooseLastSubmissionPolicy"
			${ss:checkedOrNull(project.bestSubmissionPolicy,'edu.umd.cs.submitServer.policy.ChooseLastSubmissionPolicy')}>
				The last compilable submission.<br>
			<input type="radio" name="bestSubmissionPolicy" value="edu.umd.cs.submitServer.policy.ChooseHighScoreSubmissionPolicy"
			${ss:checked(project.bestSubmissionPolicy,'edu.umd.cs.submitServer.policy.ChooseHighScoreSubmissionPolicy')}>
				High score: Grade highest scoring submission.<br>
				<input type="radio" name="bestSubmissionPolicy" value="edu.umd.cs.submitServer.policy.ReleaseTestAwareSubmissionPolicy"
			${ss:checked(project.bestSubmissionPolicy,'edu.umd.cs.submitServer.policy.ReleaseTestAwareSubmissionPolicy')}>
				Release Test Aware: Either last compilable submission, or highest scoring release tested submission, whichever is higher.<br>
			<!--
			Note that if a project has secret tests, students will not know which
			submission is their "best" one.
			-->
		</td>
	</tr>
    <tr  title="How many release tests to reveal the students when they use a token">
        <td class="label"># release tests to reveal<br>
		<td class="input">
			<select name="numReleaseTestsRevealed">
			<option value="-1">all of them</option>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,1)}>1</OPTION>
			<OPTION ${ss:selectedOrNull(project.numReleaseTestsRevealed,2)}>2</OPTION>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,3)}>3</OPTION>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,4)}>4</OPTION>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,5)}>5</OPTION>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,6)}>6</OPTION>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,7)}>7</OPTION>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,8)}>8</OPTION>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,9)}>9</OPTION>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,10)}>10</OPTION>
			</select>
		</td>
	</tr>
	<tr>
		<td  class="label"><b># release tokens</b></td>
		<td class="input"><INPUT TYPE="text" NAME="releaseTokens" VALUE="${project.releaseTokens}"></td>
	</tr>

	<tr>
		<td class="label"><b>regeneration time</b> (hours)</td>
		<td class="input"><INPUT TYPE="text" NAME="regenerationTime" VALUE="${project.regenerationTime}"></td>
	</tr>


	<tr>
		<td class="label"><b>kind of late penalty</b></td>
		<td class="input"><SELECT name="kindOfLatePenalty">
			<option ${ss:selectedOrNull(project.kindOfLatePenalty,'constant')}>constant</option>
			<option ${ss:selected(project.kindOfLatePenalty,'multiplier')}>multiplier</option>
			</SELECT>
		</td>
	</tr>
	<tr>
		<td class="label">
			<b>Late Constant</b><br>
			How many points	to subtract<br>
			from a late submission
		</td>
		<td class="input"><input type="text" name="lateConstant" value="${project.lateConstant}" /></td>
	</tr>

	<tr>
		<td class="label">
			<b>Late Multiplier</b><br>
			Fraction by which to multiply<br>
			a late submission
		</td>
		<td class="input"><input type="text" name="lateMultiplier" value="${project.lateMultiplier}" /></td>
	</tr>

</tbody>   

</table>
</div>


</form>
<ss:footer />

</body>
</html>

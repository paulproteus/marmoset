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
	title="Submit Server User Feedback Survey" />

<body>
<ss:header />
<ss:breadCrumb />

<div class="sectionTitle">
	<h1>Submit Server User Feedback Survey: Tell us what you think!</h1>
	
	<p class="sectionDescription">The server has been giving you feedback all
	semester; this is your chance to give back some feedback of your own!  Feel free to be
	brutally honest; that's the only way we're going to learn what students
	really think about the system.</p>
</div>

<p>
This survey goes directly to <b>the administrator of the Submit Server</b>.
The survey information is primarily useful to the maintainers of the
Submit Server to improve the system and to do better research into
the the student programming environment and experience.
Your instructors and TAs won't see any survey results until after the semester is over,
if they see them at all, and this will not in any way affect your grade.
<p>
But, please give me useful feedback (i.e. "The
submit server is evil" 
is not useful; "The submit server could be
greatly improved by adding feature X" is useful because then we have something
specific to look into).  Thanks very much!
<br>

<p>
<p>

<!--
NOTE:  The results of this survey are <i>anonymous</i>.  I know that
you've authenticated, but the only data stored that's bound to your
name is whether or not you filled out a survey, not <i>which</i>
survey was yours.
-->

<form action="/action/ProcessStudentSurveyResults" method="POST">
<input type="hidden" name="student_pk__survey" value="${student.studentPK}">
<input type="hidden" name="course_pk__survey" value="${course_pk}"/>
<input type="hidden" name="semester_administered" value="Spring2006"/>

<b>Important</b>: Do you want your survey results to be <i>anonymous</i>, or do you want
the server maintainers to know who you are?  If you don't choose, the default is
<i>anonymous</i>.
<p>
<input type="radio" name="anonymous" value="true"> I want my
survey results to be <i>anonymous</i>.
<p>
<input type="radio" name="anonymous" value="false"> I want the server maintainers
to know who I am so they can follow up with me on my comments, concerns or suggestions.
<p>

<p>

<table border=2>

<tr>
  <td class="description">
  When was the first semester that you used the Submit Server?
  </td>
  <td class="description">
    <input type="radio" name="first_semester_used__survey" value="Fall2009"> 
    Fall 2009 <br>
    <input type="radio" name="first_semester_used__survey" value="Spring2009">
    Spring 2009 <br>
    <input type="radio" name="first_semester_used__survey" value="Fall2010">
    Fall 2010 <br>
    <input type="radio" name="first_semester_used__survey" value="Spring2010">
    Spring 2010 <br>
  </td>
</tr>

<tr>
  <td class="description">
  Prior to this semester, had you taken at least one Computer Science
  course with a strong programming component
  that <b>did not</b> use the Submit Server?

  </td>
  <td class="description">
    <input type="radio" name="used_submit_server_before__survey" value="true"> 
    Yes <br>
    <input type="radio" name="used_submit_server_before__survey" value="false">
    No <br>
  </td>
</tr>

<tr>
  <td class="description">
  If your course used Eclipse and the Course Project Manager Eclipse
  Plugin, were you able to get the plugin to work?
  </td>
  <td class="description">
  <input type="radio" name="plugin_working__survey" value="true"> Yes <br>
  <input type="radio" name="plugin_working__survey" value="false"> No <br>
  </td>
</tr>

<tr>
  <td class="description">
  Is your overall impression of the Submit Server positive or negative?
  </td>
  <td class="description">
  <input type="radio" name="overall_impression__survey" value="1">
  Very Negative <br>
  <input type="radio" name="overall_impression__survey" value="2"> 
  Somewhat Negative <br>
  <input type="radio" name="overall_impression__survey" value="3">
  Neutral <br>
  <input type="radio" name="overall_impression__survey" value="4"> 
  Somewhat Positive <br>
  <input type="radio" name="overall_impression__survey" value="5"> 
  Very Positive <br>
  </td>
  <td class="description">
  Other overall impressions of the Submit Server:
  <textarea name="overall_impression_comments__survey" rows="4" cols="60"></textarea>
  </td>
</tr>

<c:choose>
<c:when test="${hasReleaseTests}">
<tr>
  <td class="description">
  Do you prefer release testing over traditional testing, where the
  instructor tests are not run until after the deadline?
  </td>
  <td class="description">
  <input type="radio" name="prefer_release_testing__survey" value="1">
  Strongly prefer testing after the deadline <br>
  <input type="radio" name="prefer_release_testing__survey" value="2"> 
  Somewhat prefer testing after the deadline <br>
  <input type="radio" name="prefer_release_testing__survey" value="3">
  Neutral / no opinion <br>
  <input type="radio" name="prefer_release_testing__survey" value="4"> 
  Somewhat prefer release testing <br>
  <input type="radio" name="prefer_release_testing__survey" value="5"> 
  Strongly prefer release testing <br>
  </td>
  <td class="description">
  Other comments on release testing vs. traditional,
  after-the-deadline testing:
  <textarea name="prefer_release_testing_comments__survey" rows="4" cols="60"></textarea>
  </td>
</tr>

<tr>
  <td class="description">
  Do you agree that you were able to make good use of feedback from
  release tests?
  </td>
  <td class="description">
  <input type="radio" name="good_use_of_feedback__survey" value="1">
  Strongly Disagree <br>
  <input type="radio" name="good_use_of_feedback__survey" value="2"> 
  Somewhat Disagree <br>
  <input type="radio" name="good_use_of_feedback__survey" value="3">
  Neutral / No opinion <br>
  <input type="radio" name="good_use_of_feedback__survey" value="4"> 
  Somewhat Agree <br>
  <input type="radio" name="good_use_of_feedback__survey" value="5"> 
  Strongly Agree <br>
  </td>
  <td class="description">
  Other comments on using feedback from release tests
  <textarea name="good_use_of_feedback_comments__survey" rows="4" cols="60"></textarea>
  </td>
</tr>

<tr>
  <td class="description">
  Did the feedback from release tests tend to make you feel tense and
  unsure, or relaxed and confident?
  </td>
  <td class="description">
  <input type="radio" name="feedback_feeling__survey" value="1">
  Strongly Tense / Unsure <br>
  <input type="radio" name="feedback_feeling__survey" value="2"> 
  Somewhat Tense / Unsure <br>
  <input type="radio" name="feedback_feeling__survey" value="3">
  Neutral / No opinion <br>
  <input type="radio" name="feedback_feeling__survey" value="4"> 
  Somewhat Relaxed / Confident <br>
  <input type="radio" name="feedback_feeling__survey" value="5"> 
  Strongly Relaxed / Confident <br>
  </td>
  <td class="description">
  Other comments about feedback from release tests:
  <textarea name="feedback_feeling_comments__survey" rows="4" cols="60"></textarea>
  </td>
</tr>

<tr>
  <td class="description">
  Do you think that the feedback provided by release tests on the
  Submit Server caused you to beging working on programming
  assignments earlier in order to make better use of the release
  tests?
  </td>
  <td class="description">
  <input type="radio" name="started_earlier__survey" value="true"> Yes <br>
  <input type="radio" name="started_earlier__survey" value="false"> No <br>  
  </td>
  <td class="description">
  Other comments about when you began working on programming assignments:
  <textarea name="started_earlier_comments__survey" rows="4" cols="60"></textarea>
  </td>
</tr>

<tr>
  <td class="description">
  About how many release tests do you think you averaged using on each
  project?
  <p>
  (For example, if you used 5 release tests on project 1, and 3
  release tests on project 2, then you averaged using 4 release tests
  on each project).

  </td>
  <td class="description">
  <select name="average_release_tests_used__survey">
  <option>0</option>
  <option>1</option>
  <option>2</option>
  <option>3</option>
  <option>4</option>
  <option>5</option>
  <option>6</option>
  <option>7</option>
  <option>8</option>
  <option>9</option>
  <option>10</option>
  <option value="11">more than 10</option>
  </select>
<!--
  <input type="radio" name="average_release_tests_used" value="0"> 
  0 <br>
  <input type="radio" name="average_release_tests_used" value="1">
  1 <br>
  <input type="radio" name="average_release_tests_used" value="2">
  2 <br>
  <input type="radio" name="average_release_tests_used" value="3"> 
  3 <br>
  <input type="radio" name="average_release_tests_used" value="4_or_5">
  4 or 5 <br>
  <input type="radio" name="average_release_tests_used" value="6"> 6 <br>
  <input type="radio" name="average_release_tests_used" value="7"> 7 <br>
  <input type="radio" name="average_release_tests_used" value="8"> 8
  <br>  
-->
  </td>
</tr>
</c:when>
<c:otherwise>
<input type="hidden" name="prefer_release_testing__survey" value="0"/>
<input type="hidden" name="prefer_release_testing_comments__survey" value=""/>
<input type="hidden" name="good_use_of_feedback__survey" value="0"/>
<input type="hidden" name="good_use_of_feedback_comments__survey" value=""/>
<input type="hidden" name="feedback_feeling__survey" value="0"/>
<input type="hidden" name="feedback_feeling_comments__survey" value=""/>
<input type="hidden" name="started_earlier__survey" value="0"/>
<input type="hidden" name="started_earlier_comments__survey" value=""/>
<input type="hidden" name="average_release_tests_used__survey" value="-1"/>
</c:otherwise>
</c:choose>

<tr>
  <td class="description">
  Do you see any educational disadvantage(s) in using the Submit
  Server in a computer programming course?
  </td>
  <td class="description">
  <textarea name="educational_disadvantages__survey" rows="5" cols="60"></textarea>
  </td>
</tr>

<tr>
  <td class="description">
  Do you have any other suggestions to improve the Submit Server system?
  </td>
  <td class="description">
  <textarea name="suggestions__survey" rows="5" cols="60"></textarea>
  </td>
</tr>

<tr>
<td colspan="3" align="center">
<input type="submit" value="Submit!">
</td>
</tr>
</table>
</form>

</body>
</html>

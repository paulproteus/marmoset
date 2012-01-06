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

<!DOCTYPE HTML>
<html>
<ss:head title="Developing and Using Tools to Study and Improve the Software Development Learning Processes: Background data collection"/>

<body>
<ss:header />
<ss:breadCrumb />
  
    <form action="/action/UpdateBackgroundData" method=POST>

<h1>Demographic survey form</h1>
<p>This survey form is for those students participating
in the study by William Pugh and Jaime Spacco
at the University of Maryland
on <em>Developing and Using Tools to Study and
Improve the Software Development Learning Processes</em>.
<p>
<b>NOTE:</b> Please supply an answer for each of these questions.
Your may answer "Prefer not to respond" to any or all questions, but it's helpful
to us if you respond to each question.
<p>
<font color=red> ${unfinishedMessage}
</font>

<p align=center>
<table border=2>
<tr><td rowspan=3>Gender

	<td><input type=radio name=gender value="female" ${checkedFormManager.genderFemale}>Female
	</td>
<tr> <td><input type=radio name=gender value="male" ${checkedFormManager.genderMale}>Male
	</td>

<tr> <td><input type=radio name=gender value="na" ${checkedFormManager.genderNA}>Prefer not to answer
	</td>

<tr><td rowspan=6>Ethnic/Racial Association<br> (check as many as apply)
	</td>

<td><input type=checkbox name=AmericanIndian value="American Indian" ${checkedFormManager.americanIndian}>
American Indian
	</td>
	
<tr> <td><input type=checkbox name=Asian value="Asian" ${checkedFormManager.asian}>
	Asian
	</td>
	
<tr>	<td><input type=checkbox name=Black value="Black" ${checkedFormManager.black}>
Black
	</td>

<tr> <td><input type=checkbox name=Caucasian value="Caucasian" ${checkedFormManager.caucasian}>
	Caucasian
	</td>
	
<tr> <td><input type=checkbox name=LatinoLatina value="Latino/Latina" ${checkedFormManager.latinoLatina}>
Latino/Latina
	</td>
	
<tr> <td><input type=checkbox name=EthnicRacialAssociation 
				value="na" ${checkedFormManager.ethnicRacialAssociationNA}>
	Prefer not to answer
	</td>

<tr><td rowspan=4>Age
 <td><input type=radio name=age value="18-22" ${checkedFormManager.age23To29}>
	18-22
	</td>

<tr>
 <td><input type=radio name=age value="23-29" ${checkedFormManager.age23To29}>
	23-29
	</td>

<tr>
 <td><input type=radio name=age value="30+" ${checkedFormManager.age30Plus}>
	30+
	</td>

<tr>
 <td><input type=radio name=age value="na" ${checkedFormManager.ageNA}>
	Prefer not to answer
	</td>

<tr><td>Country of High School Attendance</td>
<td>

${checkedFormManager.highSchoolSelector}

</td>

<tr><td rowspan=7>Programming Experience prior to UMCP</td>
 <td><input type=radio name=priorProgrammingExperience value="none" ${checkedFormManager.priorProgrammingNone} />
	None
 </td>

 <tr><td><input type=radio name=priorProgrammingExperience value="Community College" ${checkedFormManager.priorProgrammingCommunityCollege}>
	Community College <br>
	(please enter the name of community college in the box to the right)
	</td>

	<td>
	<input type=text name=communityCollege size=40 value="${checkedFormManager.institutionCommunityCollege}"/>
	</td>
 <tr><td><input type=radio name=priorProgrammingExperience value="Other UM System Institution" ${checkedFormManager.priorProgrammingOtherUMInstitition}>
	Other UM System Institution <br>
	(please enter the name of the UM Institition in the box to the right)
	</td>

	<td>
	<input type=text name=otherUMInstitution size=40 value="${checkedFormManager.instititutionOtherUMInstitition}"/>
	</td>
 <tr><td><input type=radio name=priorProgrammingExperience value="Other non-UM System Institution" ${checkedFormManager.priorProgrammingOtherNonUMInstitition}>
	Other (non-UM) College/University <br>
	(please enter the name of the other institution in the box to the right)
	</td>
	
	<td>
	<input type=text name=otherNonUMInstitution size=40 value="${checkedFormManager.institutionOtherNonUMInstitution}"/>
	</td>
 <tr><td><input type=radio name=priorProgrammingExperience value="High School AP Course" ${checkedFormManager.priorProgrammingHighSchoolAP}>
	High School AP Course 
 	</td>
	<td>
 	Took A exam
	(Score: <input type=text name=aExamScore size=2 maxlength=1 value="${checkedFormManager.aexamScore}" >)

 	Took AB exam
 	(Score: <input type=text name=abExamScore size=2 maxlength=1 value="${checkedFormManager.abExamScore}" >)
	</td>
 <tr><td><input type=radio name=priorProgrammingExperience value="Other High School Course" ${checkedFormManager.priorProgrammingOtherHighSchool}>
	Other High School Course 
	</td>

<tr> <td><input type=radio name=priorProgrammingExperience value="na" ${checkedFormManager.priorProgrammingNA}>
Prefer not to answer
</td>

<tr><td rowspan=5>Placement exam at UMCP
 <td><input type=radio name=placementExam value="none" ${checkedFormManager.placementExamNone}>
	none
	</td>
<tr>
 <td><input type=radio name=placementExam value="cmsc131" ${checkedFormManager.placementExam131}>
	131
	</td>

 <td><input type=radio name=placementExamResult value="failed" ${checkedFormManager.placementExamResultFailed}>
	failed
	</td>

<tr>
 <td><input type=radio name=placementExam value="cmsc132" ${checkedFormManager.placementExam132}>
	132
	</td>

 <td><input type=radio name=placementExamResult value="marginally passed" ${checkedFormManager.placementExamResultMarginallyPassed}>
	marginally passed
	</td>

<tr>
 <td><input type=radio name=placementExam value="cmsc212" ${checkedFormManager.placementExam212}>
	212
	</td>

 <td><input type=radio name=placementExamResult value="passed" ${checkedFormManager.placementExamResultPassed}>
	passed
	</td>
	
<tr> <td><input type=radio name=placementExam value="na" ${checkedFormManager.placementExamNA}>
Prefer not to answer
</td>

<tr>
<td rowspan=5>Major
 <td><input type=radio name=major value="CS" ${checkedFormManager.majorCS}>
	Computer Science
	</td>

 <tr><td><input type=radio name=major value="CE" ${checkedFormManager.majorCE}>
	Computer Engineering
	</td>

 <tr><td><input type=radio name=major value="Math" ${checkedFormManager.majorMath}>
	Math
	</td>

 <tr><td><input type=radio name=major value="Other" ${checkedFormManager.majorOther}>
	Other
	</td>

<tr> <td><input type=radio name=major value="na" ${checkedFormManager.majorNA}>
Prefer not to answer
</td>

</table>

<input type=submit value="Submit Information">

</form>
  </body>
</html>

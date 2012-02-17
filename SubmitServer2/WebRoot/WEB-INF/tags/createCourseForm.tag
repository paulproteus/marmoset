
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss" %>

<c:url var="createCourseLink" value="/action/CreateCourse"/>
    <form action="${createCourseLink}" method="post" name="createCourseForm">
    <table class="form">
        <tr><th colspan=2>Create new course</th></tr>
        <tr>
            <td class="label">Course Name:</td>
            <td class="input"><input type="text" name="courseName" maxlength=20 size=20 placeholder="e.g., CMSC 132"></td>
        </tr>
        <tr>
            <td class="label">Semester:</td>
            <td class="input">
                <input type="text" name="semester" value="${ss:webProperty('semester')}" maxlength=15 size=15/>
            </td>
        </tr>
        <tr>
            <td class="label">Title (can be empty): </td>
            <td class="input">
                <input type="text" size="40" name="description" placeholder="e.g, Object Oriented Programming II">
            </td>
        </tr>
        <tr>
        	<td class="label">Sections (optional):</td>
        	<td class="input">
        		<input type="hidden" name="sections" id="hidden-section-list" />
        		<select id="course-section-dropdown"></select>
        		<input type="text" id="section-name-input" />
        		<button id="edit-course-sections">add</button>
        		<button id="clear-course-sections">clear</button>
        	</td>
        </tr>
        <tr>
            <td class="label">URL:</td>
            <td class="input"><input type="url" name="url" size="60" placeholder="Where people can find out more about the course"></td>
        </tr>
        <tr>
            <td class="label">allows baseline/starter code download:</td>
            <td class="input"><input name="download" type="checkbox" checked  />
            </td>
        </tr>
        <tr>
           <td class="label">Default for editing source code in browser:</td>
            <td class="input">
            <select name="browserEditing">
            <option value="prohibited">Prohibited</option>
            <option value="discouraged" selected="selected">Discouraged</option>
            <option value="allowed">Allowed</option>
            </select>
            </td>
        </tr>
        <tr  class="submit"><td colspan=2>
            <input type="submit" value="Create course">
    </table>
    </form>
    <script type="text/javascript">
    	$("#edit-course-sections").click(function(event) {
    		event.preventDefault();
    	});
    	$("#clear-course-sections").click(function(event){
    		event.preventDefault();
    	});
    </script>

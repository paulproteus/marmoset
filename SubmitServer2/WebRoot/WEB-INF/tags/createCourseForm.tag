
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss" %>

<c:url var="createCourseLink" value="/action/CreateCourse"/>
    <form action="${createCourseLink}" method="post" name="createCourseForm">
    <table class="form">
        <tr><th colspan=2>Create new course</th></tr>
        <tr>
            <td class="label">Course Name:</td>
            <td class="input"><input type="text" name="courseName"></td>
        </tr>
        <tr>
            <td class="label">Semester:</td>
            <td class="input">
                <input type="text" name="semester" value="${ss:webProperty('semester')}"/>
            </td>
        </tr>
        <tr>
            <td class="label">Description <br>(can be empty): </td>
            <td class="input">
                <textarea cols="40" rows="6" name="description"></textarea>
            </td>
        </tr>
        <tr>
            <td class="label">URL:</td>
            <td class="input"><input type="url" name="url" size="60"></td>
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

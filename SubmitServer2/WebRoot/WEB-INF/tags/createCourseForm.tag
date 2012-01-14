<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

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
                <input type="text" name="semester" value="${initParam['semester']}"/>
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
        <tr  class="submit"><td colspan=2>
            <input type="submit" value="Create course">
    </table>
    </form>

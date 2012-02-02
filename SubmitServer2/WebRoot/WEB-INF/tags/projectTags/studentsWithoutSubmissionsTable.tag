<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<c:if test="${project.visibleToStudents && not empty studentsWithoutSubmissions}">
<h1>Active students without submissions</h1>
<p>
<table>
	<tr>
	<th><a href="${sortByName}">Name</a></th>
		<th><a href="${sortByAcct}">Acct</a></th>

		<th>extension</th>
	</tr>

	<c:forEach var="studentRegistration" items="${studentsWithoutSubmissions}"
		varStatus="counter">
		<c:if test="${!studentRegistration.instructor}">
		<c:url var="studentLink"
					value="/view/instructor/student.jsp">
				<c:param name="studentPK" value="${studentRegistration.studentPK}" />
				<c:param name="coursePK" value="${course.coursePK}" />
			</c:url>

		<tr class="r${counter.index % 2}">

		<td class="description"><a href="${studentLink}">
		${studentRegistration.lastname}, ${studentRegistration.firstname}</a>
	        </td>
		<td class="description">${studentRegistration.classAccount} </td>

		<td>
			<c:url var="grantExtensionLink" value="/view/instructor/grantExtension.jsp">
				<c:param name="studentRegistrationPK" value="${studentRegistration.studentRegistrationPK}"/>
				<c:param name="projectPK" value="${project.projectPK}"/>
			</c:url>
			<a href="${grantExtensionLink}">

				<c:choose>
					<c:when test="${studentSubmitStatusMap[studentRegistration.studentRegistrationPK] == null}">
						0
					</c:when>
					<c:otherwise>
						${studentSubmitStatusMap[studentRegistration.studentRegistrationPK].extension}
					</c:otherwise>
				</c:choose>

			</a>
		</td>
		</tr>
		</c:if>
	</c:forEach>
</table>
</c:if>
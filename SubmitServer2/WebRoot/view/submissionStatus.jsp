<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<json:object>
  <json:property name="submissionPK" value="${submission.submissionPK}"/>
  <json:property name="buildStatus" value="${submission.buildStatus}"/>
  <json:property name="numPendingBuildRequests" value="${submission.numPendingBuildRequests}"/>
  <c:set var="timestamp">
  <fmt:formatDate value="${submission.buildRequestTimestamp}"
				pattern="dd MMM, hh:mm a" /></c:set>
  <json:property name="buildRequestTimestamp" value="${timestamp}"/>
</json:object>

<%@ taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<json:object>
  <json:property name="submissionPK" value="${submission.submissionPK}"/>
  <json:property name="buildStatus" value="${submission.buildStatus}"/>
  <json:property name="numPendingBuildRequests" value="${submission.numPendingBuildRequests}"/>
</json:object>
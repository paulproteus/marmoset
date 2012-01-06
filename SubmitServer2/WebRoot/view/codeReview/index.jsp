
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<!DOCTYPE html>
<html>
<head>

<script>
var reviewSummary = {
    "daoKey": '<c:out value="${reviewDaoKey}" />',
    "title": '<c:out value="${reviewTitle}" />',
    "backlinkText": '<c:out value="${reviewBacklinkText}" />',
    "backlinkUrl": '<c:out value="${reviewBacklinkUrl}" />',
};
</script>

<title>"Code review"</title>
  <c:url var="gwtLink" value="/codereview/codereview.nocache.js"/>
    <script type="text/javascript"  src="${gwtLink}"></script>
    <c:url var="cssLink" value="/codereview.css"/>
    <link rel="stylesheet" href="${cssLink}" />
    <link href='http://fonts.googleapis.com/css?family=Ubuntu+Mono' rel='stylesheet' type='text/css'>
</head>
<body>

  <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>


</body>
</html>

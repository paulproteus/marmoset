
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions'%>


<c:if test="${ not empty sourceFiles || project.browserEditing != 'PROHIBITED' }">


    <c:url var="submitURL" value="/action/CodeMirrorSubmission" />
    <c:url var="codemirror" value="/codemirror" />

    <script src="${codemirror}/lib/codemirror.js"></script>
    <script src="${codemirror}/lib/util/foldcode.js"></script>
    <script src="${codemirror}/mode/clike/clike.js"></script>
<c:if test="${project.browserEditing == 'DISCOURAGED' }">
<p>It is recommend that edit this projects on your computer, using an IDE or editing tools. 
If you edit code in the browser and later want to continue work on it on your computer, keep in mind that you will have to download
the changes from the submit server.
</c:if>

    <c:choose>
        <c:when test="${not empty sourceSubmission}">
            <c:url var="submissionLink" value="/view/submission.jsp">
                <c:param name="submissionPK" value="${sourceSubmission.submissionPK}" />
            </c:url>
            <h3 id="editSource">
            
               Editing  <a href="${submissionLink}">submission #${sourceSubmission.submissionNumber}, submitted at <fmt:formatDate
                        value="${sourceSubmission.submissionTimestamp}" pattern="E',' dd MMM 'at' hh:mm a" /></a>
                        <c:url var="downloadLink" value="/data/DownloadSubmission">
                <c:param name="submissionPK" value="${submission.submissionPK}" />
            </c:url> (<a href="${downloadLink}">download</a>)
            </h3>

        </c:when>
        <c:otherwise>
            <h3>Editing baseline source for project</h3>
        </c:otherwise>
    </c:choose>
    
    <form id="submitform" method="POST" action="${submitURL}">
        <input type="hidden" name="projectPK" value="${project.projectPK}" /> <input type="hidden"
            name="submitClientTool" value="codemirror" /> 
            <c:if test="${not empty sourceSubmission}">
            <input type="hidden" name="studentRegistrationPK" value="${sourceSubmission.studentRegistrationPK}"/>
        </c:if> 
            <input type="hidden" name="numFiles"
            value="${fn:length(sourceFiles)}" />
        <input type="submit" value="Submit as new submission">
        <script>
        var editors = {};
        function toggleEditor(item) {
            $(document.getElementById(item)).slideToggle("slow");
            editors[item].refresh();
        }
        var foldFunc = CodeMirror.newFoldFunction(CodeMirror.braceRangeFinder);
        
        </script>
        
        <c:forEach var="file" items="${sourceFiles}" varStatus="counter">
            <c:set var="name" value="f-${counter.index}" />
            <c:set var="contents" value="${sourceContents[file]}" />
            <c:set var="properties" value="${sourceProperties[file]}" />
            <c:set var="rows" value="${3+fn:length(contents)}" />
            <c:set var="filename">
                <c:out value="${file}" />
                 </c:set>
            <c:set var="divName" value="div-${counter.index}" />
            <c:set var="display" value="block"/>
            <c:if test="${properties.collapsed}">
             <c:set var="display" value="none"/>
             </c:if>

            <input type="hidden" name="n-${counter.index}" value="${filename}" />

            <h4><a href="javascript:toggleEditor('${divName}')">${filename}</a>
             <c:if test="${properties.readonly}">
              (Read only)</c:if>
            </h4>
             
            <div id="${divName}" style="display: ${display}">
            
            <textarea id="${name}" name="${name}" rows="${rows}" cols="80">
                <c:forEach var="line" items="${contents}">
<c:out value="${line}" /></c:forEach>
     </textarea>
            <script>
    var myCodeMirror = CodeMirror.fromTextArea(document.getElementById('${name}'), {
  lineNumbers: true, indentUnit: 4,  onGutterClick: foldFunc, readOnly: ${properties.readonly} });
     editors['${divName}'] = myCodeMirror;
      </script>
      </div>
        </c:forEach>
    </form>

<p align="right">javascript for code editing in browser provided by <a href="http://codemirror.net/">{} CodeMirror</a>

    </c:if>
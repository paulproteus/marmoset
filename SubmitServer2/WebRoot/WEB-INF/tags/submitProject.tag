<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>


<div id="submitProject">
<h3>Uploading a submission</h3>
<p>You can submit <a href="javascript:toggle('submittingZip')" title="description">a zip file</a>
 or <a href="javascript:toggle('submittingSource')" title="description">multiple text files</a>.

<div id="submittingZip" style="display: none">
<h2>Submitting a zip file</h2>
<p>You may upload a Zip archive containing your project submission.
<p>The Zip archive must contain the <b>entire</b> project directory,
including all of your source files.
<br>If you are using Eclipse, this means your archive should include have the .project file
and src/ directories at the root of the archive.
<p>Files generated during compilation (i.e. .class or .o files) will be discarded by the server,
so don't worry if your submission includes them.
</div>

<div id="uploadSource" style="display: none">
<h2>Submitting multiple text files</h2>
<p>You can also submit multiple files, typically all source files.
If the files need to be in a particularly directory structure, then all of the submitted
files need to be ones in the baseline submission, otherwise we can't recreate the intended directory
structure. 
</div>

<script>
jQuery(document).ready(function ($) {
        $('#submitForm input:submit').attr('disabled',true);
        $('#submitForm input:file').change(
            function(){
                if ($(this).val()){
                    $('#submitForm input:submit').removeAttr('disabled'); 
                }
                else {
                    $('#submitForm input:submit').attr('disabled',true);
                }
            });
      
    });
</script>

<form id="submitForm" enctype="multipart/form-data"
    action="<c:url value="/action/SubmitProjectViaWeb"/>" method="POST"><input type="hidden"
    name="projectPK" value="${project.projectPK}" /> <input type="hidden"
    name="submitClientTool" value="web" />
<table class="form">
<tr><th colspan=2>file(s) for submission</th>
<tr><td>File(s) to Submit: <td class="input"><input type="file" name="file" class="multi" size=60 />
<tr class="submit"><td class="submit" colspan="2"><input type="submit" value="Submit project!">
</table>
</form>

</div>
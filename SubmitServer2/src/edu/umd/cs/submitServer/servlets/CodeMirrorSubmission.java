/**
 * Marmoset: a student project snapshot, submission, testing and code review
 * system developed by the Univ. of Maryland, College Park
 * 
 * Developed as part of Jaime Spacco's Ph.D. thesis work, continuing effort led
 * by William Pugh. See http://marmoset.cs.umd.edu/
 * 
 * Copyright 2005 - 2011, Univ. of Maryland
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

/*
 * Created on Jan 13, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.RequestParser;
import edu.umd.cs.submitServer.servlets.UploadSubmission.Kind;

public class CodeMirrorSubmission extends SubmitServerServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        long now = System.currentTimeMillis();
        Timestamp submissionTimestamp = new Timestamp(now);

        RequestParser parser = new RequestParser(request, getSubmitServerServletLog(), strictParameterChecking());

        // these are set by filters or previous servlets
        Project project = (Project) request.getAttribute(PROJECT);
        StudentRegistration studentRegistration = (StudentRegistration) request.getAttribute(STUDENT_REGISTRATION);
        Kind kind = Kind.CODEMIRROR;
        String clientTool = "web";
        String clientVersion = kind.toString();
        
        int num = parser.getIntParameter("numFiles");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);

        for (int i = 0; i < num; i++) {
            String fName = parser.getStringParameter("n-" + i);
            String contents = parser.getStringParameter("f-" + i);
            byte[] bytes = contents.getBytes();

            ZipEntry zentry = new ZipEntry(fName);
            zentry.setSize(bytes.length);
            zentry.setTime(now);
            zos.putNextEntry(zentry);
            zos.write(bytes);
            zos.closeEntry();
        }
        zos.flush();
        zos.close();
        kind = Kind.CODEMIRROR;
        byte[] zipOutput = bos.toByteArray();
        Submission submission
        = UploadSubmission.uploadSubmission(project, studentRegistration, zipOutput, request, submissionTimestamp, clientTool,
                    clientVersion, null, getDatabaseProps(), getSubmitServerServletLog());

        request.setAttribute("submission", submission);

        String redirectUrl = request.getContextPath() + "/view/submission.jsp?submissionPK=" + submission.getSubmissionPK();

        response.sendRedirect(redirectUrl);

    }

}

package edu.umd.cs.marmoset.modelClasses;

import java.util.Collection;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import edu.umd.cs.marmoset.utilities.DatabaseUtilities;

import junit.framework.TestCase;

public class CheckCodeReviewer extends TestCase {
    public void testCodeReviewers() throws Exception {
        java.sql.Connection conn = DatabaseUtilities.getConnection();
        Collection<CodeReviewer> c = CodeReviewer.getAll(conn);
        for(CodeReviewer r : c) {
            Submission submission = r.getSubmission();
            Project project = Project.lookupByProjectPK(submission.getProjectPK(), conn);
            StudentRegistration author = StudentRegistration.lookupByStudentRegistrationPK(submission.getStudentRegistrationPK(), conn);
            Student reviewer = r.getStudent();
            StudentRegistration reviewerRegistration
              = StudentRegistration.lookupByStudentPKAndCoursePK(reviewer.getStudentPK(), project.getCoursePK(), conn);
            if (r.isAuthor() != (reviewer.getStudentPK() == author.getStudentPK()))
                System.out.printf("is author? %5s %3d %3d %3d %3d %3d%n",
                        r.isAuthor(),
                        r.getCodeReviewAssignmentPK(),
                        r.getCodeReviewerPK(),
                        reviewer.getStudentPK(),
                        reviewerRegistration.getStudentRegistrationPK(),
                        r.getSubmissionPK());
            if (r.isInstructor() != reviewerRegistration.isInstructor())
                System.out.printf("is instructor? %5s %3d %3d %3d %3d %3d%n",
                        r.isInstructor(),
                        r.getCodeReviewAssignmentPK(),
                        r.getCodeReviewerPK(),
                        reviewer.getStudentPK(),
                        reviewerRegistration.getStudentRegistrationPK(),
                        r.getSubmissionPK());
        }
    }

}

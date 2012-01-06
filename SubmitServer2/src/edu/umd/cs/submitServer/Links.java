package edu.umd.cs.submitServer;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Submission;

public class Links {

	public static String defaultInstructorView(Project project,
			Submission submission) {
		if (project.isTested())
			return String.format(
					"/view/instructor/submission.jsp?submissionPK=%d",
					submission.getSubmissionPK());

		return String.format("/view/allSourceCode.jsp?submissionPK=%d",
				submission.getSubmissionPK());

	}
	public static String defaultStudentView(Project project,
			Submission submission) {
		if (project.isTested())
			return String.format(
					"/view/submission.jsp?submissionPK=%d",
					submission.getSubmissionPK());
		return String.format("/view/allSourceCode.jsp?submissionPK=%d",
				submission.getSubmissionPK());

	}


}

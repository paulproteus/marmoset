package edu.umd.cs.submitServer.policy;

public class ChooseHighScoreSubmissionPolicy  extends ChooseLastSubmissionPolicy {

	public static final ChooseHighScoreSubmissionPolicy
	INSTANCE = new ChooseHighScoreSubmissionPolicy();

	@Override
	public  String getDescription() {
		return "Grade the highest scoring submission";
	}

	@Override
	public String getOrderingConstraint() {
		 return  " ORDER BY submissions.num_passed_overall desc, submission_timestamp desc ";
	}




}

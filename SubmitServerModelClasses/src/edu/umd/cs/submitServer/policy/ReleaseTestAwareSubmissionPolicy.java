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

/**
 * Created on Feb 15, 2006
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.policy;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;

/**
 * ReleaseTestAwareBestSubmissionPolicy
 *
 * Best submission for a category (on-time or late) is the max of the last
 * submission that compiles and the highest-scoring release-tested submission.
 * If there is a tie between the last submission that compiles and the
 * highest-scoring release-tested submission, the last submission that compiles
 * wins.
 *
 * @author jspacco
 */
public class ReleaseTestAwareSubmissionPolicy extends
	ChooseHighScoreSubmissionPolicy {

	public static final ReleaseTestAwareSubmissionPolicy
		INSTANCE = new ReleaseTestAwareSubmissionPolicy();

	@Override
	public  String getDescription() {
		return "Grade the last submission or the highest scoring release tested submission";
	}


	@Override
	public String getQualifyingConstraint() {
		return 	 " AND submissions.release_request IS NOT NULL ";
	}

	@Override
	public String getOntimeConstraint() {
		return  " AND submission_timestamp <= DATE_ADD(projects.ontime, INTERVAL student_submit_status.extension HOUR) "
				+ " AND submissions.release_request <= DATE_ADD(projects.late, INTERVAL student_submit_status.extension HOUR) ";
	}

	@Override
	public String getLateConstraint() {
		 return " AND submission_timestamp > DATE_ADD(projects.ontime, INTERVAL student_submit_status.extension HOUR) "
		 + " AND submission_timestamp <= DATE_ADD(projects.late, INTERVAL student_submit_status.extension HOUR) "
		 + " AND submissions.release_request <= DATE_ADD(projects.late, INTERVAL student_submit_status.extension HOUR) ";
	}


	@Override
	public  Map<Integer, Submission> lookupChosenOntimeSubmissionMap(
			Project project, Connection conn) throws SQLException {
		Map<Integer, Submission> releaseTested = super.lookupChosenOntimeSubmissionMap(project, conn);
		Map<Integer, Submission> lastOntime = ChooseLastSubmissionPolicy.INSTANCE.lookupChosenOntimeSubmissionMap(project, conn);
		return bestSubmission(releaseTested, lastOntime);

	}

	@Override
	public  Map<Integer, Submission> lookupChosenLateSubmissionMap(
			Project project, Connection conn) throws SQLException {
		Map<Integer, Submission> releaseTested = super.lookupChosenLateSubmissionMap(project, conn);
		Map<Integer, Submission> lastOntime = ChooseLastSubmissionPolicy.INSTANCE.lookupChosenLateSubmissionMap(project, conn);
		return bestSubmission(releaseTested, lastOntime);

	}

	private Map<Integer, Submission> bestSubmission(
			Map<Integer, Submission> submissions1,
			Map<Integer, Submission> submissions2) {
		for(Map.Entry<Integer, Submission> e : submissions1.entrySet()) {
			@StudentRegistration.PK Integer key = e.getKey();
            Submission last = submissions2.get(key);
			Submission lastReleaseTested = e.getValue();
			if (last.getValuePassedOverall() >  lastReleaseTested.getValuePassedOverall())
				e.setValue(last);
		}
		submissions2.keySet().removeAll(submissions1.keySet());
		submissions1.putAll(submissions2);
		return submissions1;
	}




}

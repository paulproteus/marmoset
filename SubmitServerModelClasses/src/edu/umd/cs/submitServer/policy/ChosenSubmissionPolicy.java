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
 * Created on Nov 12, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.policy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.utilities.SqlUtilities;

/**
 * CSubmissionPolicy Defines how to select the 'chosen' submission for each
 * category of ontime, late, very-late and last.
 *
 * @author jspacco
 */
public abstract class ChosenSubmissionPolicy {

	public abstract String getDescription();
	public abstract Map<Integer, Submission> lookupLastSubmissionMap(
			Project project, Connection conn) throws SQLException;

	public abstract Map<Integer, Submission> lookupChosenOntimeSubmissionMap(
			Project project, Connection conn) throws SQLException;


	public abstract Map<Integer, Submission> lookupChosenLateSubmissionMap(
			Project project, Connection conn) throws SQLException;

	public abstract Map<Integer, Submission> lookupChosenVeryLateSubmissionMap(
			Project project, Connection conn) throws SQLException;

	static int getExtensionFromResultSet(ResultSet rs) throws SQLException {
		return rs.getInt("student_submit_status.extension");
	}

	static @StudentRegistration.PK  Integer getStudentRegistrationPKFromResultSet(ResultSet resultSet)
			throws SQLException {
		return StudentRegistration.asPK(SqlUtilities.getInteger(resultSet, 2));
	}
	
	   public abstract  @CheckForNull Submission lookupChosenOntimeOrLateSubmission(Project project,  
	           @StudentRegistration.PK int studentRegistrationPK, Connection conn)
	            throws SQLException;

	/**
	 * Builds a map of the best adjusted score from the ontime and late
	 * submissions.
	 *
	 * @param studentRegistrationList
	 *            list of the studentRegistrations
	 * @param ontimeMap
	 *            the ontime submissions
	 * @param lateMap
	 *            the late submissions (with adjusted scores)
	 * @return
	 */
	public Map<Integer, Submission> getChosenSubmissionMap(
			Set<StudentRegistration> studentRegistrationList,
			Map<Integer, Submission> ontimeMap, Map<Integer, Submission> lateMap) {
		Map<Integer, Submission> bestMap = new HashMap<Integer, Submission>();
		for (StudentRegistration registration : studentRegistrationList) {
			@StudentRegistration.PK Integer studentRegistrationPK = registration
					.getStudentRegistrationPK();

			Submission ontimeSubmission = ontimeMap.get(studentRegistrationPK);
			Submission lateSubmission = lateMap.get(studentRegistrationPK);

			if (ontimeSubmission == null) {
				if (lateSubmission != null) {
					bestMap.put(studentRegistrationPK, lateSubmission);
				}
			} else {
				// onTimeSubmission != null
				if (lateSubmission != null) {
					int lateScore = Math.max(0,
							lateSubmission.getAdjustedScore());
					if (lateScore >= ontimeSubmission.getAdjustedScore()) {
						// lateScore better than ontime score
						bestMap.put(studentRegistrationPK, lateSubmission);
					} else {
						// ontime score better than late score
						bestMap.put(studentRegistrationPK, ontimeSubmission);
					}
				} else {
					// no late score exists
					bestMap.put(studentRegistrationPK, ontimeSubmission);
				}

				if (lateSubmission != null
						&& lateSubmission.getAdjustedScore() > 0
						&& lateSubmission.getAdjustedScore() >= ontimeSubmission
								.getAdjustedScore()) {
					bestMap.put(studentRegistrationPK, lateSubmission);
				}
			}
		}
		return bestMap;
	}
	
	
 
    
}

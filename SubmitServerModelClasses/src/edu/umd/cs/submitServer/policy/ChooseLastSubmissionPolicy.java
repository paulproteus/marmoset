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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Queries;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.utilities.SqlUtilities;

/**
 * DefaultBestSubmissionPolicy Default implementation that always returns the
 * last submission before each deadline.
 *
 * @author jspacco
 */
public class ChooseLastSubmissionPolicy extends ChosenSubmissionPolicy {

	public static final ChooseLastSubmissionPolicy
	INSTANCE = new ChooseLastSubmissionPolicy();
	@Override
	public  String getDescription() {
		return "Grade the last submission";
	}

	public String getBaseQuery() {
		return " SELECT "
		+ Submission.ATTRIBUTES
		+ ", student_submit_status.extension "
		+ " FROM submissions, student_submit_status, projects "
		+ " WHERE submissions.project_pk = ? "
		+ " AND student_submit_status.project_pk = submissions.project_pk "
		+ " AND student_submit_status.student_registration_pk = submissions.student_registration_pk "
		+ " AND submissions.project_pk = projects.project_pk ";

	}

	public String getOntimeConstraint() {
		return  " AND submission_timestamp <= DATE_ADD(projects.ontime, INTERVAL student_submit_status.extension HOUR) ";
	}

	public String getLateConstraint() {
		 return " AND submission_timestamp > DATE_ADD(projects.ontime, INTERVAL student_submit_status.extension HOUR) "
		 + " AND submission_timestamp <= DATE_ADD(projects.late, INTERVAL student_submit_status.extension HOUR) ";
	}
	public String getOntimeOrLateConstraint() {
        return " AND submission_timestamp <= DATE_ADD(projects.late, INTERVAL student_submit_status.extension HOUR) ";
   }

	public String getVeryLateConstraint() {
		return  " AND submission_timestamp > DATE_ADD(projects.late, INTERVAL student_submit_status.extension HOUR) ";
	}
	public String getStudentRegistrationConstaints() {
        return  " AND submissions.student_registration_pk = ? ";
    }

	public String getOrderingConstraint() {
		 return " ORDER BY submission_timestamp desc ";
	}

	public String getQualifyingConstraint() {
		return 	" AND (submissions.num_build_tests_passed > 0 OR "
				+ "       NOT projects.is_tested) ";
	}
	
	public String getLimitOneConstraints() {
        return  " LIMIT 1 ";
    }
	/**
	 * Finds the last compiling submission.
	 *
	 * @param projectPK
	 * @param conn
	 * @return the last ontime submission; null if there were no ontime
	 *         submissions
	 */
	@Override
	public Map<Integer, Submission> lookupChosenOntimeSubmissionMap(
			Project project, Connection conn) throws SQLException {
		String query = getBaseQuery() + getOntimeConstraint() + getQualifyingConstraint() + getOntimeConstraint()
				+ getOrderingConstraint();

		return lookupChosenSubmissionMapAndExtensionFromQuery(query, project,
				conn);

	}
	
	@Override
    public @CheckForNull Submission lookupChosenOntimeOrLateSubmission(
            Project project, @StudentRegistration.PK int studentRegistrationPK, Connection conn) throws SQLException {
        String query = getBaseQuery() + getOntimeConstraint() + getQualifyingConstraint() + getOntimeOrLateConstraint()
                 + getStudentRegistrationConstaints() + getOrderingConstraint() + getLimitOneConstraints();
        
        PreparedStatement stmt = null;
        try {
            stmt = Queries.setStatement(conn, query, project.getProjectPK(), studentRegistrationPK);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return  new Submission(rs, 1);
            return null;
        } finally {
            Queries.closeStatement(stmt);
        }

    }

	

	/**
	 * @param project
	 * @param stmt
	 * @return
	 * @throws SQLException
	 */
	private static Map<Integer, Submission> getChosenSubmissionMapFromStmt(
			Project project, PreparedStatement stmt) throws SQLException {
		Map<Integer, Submission> result = new HashMap<Integer, Submission>();
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			@StudentRegistration.PK  Integer studentRegistrationPK = getStudentRegistrationPKFromResultSet(rs);
			if (result.containsKey(studentRegistrationPK))
				continue;
			Submission submission = new Submission(rs, 1);
			// set late status
			submission.setStatus(project);
			// adjust final score based on late status and late penalty
			submission.setAdjustedScore(project);
			result.put(studentRegistrationPK, submission);
		}
		return result;
	}

	/**
	 * Returns a map from studentRegistrationPK's to Submission objects, where
	 * the Submission object is the extension-adjusted version of the first
	 * result to appear in the query response.
	 *
	 * @param query
	 *            SQL query to use to get results (order matters)
	 * @param project
	 *            the project to query
	 * @param conn
	 *            DB connection
	 * @return
	 * @throws SQLException
	 */
	protected static Map<Integer, Submission> lookupChosenSubmissionMapAndExtensionFromQuery(
			String query, Project project, Connection conn) throws SQLException {
		Map<Integer, Submission> result = new HashMap<Integer, Submission>();
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(query);
			// Set the projectPK in the query
			SqlUtilities.setInteger(stmt, 1, project.getProjectPK());
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				@StudentRegistration.PK Integer studentRegistrationPK = getStudentRegistrationPKFromResultSet(rs);
				if (result.containsKey(studentRegistrationPK)) {
					// Student already accounted for; i.e., there was
					// another submission for that student, submitted
					// earlier than this one
					continue;
				}
				Submission submission = new Submission(rs, 1);
				// set late status, taking extension into account
				int extension = getExtensionFromResultSet(rs);
				submission.setStatus(project, extension);
				// adjust final score based on late status, late penalty,
				// and extension
				submission.setAdjustedScore(project, extension);
				result.put(studentRegistrationPK, submission);
			}
			return result;
		} finally {
			Queries.closeStatement(stmt);
		}
	}

	@Override
	public Map<Integer, Submission> lookupChosenLateSubmissionMap(
			Project project, Connection conn) throws SQLException {

		String query = getBaseQuery() + getLateConstraint() + getQualifyingConstraint() + getLateConstraint()
		 + getOrderingConstraint();

		return lookupChosenSubmissionMapAndExtensionFromQuery(query, project,
				conn);
	}

	/**
	 * Create a map from studentRegistrationPK to the last very late submission
	 * for a given project.
	 *
	 * @param project
	 *            the project for which we want the last very late submission
	 * @param conn
	 *            the connection to the database
	 * @return a map from studentRegistrationPK to the last very late
	 *         submission.
	 * @throws SQLException
	 */
	@Override
	public Map<Integer, Submission> lookupChosenVeryLateSubmissionMap(
			Project project, Connection conn) throws SQLException {
		String query = getBaseQuery() + getVeryLateConstraint()+ getQualifyingConstraint() + getVeryLateConstraint()
		 + getOrderingConstraint();

		return lookupChosenSubmissionMapAndExtensionFromQuery(query, project,
				conn);
	}

	/**
	 * Finds the last submission.
	 *
	 * @param studentRegistrationPK
	 * @param projectPK
	 * @param conn
	 * @return the last very-late submission; null if there were no very late
	 *         submissions
	 */
	@Override
	public Map<Integer, Submission> lookupLastSubmissionMap(Project project,
			Connection conn) throws SQLException {
		String query = " SELECT " + Submission.ATTRIBUTES
				+ " FROM submissions " + " WHERE submissions.project_pk = ? "
				+ " AND submissions.submission_timestamp IS NOT NULL "
				+ " ORDER BY submission_timestamp desc ";

		return getChosenSubmissionMapFromQuery(query, project, conn);
	}

	/**
	 * Gets a map from studentRegistrationPK to the last submission the student
	 * made.
	 *
	 * @param stmt
	 * @param queryTerm
	 *            TODO
	 * @param conn
	 *            TODO
	 * @return
	 * @throws SQLException
	 */
	private static Map<Integer, Submission> getChosenSubmissionMapFromQuery(
			String query, Project project, Connection conn) throws SQLException {

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(query);
			SqlUtilities.setInteger(stmt, 1, project.getProjectPK());

			return getChosenSubmissionMapFromStmt(project, stmt);
		} finally {
			Queries.closeStatement(stmt);
		}
	}
}

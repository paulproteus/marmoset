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

package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import edu.umd.cs.marmoset.utilities.MarmosetUtilities;
import edu.umd.cs.marmoset.utilities.SqlUtilities;

/**
 * @author jspacco
 *
 */
 public class StudentSubmitStatus {

     public static boolean showPostDeadlineDetails(Project project,
             StudentSubmitStatus studentSubmitStatus) {
         long now = System.currentTimeMillis();
         return project.getPostDeadlineOutcomeVisibility().equals(
                 Project.POST_DEADLINE_OUTCOME_VISIBILITY_EVERYTHING)
             && project.getLate().getTime()
                 + (studentSubmitStatus.extension*60L*60*1000) < now;
     }

	//private String pk;
	private @Project.PK int projectPK; // non-NULL
	private @StudentRegistration.PK  int studentRegistrationPK; // non-NULL
	private @CheckForNull @StudentRegistration.PK  Integer partner_sr_pk;
	private String oneTimePassword;
	private int numberSubmissions;
	private int numberRuns;
	private int numberCommits;
	private int extension = 0;
	private boolean canReleaseTest = true;
	private Timestamp lastBuildRequestTimestamp;


	/**
	 * List of all attributes of student_submit_status table.
	 */
	  public static final String[] ATTRIBUTE_NAME_LIST = {
			"project_pk",
			"student_registration_pk",
			"partner_sr_pk",
			"one_time_password",
			"number_submissions",
			"number_commits",
			"number_runs",
			"extension",
			"can_release_test",
			"last_build_request_timestamp"
	};

	public static final String TABLE_NAME = "student_submit_status";

	 /**
	 * Fully-qualified attributes for student_submit_status table.
	 */
	public static final String ATTRIBUTES =
		Queries.getAttributeList(TABLE_NAME, ATTRIBUTE_NAME_LIST);


    /**
     * @return Returns the numberSubmissions.
     */
    public int getNumberSubmissions()
    {
        return numberSubmissions;
    }
    /**
     * @param numberSubmissions The numberSubmissions to set.
     */
    public void setNumberSubmissions(int numberSubmissions)
    {
        this.numberSubmissions = numberSubmissions;
    }
	/**
	 * @return Returns the oneTimePassword.
	 */
	public String getOneTimePassword() {
		return oneTimePassword;
	}
	/**
	 * @param oneTimePassword The oneTimePassword to set.
	 */
	private void setOneTimePassword(String oneTimePassword) {
		this.oneTimePassword = oneTimePassword;
	}

	@Deprecated
	public void setOneTimePasswordHack(String oneTimePassword) {
        this.oneTimePassword = oneTimePassword;
    }
	/**
	 * @return Returns the projectPK.
	 */
	public @Project.PK int getProjectPK() {
		return projectPK;
	}
	/**
	 * @param projectPK The projectPK to set.
	 */
	public void setProjectPK(@Project.PK int projectPK) {
		this.projectPK = projectPK;
	}
	/**
	 * @return Returns the studentRegistrationPK.
	 */
	public @StudentRegistration.PK  int getStudentRegistrationPK() {
		return studentRegistrationPK;
	}
	/**
	 * @param studentRegistrationPK The studentRegistrationPK to set.
	 */
	public void setStudentRegistrationPK(@StudentRegistration.PK int studentRegistrationPK) {
		this.studentRegistrationPK = studentRegistrationPK;
	}

    public @CheckForNull @StudentRegistration.PK  Integer getPartnerPK() {
		return partner_sr_pk;
	}
	public void setPartnerPK(@CheckForNull @StudentRegistration.PK  Integer partner_sr_pk) {
		this.partner_sr_pk = partner_sr_pk;
	}
	/**
     * @return Returns the extension.
     */
    public int getExtension() {
        return extension;
    }
    /**
     * @param extension The extension to set.
     */
    public void setExtension(int extension) {
        this.extension = extension;
    }
    /**
     * @return Returns the numberCommits.
     */
    public int getNumberCommits()
    {
        return numberCommits;
    }
    /**
     * @param numberCommits The numberCommits to set.
     */
    public void setNumberCommits(int numberCommits)
    {
        this.numberCommits = numberCommits;
    }

	public int getNumberRuns() {
        return numberRuns;
    }
    public void setNumberRuns(int numberRuns) {
        this.numberRuns = numberRuns;
    }
    /**
	 * @return canReleaseTest flag indicating if release tests are allowed
	 */
	public boolean getCanReleaseTest() {
		return canReleaseTest;
	}

	/**
	 * @param canReleaseTest indicate if release tests are allowed
	 */
	public void setCanReleaseTest(boolean canReleaseTest) {
		this.canReleaseTest = canReleaseTest;
	}

	public Timestamp getLastBuildRequestTimestamp() {
		return lastBuildRequestTimestamp;
	}
	
	private void setLastBuildRequestTimestamp(Timestamp lastBuildRequestTimestamp) {
		 this.lastBuildRequestTimestamp = lastBuildRequestTimestamp;
	}
	public void fetchValues(ResultSet resultSet, int startingFrom) throws SQLException
	{
		setProjectPK(Project.asPK(resultSet.getInt(startingFrom++)));
		setStudentRegistrationPK(StudentRegistration.asPK(resultSet.getInt(startingFrom++)));
		setPartnerPK(StudentRegistration.asPK(resultSet.getInt(startingFrom++)));
		setOneTimePassword(resultSet.getString(startingFrom++));
		setNumberSubmissions(resultSet.getInt(startingFrom++));
		setNumberCommits(resultSet.getInt(startingFrom++));
		setNumberRuns(resultSet.getInt(startingFrom++));
		setExtension(resultSet.getInt(startingFrom++));
		setCanReleaseTest(resultSet.getBoolean(startingFrom++));
		setLastBuildRequestTimestamp(resultSet.getTimestamp(startingFrom++));
	}

	/**
	 * Inserts a new student registration row into the database.
	 * <p>
	 * We first check for duplicates.  The primary key field of StudentSubmitStatus
	 * is obsolete and will be removed in future versions.  StudentSubmitStatus has
	 * a compound primary key consisting of studentRegistrationPK and projectPK
	 * since that uniquely identifies a StudentSubmitStatus.
	 *
	 * @param conn the connection to the database
	 * @return the StudentSubmitStatus to use
	 * @throws SQLException
	 */
	private @CheckReturnValue StudentSubmitStatus findOrInsert(Connection conn)
		throws SQLException
	{
		StudentSubmitStatus studentSubmitStatus = StudentSubmitStatus.lookupByStudentRegistrationPKAndProjectPK(
		        getStudentRegistrationPK(),
		        getProjectPK(),
		        conn);

		// if the row already exists, return the existing record
		if (studentSubmitStatus != null)
		    return studentSubmitStatus;

		String query = Queries.makeInsertStatementUsingSetSyntax(ATTRIBUTE_NAME_LIST, TABLE_NAME, false);

		PreparedStatement stmt = conn.prepareStatement(query);

		putValues(stmt, 1);
		executeUpdate(stmt);
		
		return this;
	}
	private void putValues(PreparedStatement stmt, int index) throws SQLException {
		stmt.setInt(index++, projectPK);
		stmt.setInt(index++, studentRegistrationPK);
		if (partner_sr_pk == null)
			stmt.setNull(index++, Types.INTEGER);
		else
			stmt.setInt(index++, partner_sr_pk);
		stmt.setString(index++, oneTimePassword);
		stmt.setInt(index++, numberSubmissions);
		stmt.setInt(index++, numberCommits);
		stmt.setInt(index++, numberRuns);
		stmt.setInt(index++, extension);
		stmt.setBoolean(index++, canReleaseTest);
		stmt.setTimestamp(index++, lastBuildRequestTimestamp);
	}

	public void update(Connection conn)
	throws SQLException
	{
	    String update =
	        " UPDATE " + TABLE_NAME +
	        " SET " +
	        " partner_sr_pk = ?, " +
	        " one_time_password = ?, " +
	        " number_submissions = ?, " +
	        " number_commits = ?, " +
	        " number_runs = ?, " +
	        " extension = ?, " +
	        " can_release_test = ?, " +
	        " last_build_request_timestamp = ? " +
	        " WHERE student_registration_pk = ? " +
	        " AND project_pk = ? ";
	    PreparedStatement stmt=null;
	    try {
	        // update statement
	        int index=1;
			stmt = conn.prepareStatement(update);
			Queries.setStatement(stmt, getPartnerPK(), getOneTimePassword(),
					getNumberSubmissions(), getNumberCommits(), getNumberRuns(),
					getExtension(),
					getCanReleaseTest(),
					getLastBuildRequestTimestamp(),
					getStudentRegistrationPK(),
					getProjectPK());
			stmt.executeUpdate();
	    } finally {
			    if (stmt != null) stmt.close();
	    }
	}

	public StudentSubmitStatus() {}

	public static StudentSubmitStatus findOrCreate(
	        @Project.PK int projectPK,
	        @StudentRegistration.PK int studentRegistrationPK,
	        @Nonnull Connection conn)
	throws SQLException
	{
	    StudentSubmitStatus studentSubmitStatus = new StudentSubmitStatus();
	    studentSubmitStatus.projectPK = projectPK;
	    studentSubmitStatus.studentRegistrationPK = studentRegistrationPK;
	    studentSubmitStatus.oneTimePassword = MarmosetUtilities.nextRandomPassword(); // [NAT P002]
	    return studentSubmitStatus.findOrInsert(conn);
	}

	private int executeUpdate(PreparedStatement stmt) throws SQLException {
		try {
			return stmt.executeUpdate();
		} finally {
			try {
					stmt.close();
			} catch (SQLException ignore) {
				// ignore
			}
		}
	}

	public static @CheckForNull StudentSubmitStatus lookupByStudentRegistrationPKAndProjectPK(
			@StudentRegistration.PK Integer studentRegistrationPK,
			@Project.PK Integer projectPK,
			 @Nonnull  Connection conn)
			throws SQLException
	{
		String query =
			"SELECT " + ATTRIBUTES +
			" FROM " + TABLE_NAME +
			" WHERE student_registration_pk = ? " +
			" AND project_pk = ?";

		PreparedStatement stmt = conn.prepareStatement(query);
		SqlUtilities.setInteger(stmt, 1, studentRegistrationPK);
        SqlUtilities.setInteger(stmt, 2, projectPK);

        StudentSubmitStatus result = getFromPreparedStatement(stmt);

        return result;
	}
	
	public static void updateLastBuildRequest(
			@StudentRegistration.PK Integer studentRegistrationPK,
			@Project.PK Integer projectPK,
			Timestamp lastBuildRequest,
			 @Nonnull  Connection conn)
			throws SQLException {
		
		String query =
				 " UPDATE " + TABLE_NAME +
			        " SET " +
			        " last_build_request_timestamp = ? " +
			" WHERE student_registration_pk = ? " +
			" AND project_pk = ?";

		PreparedStatement stmt = conn.prepareStatement(query);

		stmt.setTimestamp(1, lastBuildRequest);
		SqlUtilities.setInteger(stmt, 2, studentRegistrationPK);
        SqlUtilities.setInteger(stmt, 3, projectPK);

        stmt.executeUpdate();


	}


    public static Map<Integer, StudentSubmitStatus> lookupAllByProjectPK(
            @Project.PK Integer projectPK,
            Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " + ATTRIBUTES +
            " FROM " + TABLE_NAME +
            " WHERE project_pk = ?";

        PreparedStatement stmt = conn.prepareStatement(query);
        SqlUtilities.setInteger(stmt, 1, projectPK);

        return getAllFromPreparedStatement(stmt);
    }


	private static StudentSubmitStatus getFromPreparedStatement(PreparedStatement stmt)
	throws SQLException
	{
	    try {
			ResultSet rs = stmt.executeQuery();

			if (rs.next())
			{
				StudentSubmitStatus studentSubmitStatus = new StudentSubmitStatus();
				studentSubmitStatus.fetchValues(rs, 1);
				return studentSubmitStatus;
			}
			return null;
		}
		finally
		{
			if (stmt != null)
			{
				try {
					stmt.close();
				} catch (SQLException ignore) {
					// Ignore
				}
			}
		}
	}
     private static Map<Integer, StudentSubmitStatus> getAllFromPreparedStatement(PreparedStatement stmt)
        throws SQLException
        {
            try {
                ResultSet rs = stmt.executeQuery();

                Map<Integer, StudentSubmitStatus> map = new LinkedHashMap<Integer, StudentSubmitStatus>();
                while (rs.next())
                {
                    StudentSubmitStatus studentSubmitStatus = new StudentSubmitStatus();
                    studentSubmitStatus.fetchValues(rs, 1);
                    map.put(studentSubmitStatus.getStudentRegistrationPK(), studentSubmitStatus);
                }
                return map;
            }
            finally {
                Queries.closeStatement(stmt);
            }
        }

    // [NAT]
 	/**
 	 * Ensure that existing team accounts in the course do not have access to release test
 	 * in this project
 	 * @param conn
 	 * @param projectPK
 	 * @throws SQLException
 	 */
 	public static void banExistingTeamsFromProject(Connection conn,
 			@Course.PK Integer coursePK, @Project.PK Integer projectPK)
	throws SQLException
	{
 		// get teams in course corresponding to project
 		List<StudentRegistration> courseTeams =
 			StudentRegistration.lookupAllTeamsByCoursePK(coursePK, conn);

 		if (courseTeams == null || courseTeams.isEmpty()) return; // nothing to do

 		List<Integer> studentRegistrationPKs = new ArrayList<Integer>();
 		for (StudentRegistration reg : courseTeams)
 			studentRegistrationPKs.add(reg.getStudentRegistrationPK());

 		// ban them
 		banStudentsFromProject(conn, projectPK, studentRegistrationPKs);
	}

    // [NAT]
 	/**
 	 * Ban a set of students/teams from release testing on a project. Insert values into
 	 * student_submit_status if they do not already exist.
 	 * @param projectPK project that students are not eligible for
 	 * @param studentRegistrationPKs registration pks for students/teams
 	 * @throws SQLException
 	 */
 	public static void banStudentsFromProject(Connection conn,
 			@Project.PK Integer projectPK, List<Integer> studentRegistrationPKs)
	throws SQLException
	{
 		// for each student,
 		for (Integer srPK : studentRegistrationPKs)
 		{
 		    @StudentRegistration.PK int studentRegistrationPK = StudentRegistration.asPK(srPK);
 			StudentSubmitStatus studentSubmitStatus =
 				lookupByStudentRegistrationPKAndProjectPK(
 						studentRegistrationPK, projectPK, conn);

 			// insert student_submit_status entry if it does not exist
 			if (studentSubmitStatus == null) {

 	            studentSubmitStatus = StudentSubmitStatus.findOrCreate(
 	            		projectPK, studentRegistrationPK, conn);
 			}

 			// update entry with can_release_test = false
			studentSubmitStatus.setCanReleaseTest(false);
			studentSubmitStatus.update(conn);
 		}
	}

 	/**
 	 * Prevent a student from release testing on any of the projects that
 	 * currently exist in a course, except the one specified
 	 * @param conn
 	 * @param coursePK course containing projects student is to be banned from
 	 * @param studentRegistrationPK student to be banned
 	 * @param projectPK the project to exclude from the banning process
 	 * @throws SQLException
 	 */
 	public static void banStudentFromExistingProjects(Connection conn,
 			@Course.PK Integer coursePK, @StudentRegistration.PK Integer studentRegistrationPK, @Project.PK Integer projectPK)
	throws SQLException
	{
 		List<Integer> studentRegistrationPKs = new ArrayList<Integer>();
 		studentRegistrationPKs.add(studentRegistrationPK);

 		// Get all the projects in the course
 		List<Project> projects = Project.lookupAllByCoursePK(coursePK, conn);

 		// bar student from each project unless it is the excluded project
 		for (Project p : projects) {
 			if (p.getProjectPK() != projectPK) 
 			  banStudentsFromProject(conn, p.getProjectPK(), studentRegistrationPKs);
 		}
	}

}

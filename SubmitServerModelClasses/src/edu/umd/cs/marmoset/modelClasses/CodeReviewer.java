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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifier;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * A CodeReviewer is a person in the role of commenting on a specific submission.
 * For any pair of student and submission, there is at most one code reviewer. 
 * A code reviewer can be assigned to one code review assignment, or it can be an
 * ad-hoc code review. 
 * 
 * @author pugh
 *
 */
public class CodeReviewer implements Comparable<CodeReviewer> {
	
	public static class Builder {
		private final Connection conn;
		private final @Submission.PK int submission;
		
		private @CodeReviewAssignment.PK int assignment = 0;
		private @Student.PK int student = 0;
		private boolean isAuthor = false;
		private boolean isInstructor = false;
		private boolean isAutomated = false;
		private String knownAs = "";
		
		public Builder(Connection conn, @Submission.PK int submission) {
			this.conn = conn;
			this.submission = submission;
		}
		
		public Builder setAssignment(CodeReviewAssignment assignment) {
			this.assignment = Preconditions.checkNotNull(assignment).getCodeReviewAssignmentPK();
			return this;
		}
		
		public Builder setStudent(Student student, boolean isAuthor, boolean isInstructor) {
			Preconditions.checkState(isAutomated == false, "Can't set student on automated reviewer.");
			this.student = Preconditions.checkNotNull(student).getStudentPK();
			this.isInstructor = isInstructor;
			this.isAuthor = isAuthor;
			return this;
		}
		
		public Builder setInstructor() {
			this.isInstructor = true;
			return this;
		}
		
		public Builder setAutomated(String knownAs) {
			Preconditions.checkArgument(!Strings.isNullOrEmpty(knownAs), "Must provide knownAs for automated reviewer");
			Preconditions.checkState(this.student == 0, "Reviewer is already set to automated.");
			this.isAutomated = true;
			this.knownAs = knownAs;
			return this;
		}
		
		public CodeReviewer build() throws SQLException {
			return new CodeReviewer(this);
		}
	}

	@Documented
	@TypeQualifier(applicableTo = Integer.class)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface PK {}

	public static @PK int asPK(int pk) {
		return pk;
	}
	public static @PK Integer asPK(Integer pk) {
		return pk;
	}
	
	
	public static final String TABLE_NAME = "code_reviewer";

	/**
	 * List of all attributes for courses table.
	 */
	static final String[] ATTRIBUTE_NAME_LIST = {
		"code_reviewer_pk",
		"code_review_assignment_pk",
		"submission_pk",
		"student_pk",
		"is_author",
		"is_instructor",
		"last_update",
		"num_comments",
		"known_as",
		"is_automated"
	};

	/**
	 * Fully-qualified attributes for courses table.
	 */
	 public static final String ATTRIBUTES = Queries.getAttributeList(TABLE_NAME,
			ATTRIBUTE_NAME_LIST);

	private final @CodeReviewer.PK int codeReviewerPK; // primary key
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + codeReviewerPK;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CodeReviewer))
			return false;
		CodeReviewer other = (CodeReviewer) obj;
		if (codeReviewerPK != other.codeReviewerPK)
			return false;
		return true;
	}
	private  @CodeReviewAssignment.PK int codeReviewAssignmentPK;
	private final  @Submission.PK int submissionPK;
	private final  @Student.PK int studentPK;
	private final boolean isAuthor;
	private @Nonnull String knownAs;
	private Timestamp lastUpdate = null;
	private final boolean isInstructor;
	private final boolean isAutomated;
	
	public boolean isAutomated() {
		return isAutomated;
	}
	
	public boolean isInstructor() {
		return isInstructor;
	}

	public Timestamp getLastUpdate() {
		return lastUpdate;
	}

	public int getNumComments() {
		return numComments;
	}
	private int numComments = 0;

	private Submission submission;
	private @CheckForNull CodeReviewAssignment codeReviewAssignment;
	private Student student;

	public boolean isAuthor() {
		return isAuthor;
	}
	
	public String getKnownAs() {
		return knownAs;
	}
	
	   
    public void setKnownAs(String knownAs) {
        this.knownAs = knownAs;
    }
	
	public String getName() {
	    
		if (knownAs.length() > 0)
			return knownAs;
		
		if (Student.FAKE_NAMES)
            return FakeNames.getFullname(studentPK);
	    
		if (student == null)
		    throw new IllegalStateException("No student record for CodeReviewer " + codeReviewerPK + ", student " + studentPK);
		return student.getFullname();
	}
	
	public String getNameForInstructor() {
		if (student == null)
		    throw new IllegalStateException("No student record for CodeReviewer " + codeReviewerPK + ", student " + studentPK);
		
		if (knownAs.length() > 0)
			return String.format("%s (%s)", student.getFullname(), knownAs);
		
		return student.getFullname();
	}

	public @CodeReviewer.PK int getCodeReviewerPK() {
		return codeReviewerPK;
	}

	public @Student.PK int getStudentPK() {
		return studentPK;
	}


	public boolean isAssignment() {
		return codeReviewAssignmentPK != 0;
	}
	/**
	 *
	 * @return 0 if no assignment
	 */
	public @CodeReviewAssignment.PK int getCodeReviewAssignmentPK() {
		return codeReviewAssignmentPK;
	}

	public @Submission.PK int getSubmissionPK() {
		return submissionPK;
	}

	public Submission getSubmission() {
		return submission;
	}

	public @CheckForNull CodeReviewAssignment getCodeReviewAssignment() {
		return codeReviewAssignment;
	}
	public Student getStudent() {
		return student;
	}

	public void addComments(Connection conn, int num, Timestamp now) throws SQLException {
	    if (num == 0)
	        return;
		String query = "UPDATE  " + TABLE_NAME
				+ " SET num_comments = num_comments+?, last_update = ? "
				+ " WHERE code_reviewer_pk = ?";

		PreparedStatement stmt = conn.prepareStatement(query);
		try {
			Queries.setStatement(stmt, num, now, codeReviewerPK);
			stmt.executeUpdate();
			lastUpdate = now;
			numComments += num;
		} finally {
			stmt.close();
		}
	}
	public void addComment(Connection conn, Timestamp now) throws SQLException {
		String query = "UPDATE  " + TABLE_NAME
				+ " SET num_comments = num_comments+1, last_update = ? "
				+ " WHERE code_reviewer_pk = ?";

		PreparedStatement stmt = conn.prepareStatement(query);
		try {
			Queries.setStatement(stmt, now, codeReviewerPK);
			stmt.executeUpdate();
			lastUpdate = now;
			numComments++;
		} finally {
			stmt.close();
		}
	}	

	public static CodeReviewer lookupOrInsertAuthor(Connection conn, Submission submission, 
	        @CheckForNull CodeReviewAssignment assignment,
			String authorKnownAs) throws SQLException {
		StudentRegistration sr = StudentRegistration.lookupByStudentRegistrationPK(submission.getStudentRegistrationPK(), conn);
        int submissionPK = submission.getSubmissionPK();
        int studentPK = sr.getStudentPK();
        int assignmentPK = 0;
        if (assignment != null)
            assignmentPK = assignment.getCodeReviewAssignmentPK();

		CodeReviewer result = lookupBySubmissionAndStudentPK(submissionPK, studentPK, conn);
        if (result != null) {
        	if (true != result.isAuthor)
        		throw new IllegalArgumentException("Existing codeReviewer has inconsistent isAuthor value");
        	if (!authorKnownAs.equals(result.getKnownAs()) || result.getCodeReviewAssignmentPK() != assignmentPK) {
        	    result.setKnownAs(authorKnownAs);
        	    result.codeReviewAssignmentPK = assignmentPK;
        	    String u = "UPDATE  " + TABLE_NAME + " SET code_review_assignment_pk = ?, known_as = ? "
                        + " WHERE code_reviewer_pk = ?";
                PreparedStatement update = Queries.setStatement(conn, u, assignmentPK, authorKnownAs,
                        result.getCodeReviewerPK());
                update.execute();
        	}
        	    
        	return result;
        }
        return new CodeReviewer(conn, assignment == null ? 0 : assignment.getCodeReviewAssignmentPK(), submissionPK, studentPK, authorKnownAs, true, sr.isInstructor(), false);
	}
	
	public static @Nonnull CodeReviewer lookupOrAddReviewer(Connection conn,  Submission submission,
           StudentRegistration commenter) throws SQLException {
	    
	    CodeReviewer reviewer = CodeReviewer.lookupOrAddReviewer(conn, submission.getSubmissionPK(),
	            commenter.getStudentPK(), "",
                submission.getStudentRegistrationPK() == commenter.getStudentRegistrationPK(),
                commenter.isInstructor());
	    if (commenter.isInstructor())
            submission.acceptHelpRequest(conn);
	    return reviewer;
	    
	}
	public static @Nonnull CodeReviewer lookupOrAddReviewer(Connection conn,  @Submission.PK int submissionPK,
			@Student.PK int studentPK, String knownAs, boolean isAuthor, boolean isInstructor) throws SQLException {
		CodeReviewer result = lookupBySubmissionAndStudentPK(submissionPK, studentPK, conn);
		if (result != null) {
			if (isAuthor && !result.isAuthor)
				throw new IllegalArgumentException("Existing codeReviewer has inconsistent isAuthor value");
			return result;
		}
		return new CodeReviewer(conn, 0, submissionPK, studentPK, knownAs, isAuthor, isInstructor, false);
	}

	public static boolean deleteInactiveReviewers(Connection conn, 
	       CodeReviewAssignment assignment)  throws SQLException {
	    String u = "DELETE FROM " + TABLE_NAME + " WHERE code_review_assignment_pk = ? "
	            + " AND last_update is NULL";
	    PreparedStatement update = Queries.setStatement(conn, u, assignment.getCodeReviewAssignmentPK());
	    try {
	        return update.execute();
	    } finally {
	        Queries.closeStatement(update);
	    }
	}
	   public static boolean deleteInactiveReviewers(Connection conn, 
	           CodeReviewAssignment assignment, Submission submission)  throws SQLException {
	        String u = "DELETE FROM " + TABLE_NAME + " WHERE code_review_assignment_pk = ? "
	                + " AND submission_pk = ? "
	                + " AND last_update is NULL";
	        PreparedStatement update = Queries.setStatement(conn, u, assignment.getCodeReviewAssignmentPK(), submission.getSubmissionPK());
	        try {
	            return update.execute();
	        } finally {
	            Queries.closeStatement(update);
	        }
	    }
	   
	   public static boolean deleteInactiveReviewers(Connection conn, 
                Submission submission)  throws SQLException {
            String u = "DELETE FROM " + TABLE_NAME + " WHERE "
                    + " AND submission_pk = ? "
                    + " AND last_update is NULL";
            PreparedStatement update = Queries.setStatement(conn, u,  submission.getSubmissionPK());
            try {
                return update.execute();
            } finally {
                Queries.closeStatement(update);
            }
        }
	   public static int numInactiveReviewers(Connection conn, 
               CodeReviewAssignment assignment)  throws SQLException {
            String u = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE code_review_assignment_pk = ? "
                    + " AND last_update is NULL";
            PreparedStatement query = Queries.setStatement(conn, u,  assignment.getCodeReviewAssignmentPK());
            try {
                ResultSet rs = query.executeQuery();
                if (rs.next())
                return rs.getInt(1);
                return 0;
            } finally {
                Queries.closeStatement(query);
            }
        }
	   public static int numActiveReviewers(Connection conn, 
               CodeReviewAssignment assignment)  throws SQLException {
            String u = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE code_review_assignment_pk = ? "
                    + " AND last_update is not NULL";
            PreparedStatement query = Queries.setStatement(conn, u,  assignment.getCodeReviewAssignmentPK());
            try {
                ResultSet rs = query.executeQuery();
                if (rs.next())
                return rs.getInt(1);
                return 0;
            } finally {
                Queries.closeStatement(query);
            }
        }
	   public static int numInactiveReviewers(Connection conn, 
               Submission submission)  throws SQLException {
            String u = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE "
                    + " AND submission_pk = ? "
                    + " AND last_update is NULL";
            PreparedStatement query = Queries.setStatement(conn, u,  submission.getSubmissionPK());
            try {
                ResultSet rs = query.executeQuery();
                if (rs.next())
                return rs.getInt(1);
                return 0;
            } finally {
                Queries.closeStatement(query);
            }
        }
    public static CodeReviewer updateOrInsert(Connection conn, 
    		@CodeReviewAssignment.PK int codeReviewAssignmentPK,
            @Submission.PK int submissionPK, @Student.PK int studentPK,
            @Nonnull String knownAs, boolean isAuthor, boolean isInstructor)
            throws SQLException {
        CodeReviewer result = lookupBySubmissionAndStudentPK(submissionPK, studentPK, conn);
        if (result != null) {
            if (isAuthor != result.isAuthor)
                throw new IllegalArgumentException(
                        String.format("Existing codeReviewer %d has inconsistent isAuthor value of %s for reviewer %d",
                                result.getCodeReviewerPK(), result.isAuthor,
                                studentPK));

            if (isInstructor != result.isInstructor)
                throw new IllegalArgumentException(
                        String.format("Existing codeReviewer %d has inconsistent isInstructor value of %s for reviewer %d",
                                result.getCodeReviewerPK(), result.isInstructor,
                                studentPK));
            if (codeReviewAssignmentPK != 0) {
                result.codeReviewAssignmentPK = codeReviewAssignmentPK;
                String u = "UPDATE  " + TABLE_NAME + " SET code_review_assignment_pk = ? "
                        + " WHERE code_reviewer_pk = ?";
                PreparedStatement update = Queries.setStatement(conn, u, codeReviewAssignmentPK,
                        result.getCodeReviewerPK());
                update.execute();
            }

            return result;
        }

        return new CodeReviewer(conn, codeReviewAssignmentPK, submissionPK, studentPK, knownAs, isAuthor, isInstructor, false);
    }

	public  CodeReviewer(Connection conn, @CodeReviewAssignment.PK int codeReviewAssignmentPK,   
			@Submission.PK int submissionPK,
			@Student.PK int studentPK, String knownAs, boolean isAuthor, boolean isInstructor)
	throws SQLException
	{
		this(conn, codeReviewAssignmentPK, submissionPK, studentPK, knownAs,
				isAuthor, isInstructor, false);
	}
	
	private CodeReviewer(Builder builder) throws SQLException {
		this(builder.conn, builder.assignment, builder.submission,
				builder.student, builder.knownAs, builder.isAuthor,
				builder.isInstructor, builder.isAutomated);
	}

	public CodeReviewer(Connection conn,
			@CodeReviewAssignment.PK int codeReviewAssignmentPK,
			@Submission.PK int submissionPK, @Student.PK int studentPK,
			String knownAs, boolean isAuthor, boolean isInstructor,
			boolean isAutomated)
	throws SQLException
	{
	    String insert = Queries.makeInsertStatementUsingSetSyntax(ATTRIBUTE_NAME_LIST, TABLE_NAME, true);

	    this.codeReviewAssignmentPK = codeReviewAssignmentPK;
	    this.submissionPK = submissionPK;
	    this.studentPK = studentPK;
	    this.isAuthor = isAuthor;
	    this.isInstructor = isInstructor;
	    this.knownAs = knownAs;
	    this.isAutomated = isAutomated;
	    
	    PreparedStatement stmt = null;
	    try {
	        stmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
	        int col = 1;
	        stmt.setInt(col++, codeReviewAssignmentPK);
	        stmt.setInt(col++, submissionPK);
	        stmt.setInt(col++, studentPK);
	        stmt.setBoolean(col++, isAuthor);
	        stmt.setBoolean(col++, isInstructor);
	        stmt.setTime(col++, null);
	        stmt.setInt(col++, 0);
	        stmt.setString(col++, knownAs);
	        stmt.setBoolean(col++, isAutomated);
	        stmt.executeUpdate();

	        this.codeReviewerPK = CodeReviewer.asPK(Queries.getGeneratedPrimaryKey(stmt));
	        this.submission = Submission.lookupBySubmissionPK(this.submissionPK, conn);
	        this.student = Student.lookupByStudentPK(studentPK, conn);
	    } finally {
	        Queries.closeStatement(stmt);
	    }
	}

	public  CodeReviewer(ResultSet resultSet, int startingFrom)
	throws SQLException
	{
		this.codeReviewerPK = CodeReviewer.asPK(resultSet.getInt(startingFrom++));
		this.codeReviewAssignmentPK = CodeReviewAssignment.asPK(resultSet.getInt(startingFrom++));
		this.submissionPK = Submission.asPK(resultSet.getInt(startingFrom++));
		this.studentPK = Student.asPK(resultSet.getInt(startingFrom++));
		this.isAuthor = resultSet.getBoolean(startingFrom++);
		this.isInstructor = resultSet.getBoolean(startingFrom++);
		this.lastUpdate = resultSet.getTimestamp(startingFrom++);
		this.numComments = resultSet.getInt(startingFrom++);
		this.knownAs = resultSet.getString(startingFrom++);
		this.isAutomated = resultSet.getBoolean(startingFrom++);
	}
	public  CodeReviewer(ResultSet resultSet, int startingFrom, Connection conn)
	throws SQLException
	{
		this(resultSet, startingFrom);
		if (codeReviewAssignmentPK != 0)
			this.codeReviewAssignment = CodeReviewAssignment.lookupByPK(this.codeReviewAssignmentPK, conn);
		this.submission = Submission.lookupBySubmissionPK(this.submissionPK, conn);
		this.student = Student.lookupByStudentPK(studentPK, conn);

	}

	public static CodeReviewer lookupByPK(int codeReviewerPK,
			Connection conn) throws SQLException {
		String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
				+ " WHERE code_reviewer_pk = ? ";

		PreparedStatement stmt = conn.prepareStatement(query);
		try {
			stmt.setInt(1, codeReviewerPK);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				return new CodeReviewer(rs, 1, conn);
			return null;
		} finally {
			stmt.close();
		}
	}
	public static @CheckForNull CodeReviewer lookupBySubmissionAndStudentPK( @Submission.PK int submissionPK,
			@Student.PK int studentPK,
			Connection conn) throws SQLException {
		String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
				+ " WHERE submission_pk = ? "
				+ " AND student_pk = ? ";

		PreparedStatement stmt = conn.prepareStatement(query);
		try {
			stmt.setInt(1, submissionPK);
			stmt.setInt(2, studentPK);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				return new CodeReviewer(rs, 1, conn);
			return null;
		} finally {
			stmt.close();
		}
	}
	public static @CheckForNull CodeReviewer lookupAuthorBySubmission(
			@Submission.PK int submissionPK, Connection conn) throws SQLException {
		String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
				+ " WHERE submission_pk = ? "
				+ " AND is_author = ? ";

		PreparedStatement stmt = conn.prepareStatement(query);
		try {
			stmt.setInt(1, submissionPK);
			stmt.setBoolean(2, true);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				return new CodeReviewer(rs, 1, conn);
			return null;
		} finally {
			stmt.close();
		}
	}

	public static @CheckForNull CodeReviewer lookupByCodeReviewAssignmentSubmissionAndStudentPK(
			int codeReviewAssignmentPK, @Submission.PK int submissionPK,
			@Student.PK int studentPK,
			Connection conn) throws SQLException {
		String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
				+ " WHERE code_review_assignment_pk = ? "
				+ " AND submission_pk = ? "
				+ " AND student_pk = ? ";

		PreparedStatement stmt = conn.prepareStatement(query);
		try {
			stmt.setInt(1, codeReviewAssignmentPK);
			stmt.setInt(2, submissionPK);
			stmt.setInt(3, studentPK);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				return new CodeReviewer(rs, 1, conn);
			return null;
		} finally {
			stmt.close();
		}
	}
	public static Collection<CodeReviewer> lookupByCodeReviewAssignmentPK(int codeReviewAssignmentPK,
			Connection conn) throws SQLException {
		String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
				+ " WHERE code_review_assignment_pk = ? ";

		PreparedStatement stmt = conn.prepareStatement(query);
		try {
			stmt.setInt(1, codeReviewAssignmentPK);
			ResultSet rs = stmt.executeQuery();
			LinkedList<CodeReviewer> result = new LinkedList<CodeReviewer>();

			while (rs.next())
				result.add( new CodeReviewer(rs, 1, conn));
			return result;
		} finally {
			stmt.close();
		}
	}
	public static Collection<CodeReviewer> getAll(
            Connection conn) throws SQLException {
        String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME;

        PreparedStatement stmt = conn.prepareStatement(query);
        try {
            ResultSet rs = stmt.executeQuery();
            ArrayList<CodeReviewer> result = new ArrayList<CodeReviewer>();

            while (rs.next())
                result.add( new CodeReviewer(rs, 1, conn));
            return result;
        } finally {
            stmt.close();
        }
    }
	public static Collection<CodeReviewer> lookupBySubmissionPK( @Submission.PK int submissionPK,
			Connection conn) throws SQLException {
		String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
				+ " WHERE submission_pk = ? ";

		PreparedStatement stmt = conn.prepareStatement(query);
		try {
			stmt.setInt(1, submissionPK);
			ResultSet rs = stmt.executeQuery();
			LinkedList<CodeReviewer> result = new LinkedList<CodeReviewer>();

			while (rs.next())
				result.add( new CodeReviewer(rs, 1, conn));
			return result;
		} finally {
			stmt.close();
		}
	}

	public static Collection<CodeReviewer> lookupByStudent(Student student,
			Connection conn) throws SQLException {
		return lookupByStudentPK(student.getStudentPK(), conn);
	}
	public static Collection<CodeReviewer> lookupByStudentPK(
			@Student.PK int studentPK,
			Connection conn) throws SQLException {
		String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
				+ " WHERE code_reviewer.student_pk =  ? ";

		PreparedStatement stmt = conn.prepareStatement(query);
		try {
			stmt.setInt(1, studentPK);
			ResultSet rs = stmt.executeQuery();
			LinkedList<CodeReviewer> result = new LinkedList<CodeReviewer>();

			while (rs.next())
				result.add( new CodeReviewer(rs, 1, conn));
			return result;
		} finally {
			stmt.close();
		}
	}
	
	public @CheckForNull @CodeReviewer.PK Integer getNext(Connection conn) throws SQLException {
	    if (codeReviewAssignmentPK == 0)
	        return null;
	    String query = "SELECT code_reviewer_pk FROM " + TABLE_NAME
                + " WHERE student_pk =  ? " 
                + " AND code_review_assignment_pk = ? "
                + " AND code_reviewer_pk  > ? "
                + " ORDER BY code_reviewer_pk "
                + " LIMIT 1";
	    PreparedStatement stmt = Queries.setStatement(conn, query, getStudentPK(),
	            getCodeReviewAssignmentPK(), getCodeReviewerPK());
	    try {
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next())
	            return CodeReviewer.asPK(rs.getInt(1));
	        
	        return null;
	    }  finally {
            stmt.close();
        }

	    
	    
	}

	@Override
	public int compareTo(CodeReviewer o) {
		return this.codeReviewerPK - o.codeReviewerPK;
	}

	public boolean isOmniscient() {
		if (isInstructor)
			return true;
		if (codeReviewAssignment == null)
			return false;
		Timestamp now = new Timestamp(System.currentTimeMillis());
		if (codeReviewAssignment.getDeadline().before(now))
			return true;
		return false;


	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("pk", this.codeReviewerPK)
				.add("knownAs", knownAs).toString();
	}

}

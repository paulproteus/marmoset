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
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;

public class CodeReviewComment implements Comparable<CodeReviewComment> {
	public static final String TABLE_NAME = "code_review_comment";

	static final String[] ATTRIBUTE_NAME_LIST = { "code_review_comment_pk",
			"code_review_thread_pk", "code_reviewer_pk", "comment", "draft","ack",
			"modified" };

	public static final String ATTRIBUTES = Queries.getAttributeList(
			TABLE_NAME, ATTRIBUTE_NAME_LIST);

	private final int codeReviewCommentPK;

	private final @CodeReviewer.PK  int codeReviewerPK; 
	private final @CodeReviewThread.PK int codeReviewThreadPK;
	private String comment;
	private boolean draft;
	private boolean ack;
	private Timestamp modified;

	@Override
    public int hashCode() {
        return codeReviewCommentPK;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof CodeReviewComment))
            return false;
        CodeReviewComment other = (CodeReviewComment) obj;
        if (codeReviewCommentPK != other.codeReviewCommentPK)
            return false;
        return true;
    }

    public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	public boolean isAck() {
		return ack;
	}
	public boolean isJustAck() {
	    return this.ack && comment.equals("Acknowledged.");
	  }

	public void setAck(boolean ack) {
		this.ack = ack;
	}

	public Timestamp getModified() {
		return modified;
	}

	public void setModified(Timestamp modified) {
		this.modified = modified;
	}

	public   int getCodeReviewCommentPK() {
		return codeReviewCommentPK;
	}

	public @CodeReviewer.PK int getCodeReviewerPK() {
		return codeReviewerPK;
	}

	public @CodeReviewThread.PK int getCodeReviewThreadPK() {
		return codeReviewThreadPK;
	}


	public CodeReviewComment(@CodeReviewThread.PK int codeReviewThreadPK,
			@CodeReviewer.PK int codeReviewerPK,
			String comment, boolean draft, boolean ack, Timestamp modified, Connection conn)
			throws SQLException {
		assert !(draft && ack);
		this.codeReviewerPK = codeReviewerPK;
		this.codeReviewThreadPK = codeReviewThreadPK;
		this.comment = comment;
		this.draft = draft;
		this.ack = ack;
		this.modified = modified;
		String insert = Queries.makeInsertStatementUsingSetSyntax(
				ATTRIBUTE_NAME_LIST, TABLE_NAME, true);
		PreparedStatement stmt = conn.prepareStatement(insert,
				Statement.RETURN_GENERATED_KEYS);
		try {
			Queries.setStatement(stmt, codeReviewThreadPK, codeReviewerPK,
					comment, draft, ack, modified);

			stmt.executeUpdate();

			this.codeReviewCommentPK = Queries.getGeneratedPrimaryKey(stmt);
		} finally {
			Queries.closeStatement(stmt);
		}
	}

	public CodeReviewComment(CodeReviewThread thread,
			CodeReviewer reviewer, String text, Timestamp now, boolean draft,
			Connection conn) throws SQLException {
		this( thread.getCodeReviewThreadPK(),
				reviewer.getStudentPK(), text, draft, true, now, conn);
	}

	public CodeReviewComment(ResultSet rs, int from) throws SQLException {
		this.codeReviewCommentPK = rs.getInt(from++);
		this.codeReviewThreadPK = CodeReviewThread.asPK(rs.getInt(from++));
		this.codeReviewerPK = CodeReviewer.asPK(rs.getInt(from++));
		this.comment = rs.getString(from++);
		this.draft = rs.getBoolean(from++);
		this.ack = rs.getBoolean(from++);
		this.modified = rs.getTimestamp(from++);
	}

	public static CodeReviewComment lookupByPK(int codeReviewCommentPK,
			Connection conn) throws SQLException {
		String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
				+ " WHERE code_review_comment_pk = ? ";

		PreparedStatement stmt = conn.prepareStatement(query);
		try {
			stmt.setInt(1, codeReviewCommentPK);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				return new CodeReviewComment(rs, 1);
			return null;
		} finally {
			stmt.close();
		}
	}

	public static Collection<CodeReviewComment> lookupByThreadPK(
			int codeReviewThreadPK, Connection conn) throws SQLException {
		String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
				+ " WHERE code_review_thread_pk = ? "
				+ " ORDER BY modified ASC";

		PreparedStatement stmt = conn.prepareStatement(query);
		try {
			stmt.setInt(1, codeReviewThreadPK);
			ResultSet rs = stmt.executeQuery();
			LinkedList<CodeReviewComment> result = new LinkedList<CodeReviewComment>();

			while (rs.next())
				result.add(new CodeReviewComment(rs, 1));
			return result;
		} finally {
			stmt.close();
		}
	}



	public static Collection<CodeReviewComment> lookupBySubmissionPK(
			 @Submission.PK int submissionPK, Connection conn) throws SQLException {
		String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME  + ", " + CodeReviewThread.TABLE_NAME
				+ " WHERE code_review_thread.submission_pk = ? "
				+ " AND code_review_thread.code_review_thread_pk = code_review_comment.code_review_thread_pk "
				+ " ORDER BY  code_review_comment.code_review_thread_pk ASC, code_review_comment.modified ASC";

		PreparedStatement stmt = conn.prepareStatement(query);
		try {
			stmt.setInt(1, submissionPK);
			ResultSet rs = stmt.executeQuery();
			LinkedList<CodeReviewComment> result = new LinkedList<CodeReviewComment>();

			while (rs.next())
				result.add(new CodeReviewComment(rs, 1));
			return result;
		} finally {
			stmt.close();
		}
	}

	public  static void publishAll(CodeReviewer codeReviewer, Timestamp now, Connection conn) throws SQLException {
        String query = "UPDATE " + TABLE_NAME  + " , " + CodeReviewThread.TABLE_NAME
            + " SET " + TABLE_NAME + ".draft=?, "
                   + TABLE_NAME +".modified=? "
            + " WHERE " + TABLE_NAME + ".code_reviewer_pk = ? "
            + " AND " + CodeReviewThread.TABLE_NAME +".submission_pk = ?"
            + " AND " + TABLE_NAME + ".draft=? "
            + " AND " + TABLE_NAME +".code_review_thread_pk = " + CodeReviewThread.TABLE_NAME + ".code_review_thread_pk ";

        PreparedStatement stmt = conn.prepareStatement(query);

        try {
            Queries.setStatement(stmt, false, now,  codeReviewer.getCodeReviewerPK(), codeReviewer.getSubmissionPK(), true);
            stmt.execute();
        } finally {
            stmt.close();
        }
    }


	public  void publish(Timestamp now, Connection conn) throws SQLException {
	    if (!this.draft)
	        return;
		String query = "UPDATE " + TABLE_NAME
		    + "SET draft=?, modified=? where code_review_comment_pk = ? AND draft=?";

		this.modified = now;
		this.draft = false;
		PreparedStatement stmt = conn.prepareStatement(query);
		try {
			Queries.setStatement(stmt, false, now, codeReviewCommentPK, true);
			stmt.execute();
		} finally {
			stmt.close();
		}
	}
	public static void publish(
			int codeReviewCommentPK, Timestamp now, Connection conn) throws SQLException {
		String query = "UPDATE " + TABLE_NAME
		    + " SET draft=?, modified=? WHERE code_review_comment_pk = ? AND draft=?";

		PreparedStatement stmt = conn.prepareStatement(query);
		try {
			Queries.setStatement(stmt, false, now, codeReviewCommentPK, true);
			stmt.execute();
		} finally {
			stmt.close();
		}
	}
	public static void update(
			int codeReviewCommentPK, String text, boolean isAck, Timestamp now, Connection conn) throws SQLException {
		String query = "UPDATE " + TABLE_NAME
		    + " SET comment=?, ack = ?, modified=?  WHERE code_review_comment_pk = ?";

		PreparedStatement stmt = conn.prepareStatement(query);
		try {
			Queries.setStatement(stmt, text, isAck, now, codeReviewCommentPK);
			stmt.execute();
		} finally {
			stmt.close();
		}
	}
	public static void delete(
			int codeReviewCommentPK, Connection conn) throws SQLException {
		String query = "DELETE FROM " + TABLE_NAME
		    + " WHERE code_review_comment_pk = ?";

		PreparedStatement stmt = conn.prepareStatement(query);
		try {
			Queries.setStatement(stmt, codeReviewCommentPK);
			stmt.execute();
		} finally {
			stmt.close();
		}
	}
	@Override
	public int compareTo(CodeReviewComment that) {
		return this.codeReviewCommentPK - that.codeReviewCommentPK;
	}

	public static class Info {
		Info(ResultSet rs) throws SQLException {
			int col = 1;
			this.id = rs.getInt(col++);
			this.file = rs.getString(col++);
			this.line = rs.getInt(col++);
			this.comment = rs.getString(col++);
			this.ack = rs.getBoolean(col++);
		}
		Info(int id, String file, int line, String comment, boolean ack) {
			this.id = id;
			this.file = file;
			this.line = line;
			this.comment = comment;
			this.ack = ack;
		}
		public final int id;
		public final String file;
		public final int line;
		public final String comment;
		public final boolean ack;
	}

	public static Collection<Info> lookupDraftByAuthorAndSubmissionPK(
			Student student, Submission submission,
			Connection conn) throws SQLException {
		String query = "SELECT code_review_comment.code_review_comment_pk "
				+ ", code_review_thread.file "
				+ ", code_review_thread.line "
				+ ", code_review_comment.comment "
				+ ", code_review_comment.ack "
				+ " FROM " + TABLE_NAME + ", " + CodeReviewThread.TABLE_NAME
				+ " WHERE code_review_thread.submission_pk = ? "
				+ " AND code_review_comment.student_pk = ? "
				+ " AND code_review_comment.draft = ? "
				+ " AND code_review_comment.code_review_thread_pk = code_review_thread.code_review_thread_pk";

		PreparedStatement stmt = conn.prepareStatement(query);
		try {

			Queries.setStatement(stmt, submission.getSubmissionPK(),
					student.getStudentPK(), true);

			ResultSet rs = stmt.executeQuery();
			LinkedList<Info> result = new LinkedList<Info>();

			while (rs.next())
				result.add(new Info(rs));
			return result;
		} finally {
			stmt.close();
		}
	}
	
	public boolean isBy(CodeReviewer reviewer) {
		return reviewer != null && this.codeReviewerPK == reviewer.getCodeReviewerPK();
	}

}

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
import java.util.Collection;
import java.util.LinkedList;

import javax.annotation.meta.TypeQualifier;
import javax.annotation.meta.When;

import edu.umd.cs.marmoset.modelClasses.CodeReviewer.PK;

public class CodeReviewThread implements Comparable<CodeReviewThread> {

    @Documented
    @TypeQualifier(applicableTo = Integer.class)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PK {
    }

    public static @PK
    int asPK(int pk) {
        return pk;
    }

    public static @PK
    Integer asPK(Integer pk) {
        return pk;
    }

    public static final String TABLE_NAME = "code_review_thread";

    /**
     * List of all attributes for courses table.
     */
    static final String[] ATTRIBUTE_NAME_LIST = { "code_review_thread_pk", "submission_pk", "file", "line", "created_by",
            "created", "rubric_evaluation_pk", };

    /**
     * Fully-qualified attributes for courses table.
     */
    public static final String ATTRIBUTES = Queries.getAttributeList(TABLE_NAME, ATTRIBUTE_NAME_LIST);

    private final @CodeReviewThread.PK
    int codeReviewThreadPK;// non-NULL, autoincrement
    private final @Submission.PK
    int submissionPK;

    private final Timestamp created;
    private final String file;
    private final int line;
    private final @CodeReviewer.PK
    int createdBy;
    private @RubricEvaluation.PK
    int rubricEvaluationPK;

    public @CodeReviewer.PK
    int getCreatedBy() {
        return createdBy;
    }

    public boolean isCreatedBy(CodeReviewer cr) {
        return cr.getCodeReviewerPK() == createdBy;
    }

    public @Submission.PK
    int getSubmissionPK() {
        return submissionPK;
    }

    public @CodeReviewThread.PK
    int getCodeReviewThreadPK() {
        return codeReviewThreadPK;
    }

    public @RubricEvaluation.PK
    int getRubricEvaluationPK() {
        return rubricEvaluationPK;
    }

    public void setAndUpdateRubricEvaluationPK(Connection conn, @RubricEvaluation.PK int rubricEvaluationPK) throws SQLException {
        this.rubricEvaluationPK = rubricEvaluationPK;
        String query = "UPDATE " + TABLE_NAME + " SET  rubric_evaluation_pk = ?" + " WHERE code_review_thread_pk = ? ";

        PreparedStatement stmt = null;
        try {
            stmt = Queries.setStatement(conn, query, rubricEvaluationPK, codeReviewThreadPK);
            stmt.executeUpdate();
        } finally {
            Queries.closeStatement(stmt);
        }

    }

    public String getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }

    public Timestamp getCreated() {
        return created;
    }

    /**
     * @param codeReviewerPK
     * @param file
     * @param line
     * @param comment
     * @param when
     * @throws SQLException
     */
    public CodeReviewThread(Connection conn, @Submission.PK int submissionPK, String file, int line, Timestamp created,
            @CodeReviewer.PK int createdBy) throws SQLException {
        this(conn, submissionPK, file, line, created, createdBy, 0);
    }

    /**
     * @param file
     * @param line
     * @param rubricEvaluationPK
     *            TODO
     * @param codeReviewerPK
     * @param comment
     * @param when
     * @throws SQLException
     */
    public CodeReviewThread(Connection conn, @Submission.PK int submissionPK, String file, int line, Timestamp created,
            @CodeReviewer.PK int createdBy, @RubricEvaluation.PK int rubricEvaluationPK) throws SQLException {
        this.file = file;
        this.line = line;
        this.submissionPK = submissionPK;
        this.createdBy = createdBy;
        this.created = created;
        this.rubricEvaluationPK = rubricEvaluationPK;
        String insert = Queries.makeInsertStatementUsingSetSyntax(ATTRIBUTE_NAME_LIST, TABLE_NAME, true);
        PreparedStatement stmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
        try {
            Queries.setStatement(stmt, submissionPK, file, line, createdBy, created, rubricEvaluationPK);
            stmt.executeUpdate();
            this.codeReviewThreadPK = CodeReviewThread.asPK(Queries.getGeneratedPrimaryKey(stmt));
        } finally {
            Queries.closeStatement(stmt);
        }
    }

    /**
     * @param codeReviewCommentPK
     * @param codeReviewerPK
     * @param file
     * @param line
     * @param comment
     * @param modified
     * @throws SQLException
     */
    public CodeReviewThread(ResultSet resultSet, int startingFrom) throws SQLException {
        this.codeReviewThreadPK = CodeReviewThread.asPK(resultSet.getInt(startingFrom++));
        this.submissionPK = Submission.asPK(resultSet.getInt(startingFrom++));
        this.file = resultSet.getString(startingFrom++);
        this.line = resultSet.getInt(startingFrom++);
        this.createdBy = CodeReviewer.asPK(resultSet.getInt(startingFrom++));
        this.created = resultSet.getTimestamp(startingFrom++);
        this.rubricEvaluationPK = RubricEvaluation.asPK(resultSet.getInt(startingFrom++));
    }

    public static CodeReviewThread lookupByPK(int codeReviewThreadPK, Connection conn) throws SQLException {
        String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME + " WHERE code_review_thread_pk = ? ";

        PreparedStatement stmt = conn.prepareStatement(query);
        try {
            stmt.setInt(1, codeReviewThreadPK);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return new CodeReviewThread(rs, 1);
            return null;
        } finally {
            stmt.close();
        }
    }

    public static Collection<CodeReviewThread> lookupBySubmissionPK(@Submission.PK int submissionPK, Connection conn)
            throws SQLException {
        String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME + " WHERE submission_pk = ? " + " ORDER BY created ASC";

        PreparedStatement stmt = conn.prepareStatement(query);
        try {
            stmt.setInt(1, submissionPK);
            ResultSet rs = stmt.executeQuery();
            LinkedList<CodeReviewThread> result = new LinkedList<CodeReviewThread>();

            while (rs.next())
                result.add(new CodeReviewThread(rs, 1));
            return result;
        } finally {
            stmt.close();
        }
    }

    public static void delete(@CodeReviewThread.PK int codeReviewThreadPK, Connection conn) throws SQLException {
        String query = "DELETE FROM " + TABLE_NAME + " WHERE code_review_thread_pk = ?";

        PreparedStatement stmt = conn.prepareStatement(query);
        try {
            Queries.setStatement(stmt, codeReviewThreadPK);
            stmt.execute();
        } finally {
            stmt.close();
        }
    }

    @Override
    public int compareTo(CodeReviewThread that) {
        int result = this.file.compareTo(that.file);
        if (result != 0)
            return result;
        result = this.line - that.line;
        if (result != 0)
            return result;
        result = this.created.compareTo(that.created);
        if (result != 0)
            return result;
        return this.codeReviewThreadPK - that.codeReviewThreadPK;
    }

    @Override
    public int hashCode() {
        return codeReviewThreadPK;

    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CodeReviewThread))
            return false;
        CodeReviewThread other = (CodeReviewThread) obj;
        return codeReviewThreadPK == other.codeReviewThreadPK;
    }

}

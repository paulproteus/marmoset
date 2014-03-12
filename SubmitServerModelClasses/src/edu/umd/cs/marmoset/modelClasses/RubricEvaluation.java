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

import javax.annotation.meta.TypeQualifier;

public class RubricEvaluation {

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

  public static final String TABLE_NAME = "rubric_evaluations";

  /**
   * List of all attributes for courses table.
   */
  static final String[] ATTRIBUTE_NAME_LIST = {
      "rubric_evaluation_pk",
      "rubric_pk",
      "code_reviewer_pk",
      "code_review_thread_pk",
      "explanation",
      "points",
      "value",
      "status",
      "modified"
  };

  /**
   * Fully-qualified attributes for courses table.
   */
  public static final String ATTRIBUTES = Queries.getAttributeList(
      TABLE_NAME, ATTRIBUTE_NAME_LIST);

  private @RubricEvaluation.PK int rubricEvaluationPK;
  private final @Rubric.PK int rubricPK;
  private final @CodeReviewer.PK int codeReviewerPK;
  private final @CodeReviewThread.PK int codeReviewThreadPK;
  private String explanation;
  private int points;
  private String value;
  private String status;
  private Timestamp modified;

  public RubricEvaluation(Connection conn,
                          Rubric rubric,
                          CodeReviewer reviewer,
                          CodeReviewThread thread) throws SQLException {
    this(conn,
         rubric.getRubricPK(),
         reviewer.getCodeReviewerPK(),
         thread.getCodeReviewThreadPK());
  }

  public RubricEvaluation(ResultSet rs, int col) throws SQLException {
    this.rubricEvaluationPK = asPK(rs.getInt(col++));
    this.rubricPK = Rubric.asPK(rs.getInt(col++));
    this.codeReviewerPK = CodeReviewer.asPK(rs.getInt(col++));
    this.codeReviewThreadPK = CodeReviewThread.asPK(rs.getInt(col++));
    this.explanation = rs.getString(col++);
    this.points = rs.getInt(col++);
    this.value = rs.getString(col++);
    this.status = rs.getString(col++);
    this.modified = rs.getTimestamp(col++);
  }

  public RubricEvaluation(Connection conn,
                          @Rubric.PK int rubricPK,
                          @CodeReviewer.PK int codeReviewerPK,
                          @CodeReviewThread.PK int codeReviewThreadPK) throws SQLException {
    this.rubricPK = rubricPK;
    this.codeReviewerPK = codeReviewerPK;
    this.codeReviewThreadPK = codeReviewThreadPK;
    this.status = "NEW";
    this.explanation="";
    this.value="";
    this.points = 0;
    this.modified = new Timestamp(System.currentTimeMillis());

    String insert = Queries.makeInsertStatementUsingSetSyntax(ATTRIBUTE_NAME_LIST, TABLE_NAME, true);
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
      Queries.setStatement(stmt, rubricPK, codeReviewerPK, codeReviewThreadPK, explanation, points, value, status, modified);
      stmt.executeUpdate();
      this.rubricEvaluationPK = asPK(Queries.getGeneratedPrimaryKey(stmt));
    } finally {
      Queries.closeStatement(stmt);
    }
  }

  public  @RubricEvaluation.PK  int getRubricEvaluationPK() {
    return rubricEvaluationPK;
  }

  public boolean isDraft() {
      return status.equals("NEW") || status.equals("DRAFT");
  }

  public String getValueAndExplanation(Rubric rubric) {
      if (rubric.getPresentation().equals("NUMERIC"))
          return explanation;
      return getValue() + " : "+ explanation;
    }

  public String getExplanation() {
    return explanation;
  }

  public void setExplanation(String explanation) {
    this.explanation = explanation;
  }

  public int getPoints() {
    return points;
  }

  public void setPoints(int points) {
    this.points = points;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public @Rubric.PK int getRubricPK() {
    return rubricPK;
  }

  public @CodeReviewer.PK int getCodeReviewerPK() {
    return codeReviewerPK;
  }

  public boolean isBy(CodeReviewer reviewer) {
      if (reviewer == null)
          return false;
      return codeReviewerPK == reviewer.getCodeReviewerPK();
  }

  public int getCodeReviewThreadPK() {
    return codeReviewThreadPK;
  }

  public String getStatus() {
    return status;
  }

  public Timestamp getModified() {
    return modified;
}
public void update(Connection conn) throws SQLException {
    String query = "UPDATE " + TABLE_NAME
        + " SET text=?, points=?, status=? WHERE rubric_evaluation_pk = ?";
    PreparedStatement stmt = conn.prepareStatement(query);
    try {
      Queries.setStatement(stmt, explanation, points, status, rubricEvaluationPK);
      stmt.execute();
    } finally {
      stmt.close();
    }
  }

  public static RubricEvaluation lookupByThread(Connection conn,
      @CodeReviewThread.PK int  codeReviewThreadPK) throws SQLException {
    String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
        + " WHERE code_review_thread_pk = ? ";

    PreparedStatement stmt = conn.prepareStatement(query);
    try {
      stmt.setInt(1, codeReviewThreadPK);
      ResultSet rs = stmt.executeQuery();
      if (rs.next())
        return new RubricEvaluation(rs, 1);
      return null;
    } finally {
      stmt.close();
    }
  }

  public static void update(Connection conn, @RubricEvaluation.PK int rubricEvaluationPK,
      String text, int points, String value, String status)
      throws SQLException {
    String query = "UPDATE " + TABLE_NAME
        + " SET explanation=?, points=?, value=?, status=? WHERE rubric_evaluation_pk = ?";
    PreparedStatement stmt = conn.prepareStatement(query);
    try {
      Queries.setStatement(stmt, text, points, value, status, rubricEvaluationPK);
      stmt.execute();
    } finally {
      stmt.close();
    }
  }

  public static void delete(Connection conn, @RubricEvaluation.PK int rubricEvaluationPK)
      throws SQLException {
    String query = "DELETE FROM " + TABLE_NAME
          + " WHERE rubric_evaluation_pk = ?";

      PreparedStatement stmt = conn.prepareStatement(query);
      try {
        Queries.setStatement(stmt, rubricEvaluationPK);
        stmt.execute();
      } finally {
        stmt.close();
      }
  }

  public static Collection<RubricEvaluation> lookupByReviewer(CodeReviewer reviewer,
      Connection conn) throws SQLException {
    Collection<RubricEvaluation> result =
        new ArrayList<RubricEvaluation>();
    String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
        + " WHERE code_reviewer_pk = ? ";

    PreparedStatement stmt = conn.prepareStatement(query);
    try {
      stmt.setInt(1, reviewer.getCodeReviewerPK());
      ResultSet rs = stmt.executeQuery();
      while (rs.next())
         result.add(new RubricEvaluation(rs, 1));
      return result;
    } finally {
      stmt.close();
    }
  }

     public static Collection<RubricEvaluation> lookupBySubmissionPK(
               @Submission.PK int submissionPK, Connection conn) throws SQLException {
          String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME  + ", " + CodeReviewThread.TABLE_NAME
                  + " WHERE code_review_thread.submission_pk = ? "
                  + " AND code_review_thread.code_review_thread_pk = " + TABLE_NAME+".code_review_thread_pk ";

          PreparedStatement stmt = conn.prepareStatement(query);
          try {
              stmt.setInt(1, submissionPK);
              ResultSet rs = stmt.executeQuery();
              LinkedList<RubricEvaluation> result = new LinkedList<RubricEvaluation>();

              while (rs.next())
                  result.add(new RubricEvaluation(rs, 1));
              return result;
          } finally {
              stmt.close();
          }
      }

  public static void publish(@RubricEvaluation.PK int id, Timestamp now, Connection conn)
      throws SQLException {
    String query = "UPDATE " + TABLE_NAME +
        " SET status = ?, modified=? WHERE rubric_evaluation_pk = ?";
    PreparedStatement stmt = conn.prepareStatement(query);
    try {
      Queries.setStatement(stmt, "LIVE", now, id);
      stmt.execute();
    } finally {
      stmt.close();
    }
  }

  public static int publishAll(CodeReviewer reviewer, Timestamp now, Connection conn)
      throws SQLException {
    String query = "UPDATE " + TABLE_NAME + ", " + CodeReviewThread.TABLE_NAME
        + " SET " + TABLE_NAME + ".status = ?, " +TABLE_NAME+".modified=? "
        + " WHERE " + TABLE_NAME + ".code_reviewer_pk=?"
        + " AND " + CodeReviewThread.TABLE_NAME + ".submission_pk = ?"
        + " AND " + TABLE_NAME + ".code_review_thread_pk = "
        + CodeReviewThread.TABLE_NAME + ".code_review_thread_pk"
        + " AND (" + TABLE_NAME + ".status = 'DRAFT' OR " + TABLE_NAME + ".status = 'NEW')";
    PreparedStatement stmt = conn.prepareStatement(query);

    try {
      Queries.setStatement(stmt, "LIVE", now, reviewer.getCodeReviewerPK(), reviewer.getSubmissionPK());
      return stmt.executeUpdate();
    } finally {
      stmt.close();
    }
  }
}

package edu.umd.cs.marmoset.modelClasses;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.annotation.meta.TypeQualifier;

public class Rubric {

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

  public static final String TABLE_NAME = "rubrics";

  public static LinkedHashMap<String, Integer> parseDataToMap(String data) {
    LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
    for (String pair : data.split(",")) {
      String[] keyValue = pair.split(":");
      if (keyValue.length == 2)
        map.put(keyValue[0].trim(), Integer.parseInt(keyValue[1].trim()));
    }
    return map;
  }

  public static String serializeMapToData(LinkedHashMap<String, Integer> map) {
    StringBuilder builder = new StringBuilder();
    Iterator<Entry<String, Integer>> iter = map.entrySet().iterator();
    while (iter.hasNext()) {
      Entry<String, Integer> entry = iter.next();
      builder.append(String.format("%s:%s", entry.getKey(), entry.getValue()));
      if (iter.hasNext()) {
        builder.append(", ");
      }
    }
    return builder.toString();
  }


  static final String[] ATTRIBUTE_NAME_LIST = {
    "rubric_pk",
    "code_review_assignment_pk",
    "name",
    "description",
    "presentation",
    "data"
  };

  /**
   * Fully-qualified attributes for courses table.
   */
   public static final String ATTRIBUTES = Queries.getAttributeList(TABLE_NAME,
      ATTRIBUTE_NAME_LIST);

  private @Rubric.PK int rubricPK; // primary key
  private @CodeReviewAssignment.PK int codeReviewAssignmentPK;
  private String name;
  private String description;
  private final String presentation;
  private String data;

  @Override
  public int hashCode() {
    return rubricPK;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Rubric))
      return false;
    Rubric that = (Rubric) obj;
    return this.rubricPK == that.rubricPK;

  }

	public Rubric(Connection conn,
	              CodeReviewAssignment assignment,
	              String name,
	              String description,
	              String presentation,
	              LinkedHashMap<String,Integer> data) throws SQLException {
		this(conn, assignment, name, description, presentation, serializeMapToData(data));
	}

    public Rubric(Connection conn, CodeReviewAssignment assignment, String name, String presentation, String data)
            throws SQLException {
        this(conn, assignment, name, presentation + ":" + data, presentation, data);

    }

  public Rubric(Connection conn, CodeReviewAssignment assignment , String name, String description,
      String presentation, String data) throws SQLException {

     String insert = Queries.makeInsertStatementUsingSetSyntax(ATTRIBUTE_NAME_LIST, TABLE_NAME, true);

    this.codeReviewAssignmentPK = assignment.getCodeReviewAssignmentPK();
    this.name = name;
    this.description = description;
    this.presentation = presentation;
    this.data = data;
      PreparedStatement stmt = null;
      try {
          stmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
          int col = 1;
          stmt.setInt(col++, codeReviewAssignmentPK);
          stmt.setString(col++, name);
          stmt.setString(col++, description);
          stmt.setString(col++, presentation);
          stmt.setString(col++, data);
          stmt.executeUpdate();

          this.rubricPK = Rubric.asPK(Queries.getGeneratedPrimaryKey(stmt));
      } finally {
          Queries.closeStatement(stmt);
      }

  }
  public @Rubric.PK int getRubricPK() {
    return rubricPK;
  }

  public int getCodeReviewAssignmentPK() {
    return codeReviewAssignmentPK;
  }
  public void setCodeReviewAssignmentPK(@CodeReviewAssignment.PK int codeReviewAssignmentPK) {
    this.codeReviewAssignmentPK = codeReviewAssignmentPK;
  }
  public String getName() {
    return name;
  }
  public String getDescription() {
    return description;
  }
  public String getPresentation() {
    return presentation;
  }
  public String getData() {
    return data;
  }
  public void setData(String data) {
    this.data = data;
  }
  public void setName(String name) {
    this.name = name;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public LinkedHashMap<String,Integer> getDataAsMap() {
      return parseDataToMap(data);
    }
  public int getMaxValue() {
      LinkedHashMap<String, Integer> map = parseDataToMap(data);
      if (map.isEmpty())
          return 0;
     return Collections.max(map.values());
    }


  public static Collection<Rubric> lookupByAssignment( CodeReviewAssignment assignment, 
          Connection conn) throws SQLException {
      return lookupByAssignment(assignment.getCodeReviewAssignmentPK(), conn);
  }
     
  public static Collection<Rubric> lookupByAssignment( @CodeReviewAssignment.PK int codeReviewAssignmentPK,
      Connection conn) throws SQLException {
    String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
        + " WHERE code_review_assignment_pk = ? "
        + "  ORDER BY rubric_pk ASC ";

    PreparedStatement stmt = conn.prepareStatement(query);
    try {
      stmt.setInt(1, codeReviewAssignmentPK);
      ResultSet rs = stmt.executeQuery();
      LinkedList<Rubric> result = new LinkedList<Rubric>();

      while (rs.next())
        result.add( new Rubric(rs, 1));
      return result;
    } finally {
      stmt.close();
    }
  }
  public static int countByAssignment( @CodeReviewAssignment.PK int codeReviewAssignmentPK,
	      Connection conn) throws SQLException {
	    String query = "SELECT COUNT(*) FROM " + TABLE_NAME
	        + " WHERE code_review_assignment_pk = ? ";

	    PreparedStatement stmt = conn.prepareStatement(query);
	    try {
	      stmt.setInt(1, codeReviewAssignmentPK);
	      ResultSet rs = stmt.executeQuery();
          if (rs.next())
          return rs.getInt(1);
          return 0;
	    } finally {
	      stmt.close();
	    }
	  }

  public static Collection<Rubric> lookupBySubmission(@Submission.PK int submissionPK, Connection conn)
  throws SQLException {
  	String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
  			+ " JOIN " + RubricEvaluation.TABLE_NAME + " USING (rubric_pk) "
  			+ " JOIN " + CodeReviewer.TABLE_NAME + " USING (code_reviewer_pk) "
  			+ " WHERE submission_pk = ?";

  	PreparedStatement stmt = conn.prepareStatement(query);
  	try {
  		stmt.setInt(1, submissionPK);
  		ResultSet rs = stmt.executeQuery();
  		LinkedList<Rubric> result = new LinkedList<Rubric>();
  		while (rs.next())
  			result.add(new Rubric(rs, 1));
  		return result;
  	} finally {
  		stmt.close();
  	}
  }

  public static Rubric lookupByPK(@Rubric.PK int rubricPK,
      Connection conn) throws SQLException {
    String query = "SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
        + " WHERE rubric_pk = ? ";

    PreparedStatement stmt = conn.prepareStatement(query);
    try {
      stmt.setInt(1, rubricPK);
      ResultSet rs = stmt.executeQuery();

      while (rs.next())
        return new Rubric(rs, 1);
      return null;
    } finally {
      stmt.close();
    }
  }

  public  Rubric(ResultSet resultSet, int startingFrom)
  throws SQLException
  {
    this.rubricPK = Rubric.asPK(resultSet.getInt(startingFrom++));
    this.codeReviewAssignmentPK = CodeReviewAssignment.asPK( resultSet.getInt(startingFrom++));
    this.name = resultSet.getString(startingFrom++);
    this.description = resultSet.getString(startingFrom++);
    this.presentation = resultSet.getString(startingFrom++);
    this.data = resultSet.getString(startingFrom++);
  }
  
  public void update(Connection conn) throws SQLException {

    String update = Queries.makeUpdateStatementWithWhereClause(ATTRIBUTE_NAME_LIST, TABLE_NAME, "WHERE rubric_pk = " + this.rubricPK);
     PreparedStatement stmt = null;
     try {
         stmt = conn.prepareStatement(update);
         int col = 1;
         stmt.setInt(col++, codeReviewAssignmentPK);
         stmt.setString(col++, name);
         stmt.setString(col++, description);
         stmt.setString(col++, presentation);
         stmt.setString(col++, data);
         stmt.executeUpdate();
     } finally {
         Queries.closeStatement(stmt);
     }
  }
}

package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import edu.umd.cs.marmoset.utilities.TextUtilities;

public class ServerError {

	public static final String TABLE_NAME = "errors";

	static final String[] ATTRIBUTE_NAME_LIST = { "error_pk", "when",
			"user_pk", "student_pk", "course_pk", "project_pk", "submission_pk", "code", "message", "type", "servlet", "uri",
			"query_string","remote_host","referer",
			"throwable_as_string", "throwable" };

	 public static final String ATTRIBUTES = Queries.getAttributeList(
	            TABLE_NAME, ATTRIBUTE_NAME_LIST);


    public ServerError(int errorPK, Timestamp when, String message) {
        this.errorPK = errorPK;
        this.when = when;
        this.message = message;
    }

    final int errorPK;
    final Timestamp when;
	final String message;
	
	
	
	public int getErrorPK() {
        return errorPK;
    }

    public Timestamp getWhen() {
        return when;
    }

    public String getMessage() {
        return message;
    }

    public static List<ServerError> recentErrors(int limit, Timestamp maxAge, Connection conn) throws SQLException {
        String query = "SELECT error_pk, `when`, message FROM " + TABLE_NAME 
                + " WHERE `when` >= ? "
                + " ORDER BY  `errors`.`when` DESC "
                + " LIMIT ?";
        List<ServerError> result = new ArrayList<ServerError>();
        PreparedStatement stmt = Queries.setStatement(conn,  query, maxAge, limit);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            ServerError e = new ServerError(rs.getInt(1), rs.getTimestamp(2), rs.getString(3));
            result.add(e);
        }
        return result;
    }


    public static int insert(Connection conn,
            String code,
            String message, String type, String servlet, String uri, String queryString, 
            String remoteHost, String referer, Throwable t) throws SQLException {
        return ServerError.insert(conn, null, null, null, null, null, 
                code, message, type, servlet, uri, queryString, remoteHost, referer, t);
    }


	public static int insert(Connection conn, @Student.PK Integer userPK, 
			@Student.PK Integer studentPK,  Integer coursePK,
			@Project.PK Integer projectPK, @Submission.PK Integer submissionPK, 
			String code,
			String message, String type, String servlet, String uri, String queryString, 
			String remoteHost, String referer, Throwable t)
			throws SQLException {
		if (conn == null)
			return -1;
		Timestamp now = new Timestamp(System.currentTimeMillis());
		String throwableAsString = TextUtilities.dumpException(t);
		if (message == null && t != null)
		    message = t.getClass().getSimpleName();

		String query = Queries.makeInsertStatement(ATTRIBUTE_NAME_LIST,
				TABLE_NAME);
		PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		try {
			Queries.setStatement(stmt, now, userPK, studentPK, coursePK, projectPK, submissionPK, code, message, type,
					servlet, uri, queryString, remoteHost, referer, throwableAsString, Queries.serialize(conn, t));
			stmt.executeUpdate();
			return Queries.getGeneratedPrimaryKey(stmt);
		} finally {
			Queries.closeStatement(stmt);
		}
	}

}

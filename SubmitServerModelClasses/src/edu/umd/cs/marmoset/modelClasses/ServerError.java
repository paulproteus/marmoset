package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umd.cs.marmoset.utilities.TextUtilities;

public class ServerError {

	public static final String TABLE_NAME = "errors";

	static final String[] ATTRIBUTE_NAME_LIST = { "error_pk", "when",
			"user_pk", "student_pk", "course_pk", "project_pk", "submission_pk", "code", "message", "type", "servlet", "uri",
			"query_string","remote_host","referer",
			"throwable_as_string", "throwable", "kind" };
	
	public enum Kind {
	    SLOW(600), PAGE_NOT_FOUND(500), BAD_AUTHENTICATION(500), NOT_REGISTERED(500),
	    BAD_PARAMETERS(500),SUBMIT(500),
	    EXCEPTION(900), UNKNOWN(600);
	    Kind(int level) {
	        this.level = level;
	    }
	    final int level;
	    public String toString() {
	        return name().toLowerCase().replace('_', ' ');
        }

        public static Kind safeValueOf(String s) {
            if (s == null)
                return UNKNOWN;
            try {
                return valueOf(s);
            } catch (IllegalArgumentException e) {
                return UNKNOWN;
            }
        }
    }

	 public static final String ATTRIBUTES = Queries.getAttributeList(
	            TABLE_NAME, ATTRIBUTE_NAME_LIST);


	 private ServerError(int errorPK, Timestamp when, Kind kind, String message) {
	        this.errorPK = errorPK;
	        this.when = when;
	        this.kind = kind;
	        this.message = message;
	    }
    private ServerError(int errorPK, Timestamp when, String kind, String message) {
        this(errorPK, when, Kind.safeValueOf(kind), message);
    }

    final int errorPK;
    final Timestamp when;
	final String message;
	final Kind kind;
	
	public int getErrorPK() {
        return errorPK;
    }

    public Timestamp getWhen() {
        return when;
    }

    public Kind getKind() {
        return kind;
    }
    public String getMessage() {
        return message;
    }

    
    public static ServerError getError(int errorPK, Connection conn) throws SQLException {
        String query = "SELECT error_pk, `when`, kind, message FROM " + TABLE_NAME 
                + " WHERE `error_pk` = ? "
                + " ORDER BY  `errors`.`when` DESC ";
        PreparedStatement stmt = Queries.setStatement(conn,  query, errorPK);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            return new ServerError(rs.getInt(1), rs.getTimestamp(2), rs.getString(3), rs.getString(4));
        }
        return null;
    }
    public static Map<Object,Object> getAllFields(Connection conn, int errorPK) 
            throws SQLException {
        
        HashMap<Object,Object> result = new HashMap<Object,Object>();
        String query = "SELECT " +ATTRIBUTES+
                " FROM errors  where error_pk = ?";
        PreparedStatement stmt = Queries.setStatement(conn,  query, errorPK);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            int col = 1;
            for(String c : ATTRIBUTE_NAME_LIST) {
                Object value;
                if (c.endsWith("_pk"))
                    value = rs.getInt(col);
                else if (c.equals("throwable"))
                    value = rs.getBlob(col);
                else if (c.equals("when"))
                    value = rs.getDate(col);
                else value = rs.getString(col);
                col++;
                result.put(c, value);
            }
        }
        
        return result;
        
    }

     
    public static List<ServerError> recentErrors(int limit, Timestamp maxAge, Connection conn) throws SQLException {
        String query = "SELECT error_pk, `when`, kind, message FROM " + TABLE_NAME 
                + " WHERE `when` >= ? "
                + " ORDER BY  `errors`.`when` DESC "
                + " LIMIT ?";
        List<ServerError> result = new ArrayList<ServerError>();
        PreparedStatement stmt = Queries.setStatement(conn,  query, maxAge, limit);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            ServerError e = new ServerError(rs.getInt(1), rs.getTimestamp(2), rs.getString(3), rs.getString(4));
            result.add(e);
        }
        return result;
    }
    public static List<ServerError> recentErrorsExcludingKind(int limit, Kind kind, Timestamp maxAge, Connection conn) throws SQLException {
        String query = "SELECT error_pk, `when`,kind, message FROM " + TABLE_NAME 
                + " WHERE `when` >= ?  and (`kind` != ?  OR `kind` is NULL) "
                + " ORDER BY  `errors`.`when` DESC "
                + " LIMIT ?";
        List<ServerError> result = new ArrayList<ServerError>();
        PreparedStatement stmt = Queries.setStatement(conn,  query, maxAge, kind, limit);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            ServerError e = new ServerError(rs.getInt(1), rs.getTimestamp(2), rs.getString(3), rs.getString(4));
            result.add(e);
        }
        return result;
    }
    public static List<ServerError> recentErrors(int limit, Kind kind, Timestamp maxAge, Connection conn) throws SQLException {
        String query = "SELECT error_pk, `when`, message FROM " + TABLE_NAME 
                + " WHERE `when` >= ?  and `kind` = ? "
                + " ORDER BY  `errors`.`when` DESC "
                + " LIMIT ?";
        List<ServerError> result = new ArrayList<ServerError>();
        PreparedStatement stmt = Queries.setStatement(conn,  query, maxAge, kind, limit);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            ServerError e = new ServerError(rs.getInt(1), rs.getTimestamp(2), kind, rs.getString(3));
            result.add(e);
        }
        return result;
    }

	public static int insert(Connection conn, Kind kind, 
			@Student.PK Integer userPK,  @Student.PK Integer studentPK,
			Integer coursePK, @Project.PK Integer projectPK, 
			@Submission.PK Integer submissionPK,
			String code, String message, String type, String servlet, String uri, 
			String queryString, String remoteHost, String referer, Throwable t)
			throws SQLException {
		if (conn == null)
			return -1;
		if (kind == Kind.UNKNOWN && code.equals("404"))
		    kind = Kind.PAGE_NOT_FOUND;
		Timestamp now = new Timestamp(System.currentTimeMillis());
		String throwableAsString = TextUtilities.dumpException(t);
		if (message == null && t != null)
		    message = t.getClass().getSimpleName();

		String query = Queries.makeInsertStatement(ATTRIBUTE_NAME_LIST,
				TABLE_NAME);
		PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		try {
			Queries.setStatement(stmt, now, userPK, studentPK, coursePK, projectPK, submissionPK, code, message, type,
					servlet, uri, queryString, remoteHost, referer, throwableAsString, Queries.serialize(conn, t), kind);
			stmt.executeUpdate();
			return Queries.getGeneratedPrimaryKey(stmt);
		} finally {
			Queries.closeStatement(stmt);
		}
	}

}

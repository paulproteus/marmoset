package edu.umd.cs.submitServer.filters;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import edu.umd.cs.submitServer.GradeServerDatabaseProperties;

public abstract class GradeServerInterfaceFilter extends SubmitServerFilter {

	private GradeServerDatabaseProperties gradeServerDatabaseProperties;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
		ServletContext servletContext = filterConfig.getServletContext();
		
			gradeServerDatabaseProperties = new GradeServerDatabaseProperties(
					servletContext);


	}

	/**
	 * Gets a connection to the database.
	 *
	 * @return a connection to the database.
	 * @throws SQLException
	 */
	protected Connection getGradesConnection() throws SQLException {
		return gradeServerDatabaseProperties.getConnection();
	}

	protected void releaseGradesConnection(Connection conn) {
		try {
			gradeServerDatabaseProperties.releaseConnection(conn);
		} catch (SQLException e) {
			getSubmitServerFilterLog().warn("Unable to close connection", e);
		}
	}


}

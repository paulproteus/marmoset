package edu.umd.cs.submitServer.filters;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;

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

        String user = servletContext.getInitParameter("grades.user");
        if (user != null)
            gradeServerDatabaseProperties = new GradeServerDatabaseProperties(servletContext);
        else {
            for( Enumeration<String> e = servletContext.getInitParameterNames();
                    e.hasMoreElements();) {
                String s = e.nextElement();
                System.out.println(s);
            }
        }
    }

    protected boolean supportsGradeServer() {
        return gradeServerDatabaseProperties != null;
    }

    /**
     * Gets a connection to the database.
     * 
     * @return a connection to the database.
     * @throws SQLException
     */
    protected Connection getGradesConnection() throws SQLException {
        if (!supportsGradeServer())
            throw new UnsupportedOperationException("Not configured for grade server");
        return gradeServerDatabaseProperties.getConnection();
    }

    protected void releaseGradesConnection(Connection conn) {
        if (!supportsGradeServer())
            throw new UnsupportedOperationException("Not configured for grade server");

        try {
            gradeServerDatabaseProperties.releaseConnection(conn);
        } catch (SQLException e) {
            getSubmitServerFilterLog().warn("Unable to close connection", e);
        }
    }

}

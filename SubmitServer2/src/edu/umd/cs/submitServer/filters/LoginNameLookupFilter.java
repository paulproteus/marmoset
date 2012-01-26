package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import edu.umd.cs.marmoset.modelClasses.Student;


/**
 * Looks up a student from the "login_name" parameter and stores the result in the request.
 * 
 * @author rwsims
 *
 */
public class LoginNameLookupFilter extends SubmitServerFilter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	    throws IOException, ServletException {
		String loginName = request.getParameter("login_name");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(loginName), "No login name specified.");
		
		Connection conn = null;
		try {
			conn = getConnection();
			Student student = Student.lookupByLoginName(loginName, conn);
			if (student == null) {
				throw new ServletException("Can't find student");
			}
			if (student.getCanImportCourses()) {
				throw new ServletException("Can't edit instructor accounts");
			}
			request.setAttribute("editStudent", student);
		} catch (SQLException e) {
	    throw new ServletException(e);
    } finally {
			releaseConnection(conn);
		}
		chain.doFilter(request, response);
	}
}

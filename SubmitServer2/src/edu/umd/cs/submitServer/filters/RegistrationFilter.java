package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.dao.RegistrationDao;
import edu.umd.cs.submitServer.dao.impl.MySqlRegistrationDaoImpl;

/**
 * Filter to get a list of all courses available for registration and any pending registration requests.
 * 
 * @author rwsims
 *
 */
public class RegistrationFilter extends SubmitServerFilter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	    throws IOException, ServletException {
		Student student = (Student) ((HttpServletRequest) request).getSession()
		                                                          .getAttribute(SubmitServerConstants.STUDENT);
		RegistrationDao dao = new MySqlRegistrationDaoImpl(student, submitServerDatabaseProperties);
		request.setAttribute("pendingRequests", dao.getPendingRequests());
		
		List<Course> registeredCourses = (List<Course>) request.getAttribute(SubmitServerConstants.COURSE_LIST);
		Connection conn = null;
		try {
			conn = getConnection();
			List<Course> openCourses = Course.lookupAll(conn);
			openCourses.removeAll(registeredCourses);
			request.setAttribute(SubmitServerConstants.OPEN_COURSES, openCourses);
			chain.doFilter(request, response);
		} catch (SQLException e) {
			throw new ServletException(e);
    } finally {
			releaseConnection(conn);
		}
	}
}

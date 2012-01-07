package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.submitServer.SubmitServerConstants;

/**
 * Filter to get a list of all courses available for registration. Must be applied <b>after</b>
 * {@code ExtractParametersFilter}.
 * 
 * @author rwsims
 *
 */
public class OpenCourseFilter extends SubmitServerFilter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	    throws IOException, ServletException {
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

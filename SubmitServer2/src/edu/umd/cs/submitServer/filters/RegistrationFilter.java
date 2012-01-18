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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        String gradesServer = request.getServletContext().getInitParameter("grades.server");
        if (gradesServer == null) {
            Student user = (Student) request.getAttribute(SubmitServerConstants.USER);
            RegistrationDao dao = new MySqlRegistrationDaoImpl(user, submitServerDatabaseProperties);
            request.setAttribute("pendingRequests", dao.getPendingRequests());
            request.setAttribute(SubmitServerConstants.OPEN_COURSES, dao.getOpenCourses());
        }
        chain.doFilter(request, response);
    }
}

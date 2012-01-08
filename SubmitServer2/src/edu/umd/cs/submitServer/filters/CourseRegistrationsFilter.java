package edu.umd.cs.submitServer.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.base.Preconditions;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.dao.RegistrationDao;
import edu.umd.cs.submitServer.dao.impl.MySqlRegistrationDaoImpl;

public class CourseRegistrationsFilter extends SubmitServerFilter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	    throws IOException, ServletException {
		
		Student user = (Student) request.getAttribute(SubmitServerConstants.USER);
		Course course = (Course) Preconditions.checkNotNull(request.getAttribute(SubmitServerConstants.COURSE));
		
		RegistrationDao dao = new MySqlRegistrationDaoImpl(user, submitServerDatabaseProperties);
		request.setAttribute("pendingRegistrations", dao.getPendingRegistrations(course.getCoursePK()));
		
		chain.doFilter(request, response);
	}
}

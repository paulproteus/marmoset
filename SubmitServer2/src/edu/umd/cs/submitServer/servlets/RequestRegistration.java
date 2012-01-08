package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.UserSession;
import edu.umd.cs.submitServer.dao.RegistrationDao;
import edu.umd.cs.submitServer.dao.impl.MySqlRegistrationDaoImpl;

public class RequestRegistration extends SubmitServerServlet {
	private static Pattern checkboxNamePattern = Pattern.compile("course-pk-(\\d)");
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
		HttpSession session = ((HttpServletRequest) req).getSession();
		UserSession userSession = (UserSession) session.getAttribute(SubmitServerConstants.USER_SESSION);
		
		Connection conn = null;
		try {
			conn = getConnection();
			Student student = Student.getByStudentPK(userSession.getStudentPK(), conn);
			RegistrationDao dao = new MySqlRegistrationDaoImpl(student, getDatabaseProps());
			for (String name : req.getParameterMap().keySet()) {
				Matcher matcher = checkboxNamePattern.matcher(name);
				if (!matcher.matches()) {
					continue;
				}
				int coursePK = Integer.parseInt(matcher.group(1));
				dao.requestRegistration(coursePK);
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
		
		resp.sendRedirect(req.getContextPath() + "/view/index.jsp");
	}
}

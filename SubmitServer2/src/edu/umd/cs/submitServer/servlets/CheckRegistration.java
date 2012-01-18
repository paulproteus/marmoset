package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.umd.cs.marmoset.modelClasses.Student;

/**
 * Class to check unique registration fields, such as usernames and email addresses.
 * 
 * @author rwsims
 *
 */
public class CheckRegistration extends SubmitServerServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
		String email = req.getParameter("email");
		String login = req.getParameter("login");
		
		Connection conn = null;
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		Logger logger = getSubmitServerServletLog();
		try {
	    conn = getConnection();
	    if (!Strings.isNullOrEmpty(login)) {
	    	logger.info("Checking unique login name " + login);
	    	Student student = Student.lookupByLoginName(login, conn);
	    	out.println(student != null ? "false" : "true");
	    	return;
	    }
			if (!Strings.isNullOrEmpty(email)) {
				logger.info("Checking unique email " + email);
				Student student = Student.lookupByEmailAddress(email, conn);
				out.println(student != null ? "false" : "true");
				return;
			}
    } catch (SQLException e) {
    	throw new ServletException(e);
    } finally {
    	releaseConnection(conn);
    }
	}
}

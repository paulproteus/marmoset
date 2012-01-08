package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.utilities.Charsets;

/**
 * Complete registration of a new OpenID identifier. This servlet handles the form post from
 * {@code /openid/register.jsp}.
 * 
 * @author rwsims
 * 
 */
public class RegisterOpenId extends SubmitServerServlet {
	private static final Logger logger = Logger.getLogger(RegisterOpenId.class);
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
		String uid = req.getParameter("uid");
		logger.info("Registering uid " + uid);
		String firstname = req.getParameter("firstname");
		String lastname = req.getParameter("lastname");
		String loginName = req.getParameter("login");
		String email = req.getParameter("email");
		
		Connection conn = null;
		try {
			conn = getConnection();
			boolean emptyDatabase = !Student.existAny(conn);
			
			// Create normal user account.
			Student student = Student.insertOrUpdateByUID(uid, firstname, lastname, loginName, email, conn);
			
			if (emptyDatabase) {
				// This is the first user to register, so need to add admin account for them.
				Student admin = new Student();
				admin.setLastname(student.getLastname());
				admin.setFirstname(student.getFirstname());
				admin.setCampusUID(student.getCampusUID());
				admin.setLoginName(student.getLoginName()+ "-admin");
				admin.setEmail(student.getEmail());
				admin.setSuperUser(true);
				admin = admin.insertOrUpdateCheckingLoginNameAndCampusUID(conn);
				PerformLogin.setUserSession(req.getSession(), admin, conn);
			} else {
				PerformLogin.setUserSession(req.getSession(), student, conn);
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}
		  // check to see if user tried to view a page before logging in
        String target = req.getParameter("target");

        if (target != null && !target.equals("")) {
            target = Charsets.decodeURL(target);
            resp.sendRedirect(target);
            return;
        }

		
	}
}

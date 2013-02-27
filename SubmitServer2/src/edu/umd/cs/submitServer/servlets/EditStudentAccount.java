package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import edu.umd.cs.marmoset.modelClasses.Student;

/**
 * Edits or updates a student account.
 * 
 * @author rwsims
 *
 */
public class EditStudentAccount extends SubmitServerServlet {
	
	private enum Action {
		UPDATE,
		DELETE;
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
		int studentPK = Integer.parseInt(req.getParameter("studentPK"));
		Action action = Action.valueOf(req.getParameter("action"));
		Preconditions.checkNotNull(action, "Invalid action parameter.");
		Connection conn = null;
		try {
			conn = getConnection();
			Student student = Student.getByStudentPK(Student.asPK(studentPK), conn);
			Preconditions.checkNotNull(student, "Could not find student by PK");
			
			switch (action) {
				case UPDATE:
        String firstName = require("firstname", req);
        student.setFirstname(firstName);
        String lastName = require("lastname", req);
        student.setLastname(lastName);
					student.setLoginName(require("login", req));
					student.setEmail(require("email", req));
					student.update(conn);
					break;
					
				case DELETE:
					throw new UnsupportedOperationException();
			}
			
		} catch (SQLException e) {
			throw new ServletException(e);
    } finally {
			releaseConnection(conn);
		}
		resp.sendRedirect(req.getContextPath() + "/view/admin/index.jsp");
	}
	
	private static String require(String param, HttpServletRequest req) {
		String value = req.getParameter(param);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(value), "Missing required parameter %s", param);
		return value;
	}
}

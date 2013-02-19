package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.ServerError;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.UserSession;

public class LogErrorFilter extends SubmitServerFilter {

	public static String nullSafeToString(Object x) {
		if (x == null)
			return null;
		return x.toString();
	}

	public static String getOptionalParameterAsString(ServletRequest req,
			String name) {
		return nullSafeToString(req.getAttribute(name));
	}
	
	public static boolean ignore(String code, String message) {
	    if (!code.equals("404")) 
	        return false;
	    
	    
	    if (message.startsWith("/apple-touch-icon"))
	        return true;
	    if (message.startsWith("/images/handycons/"))
	        return true;
	    if (message.toLowerCase().contains("translators.html"))
	        return true;
	    
	    return false;
	}

	public void doFilter(ServletRequest rq, ServletResponse rs,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) rq;
		HttpServletResponse resp = (HttpServletResponse) rs;
		String userAgent = req.getHeader("User-Agent");

		HttpSession session = req.getSession();
		Connection conn = null;

			UserSession userSession = (UserSession) session
					.getAttribute(USER_SESSION);

			@Student.PK Integer userPK = userSession == null ? null : userSession
					.getStudentPK();
			Course course = (Course) req.getAttribute(COURSE);
			Integer coursePK = null;
			if (course != null)
			    coursePK = course.getCoursePK();
			else if (userSession != null)
			    coursePK = userSession.getOnlyCoursePK();
			Student student = (Student) req.getAttribute(STUDENT);
			Project project = (Project) req.getAttribute(PROJECT);
			Submission submission = (Submission) req.getAttribute(SUBMISSION);
			String referer = req.getHeader("referer");
			String remoteHost =  SubmitServerFilter.getRemoteHost(req);
			 
			
			String code = getOptionalParameterAsString(req,
					"javax.servlet.error.status_code");
			String message = getOptionalParameterAsString(req,
					"javax.servlet.error.message");
			String type = getOptionalParameterAsString(req,
					"javax.servlet.error.exception_type");
			String servletName = getOptionalParameterAsString(req,
					"javax.servlet.error.servlet_name");
			String requestURI = getOptionalParameterAsString(req,
					"javax.servlet.error.request_uri");
			Throwable throwable = (Throwable) req
					.getAttribute("javax.servlet.error.exception");
			if (requestURI == null)
				requestURI = req.getRequestURI();

			String queryString = getOptionalParameterAsString(req,
					"javax.servlet.forward.query_string");
			if (queryString == null)
				queryString = req.getQueryString();
			
			if (!ignore(code, message) )  try {
			conn = getConnection();

			int errorPK = ServerError.insert(conn,throwable != null ? ServerError.Kind.EXCEPTION 
			        : ServerError.Kind.UNKNOWN,
					userPK, 
							student == null ? null : student.getStudentPK(),
									coursePK,
											project == null ? null : project.getProjectPK(),
							submission == null ? null : submission.getSubmissionPK(), code, message,
					type, servletName, requestURI, queryString, remoteHost, referer, userAgent, throwable);
			req.setAttribute("errorPK", errorPK);
		} catch (Exception t) {
			getSubmitServerFilterLog().warn(t);
		} finally {
			releaseConnection(conn);
		}

		try {
		
		chain.doFilter(req, resp);
		} catch (RuntimeException e) {
			try {
				getSubmitServerFilterLog().warn(e);
				conn = getConnection();
				ServerError.insert(conn, ServerError.Kind.EXCEPTION, 
						userPK, 
								student == null ? null : student.getStudentPK(),
										course == null ? null : course.getCoursePK(),
												project == null ? null : project.getProjectPK(),
								submission == null ? null : submission.getSubmissionPK(), code, message,
								type, "on chain", requestURI, queryString, remoteHost, referer, userAgent, e);
			} catch (SQLException e2) {
				getSubmitServerFilterLog().warn(e2);
			} finally {
				releaseConnection(conn);
			}
		}

	}
}
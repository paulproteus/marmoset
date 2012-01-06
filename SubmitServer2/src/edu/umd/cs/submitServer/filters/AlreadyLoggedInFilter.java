package edu.umd.cs.submitServer.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.submitServer.UserSession;

public class AlreadyLoggedInFilter extends SubmitServerFilter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		HttpSession session = request.getSession(false);
		if (session != null) {
			UserSession userSession = (UserSession) session
					.getAttribute(USER_SESSION);
			if (userSession != null) {
				response.sendRedirect(request.getContextPath()
						+ "/view/index.jsp");
				return;
			}

		}
		chain.doFilter(request, response);
	}

}

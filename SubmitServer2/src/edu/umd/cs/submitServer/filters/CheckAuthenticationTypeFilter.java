package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.WebConfigProperties;

/**
 * Filter that intercepts requests for authentication and checks that they're in keeping with the
 * authentication type set in {@code authentication.type}.
 * 
 * Note that this seems to mean that a request to a nonexistent url under /authenticate/ wont result
 * in a 404, it'll be a servlet exception.
 * 
 * @author rwsims
 * 
 */
public class CheckAuthenticationTypeFilter implements Filter {
	private static final WebConfigProperties webProperties = WebConfigProperties.get();
	private Pattern safeUrl;
	private Pattern guardedUrl;
	private String authType;

	@Override
  public void init(FilterConfig filterConfig) throws ServletException {
		ServletContext ctx = filterConfig.getServletContext();
		authType = webProperties.getRequiredProperty(SubmitServerConstants.AUTHENTICATION_TYPE, "openid");
		safeUrl = Pattern.compile("^/authenticate/[a-zA-Z0-9]+(.jsp)?");
	  guardedUrl = Pattern.compile("^/authenticate/([a-zA-Z0-9]+)/.*");
  }
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
	    throws IOException, ServletException {
		String path = ((HttpServletRequest) req).getServletPath();
		if (safeUrl.matcher(path).matches()) {
			chain.doFilter(req, resp);
			return;
		}
		Matcher matcher = guardedUrl.matcher(path);
		if (!matcher.matches() || !matcher.group(1).equals(authType)) {
			throw new ServletException("Invalid authorization attempt");
		}
		chain.doFilter(req, resp);
	}
	
  public void destroy() {}
}

package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.submitServer.SubmitServerConstants;

/**
 * Servlet for verifying OpenID responses from the OP.
 * 
 * @author Ryan W Sims (rwsims@umd.edu)
 * @see InitiateOpenId
 * 
 */
public class VerifyOpenId extends SubmitServerServlet {
	public static String url(String contextPath) {
		return contextPath + "/authenticate/openid/verify";
	}

	private final transient ConsumerManager consumerManager = new ConsumerManager();

	@Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
		boolean skipAuthentication = "true".equals(req.getServletContext().getInitParameter("authentication.skip"));
		String uid = null;
		String loginName = null;
		if (skipAuthentication) {
			uid = req.getParameter("uid");
			loginName = req.getParameter("login_name");
		}
	    if (uid == null) {
	        uid = verifyIdentity(req);
	    }
		Connection conn = null;
		try {
	    conn = getConnection();
	    Student student;
	    if (uid != null) 
	        student = Student.lookupByCampusUID(uid, conn);
	    else 
	        student = Student.lookupByLoginName(loginName, conn);
	    
	    String targetUrl = req.getParameter("marmoset.target");
	    if (Strings.isNullOrEmpty(targetUrl)) {
	    	targetUrl = Util.urlEncode(req.getContextPath() + "/view/index.jsp");
	    }
	    if (student == null) {
	    	// Redirect to account registration page.
	  		resp.sendRedirect(String.format("%s/%s?uid=%s&target=%s",
	  		                                req.getContextPath(),
	  		                                "authenticate/openid/register.jsp",
	  		                                uid,
	  		                                targetUrl));
	  		return;
	    }
	    PerformLogin.setUserSession(req.getSession(), student, conn);
	    resp.sendRedirect(Util.urlDecode(targetUrl));
    } catch (SQLException e) {
	    throw new ServletException(e);
    } finally {
    	releaseConnection(conn);
    }
  }

	private String verifyIdentity(HttpServletRequest req) throws ServletException {
	  ParameterList openIdResp = new ParameterList(req.getParameterMap());
    DiscoveryInformation discovered = (DiscoveryInformation) req.getSession().getAttribute(SubmitServerConstants.OPENID_DISCOVERED);
    
    StringBuffer receivingUrl = req.getRequestURL();
    String queryString = req.getQueryString();
    if (!Strings.isNullOrEmpty(queryString)) {
      receivingUrl.append("?").append(queryString);
    }
    VerificationResult verification;
    try {
      verification = consumerManager.verify(receivingUrl.toString(), openIdResp, discovered);
    } catch (MessageException e) {
      throw new ServletException(e);
    } catch (DiscoveryException e) {
      throw new ServletException(e);
    } catch (AssociationException e) {
      throw new ServletException(e);
    }

		Identifier verified = Preconditions.checkNotNull(verification.getVerifiedId(),
		                                                 "OpenID authentication failed");
		getSubmitServerServletLog().info("Verified OpenID " + verified.getIdentifier());
		String uid = hashOpenId(verified.getIdentifier());
	  return uid;
  }

	/**
	 * SHA-1 hash an OpenID identifier, since OpenID puts no restriction on the length of an
	 * identifier.
	 */
	private static String hashOpenId(String identifier) throws ServletException {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(identifier.getBytes());
			return String.format("%040x", new BigInteger(1, digest.digest()));
		} catch (NoSuchAlgorithmException e) {
			throw new ServletException("Couldn't hash OpenID identifier");
		}
	}
}

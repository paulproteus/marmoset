package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.MessageException;
import org.openid4java.message.ax.FetchRequest;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.UrlBuilder;

/**
 * Servlet for initiating OpenID login request. It uses the {@code openid_identifier} form parameter
 * from {@code /openid/login.jsp}.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 * @see VerifyOpenId
 */
public class InitiateOpenId extends SubmitServerServlet {
  private final transient ConsumerManager consumerManager = new ConsumerManager();
  
  private String getOpenidRealm(HttpServletRequest req) {
  	return String.format("%s://%s:%d/",
  	                     req.getScheme(),
  	                     req.getServerName(),
  	                     req.getServerPort());
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String identifier = req.getParameter("openid_identifier");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(identifier),
                                "Invalid OpenID identifier received: %s", identifier);
    List<?> discoveries;
    try {
      discoveries = consumerManager.discover(identifier);
    } catch (DiscoveryException e) {
      throw new ServletException(e);
    }
    DiscoveryInformation discovered = consumerManager.associate(discoveries);
    req.getSession().setAttribute(SubmitServerConstants.OPENID_DISCOVERED, discovered);
    getSubmitServerServletLog().info("request uri " + req.getRequestURI());
    
    try {
    	UrlBuilder returnUrl = new UrlBuilder(req);
    	returnUrl.addPathElement("authenticate/openid/verify");
    	String targetPath = req.getParameter("target");
    	if (!Strings.isNullOrEmpty(targetPath)) {
    		returnUrl.setParameter("marmoset.target", targetPath);
    	}
      getSubmitServerServletLog().info("Using returnURL  " + returnUrl);
         
      AuthRequest authReq = consumerManager.authenticate(discovered, returnUrl.toString());
      String realm = getOpenidRealm(req);
      getSubmitServerServletLog().info("Using openid realm " + realm);
      authReq.setRealm(realm);
      
			FetchRequest fetch = FetchRequest.createFetchRequest();
			fetch.addAttribute("firstname", "http://schema.openid.net/namePerson/first", true);
			fetch.addAttribute("lastname", "http://schema.openid.net/namePerson/last", true);
			fetch.addAttribute("email", "http://schema.openid.net/contact/email", true);
      authReq.addExtension(fetch);
      
      // Calling with "true" returns a URL suitable for a GET request.
      resp.sendRedirect(authReq.getDestinationUrl(true));
    } catch (MessageException e) {
      throw new ServletException(e);
    } catch (ConsumerException e) {
      throw new ServletException(e);
    }
  }
}

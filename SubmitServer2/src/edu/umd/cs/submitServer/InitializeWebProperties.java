package edu.umd.cs.submitServer;

import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class InitializeWebProperties implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		ServletContext ctx = servletContextEvent.getServletContext();

	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		try {
			ServletContext ctx = servletContextEvent.getServletContext();
			InputStream in = InitializeWebProperties.class
					.getResourceAsStream("/web.properties");
			if (in == null)
				throw new IllegalStateException("Did not find web.properties");
			Properties p = new Properties();
			p.load(in);
			in.close();
			for (Object o : p.keySet()) {
				ctx.setInitParameter((String) o, (String) p.get(o));
			}
			if (ctx.getInitParameter("grades.server.jdbc.url") != null)
			    ctx.setInitParameter("grades.server", "true");

			String keyStore = ctx.getInitParameter(SubmitServerConstants.AUTHENTICATION_KEYSTORE_PATH);
			String keyPass = ctx.getInitParameter(SubmitServerConstants.AUTHENTICATION_KEYSTORE_PASSWORD);
			useSSL(keyStore, keyPass);
	 
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
	
	  public void useSSL(String cacertsFile, String cacertsPassword) {
	        if (cacertsFile != null)
	            System.setProperty("javax.net.ssl.trustStore", cacertsFile);
	        if (cacertsPassword != null)
	            System.setProperty("javax.net.ssl.trustStorePassword", cacertsPassword);
	    }

}

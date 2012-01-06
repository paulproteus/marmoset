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
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}

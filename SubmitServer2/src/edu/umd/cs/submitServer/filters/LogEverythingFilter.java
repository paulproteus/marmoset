package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogEverythingFilter extends SubmitServerFilter  {
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		
		System.out.println(request.getRequestURI());
		
		try {
		chain.doFilter(request, response);
		} catch (Throwable t) {
		    t.printStackTrace();
		    throw wrapIfNeededAndThrow(t);
		}
		System.out.println("finished " + request.getRequestURI());
       

	}
	
	  public static RuntimeException wrapIfNeededAndThrow(Throwable e) {
	        if (e instanceof ExecutionException) {
	            return wrapIfNeededAndThrow(e.getCause());
	        }
	        if (e instanceof RuntimeException)
	            throw (RuntimeException) e;
	        if (e instanceof Error)
	            throw (Error) e;
	        throw new RuntimeException(e);
	    }

}

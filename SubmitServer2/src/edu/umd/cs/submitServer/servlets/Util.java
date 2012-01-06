package edu.umd.cs.submitServer.servlets;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

public class Util {

	public static void setAttachmentHeaders(HttpServletResponse response,
			String filename) {
		String disposition = "attachment; filename=\"" + filename + "\"";
		response.setHeader("Content-Disposition", disposition);
		setNoCache(response);
	}

	/** Tell client not to cache the response. */
	public static void setNoCache(HttpServletResponse response) {

		// See http://www.jguru.com/faq/view.jsp?EID=377

		response.setHeader("Cache-Control", "private");
		response.setHeader("Pragma", "IE is broken");
		response.setDateHeader("Expires", 0); // prevents caching at the proxy
												// server

	}
	
	/** Encode a string for a url parameter. */
	public static String urlEncode(String s) {
		try {
	    return URLEncoder.encode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
    	// This should never happen.
	    throw new IllegalArgumentException(e);
    }
	}
	
	/** Decode a string from a url parameter. */
	public static String urlDecode(String s) {
		try {
	    return URLDecoder.decode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
    	// This should never happen.
	    throw new IllegalArgumentException(e);
    }
	}

}

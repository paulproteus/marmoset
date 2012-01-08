package edu.umd.cs.marmoset.utilities;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class Charsets {

	public final static Charset UTF8 = Charset.forName("UTF-8");
	
	public static String sanityCheck(String s) {
	    if (s.indexOf('\n') >= 0)
	        throw new IllegalArgumentException(s);
	    return s;
	}
	
	public static String encodeURL(String s) {
		try {
			return sanityCheck(URLEncoder.encode(sanityCheck(s), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("UTF-8 should always be available");
		}
	}
	
	public static String decodeURL(String s) {
		try {
			return sanityCheck(URLDecoder.decode(sanityCheck(s), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("UTF-8 should always be available");
		}
	}

}

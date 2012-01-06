package edu.umd.cs.marmoset.utilities;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class Charsets {

	public final static Charset UTF8 = Charset.forName("UTF-8");
	
	public static String encodeURL(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("UTF-8 should always be available");
		}
	}
	
	public static String decodeURL(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("UTF-8 should always be available");
		}
	}

}

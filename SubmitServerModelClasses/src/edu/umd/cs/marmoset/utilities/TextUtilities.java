package edu.umd.cs.marmoset.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.CheckForNull;

public class TextUtilities {


	private static final FileNameMap mimeMap = URLConnection.getFileNameMap();
	static final HashSet<String> binaryFileExtensions = new HashSet<String>(
			Arrays.asList("o","class","zip","jar","gif","png","jpg","mp4", "exe", "tar", "Z", "gz", "tgz" , "img"));

	public static void main(String args[]) throws IOException {
		for(String e : binaryFileExtensions)
			System.out.printf("%6s %s%n", e, mimeMap.getContentTypeFor("foo." + e));

		 Map<String, List<String>> m = scanTextFilesInZip(new File(args[0]));
		 for(Map.Entry<String, List<String>> e : m.entrySet()) {
			 System.out.println(e.getKey());
			 if (false) for(String s : e.getValue()) {
				 System.out.println("  " + s);
			 }
		 }

	}

	   public static Map<String, List<String>> scanTextFilesInZip(byte[] in) 
	           throws IOException {
	       return scanTextFilesInZip(in, null);
	   }

	public static Map<String, List<String>> scanTextFilesInZip(byte[] in, DisplayProperties fileProperties) throws IOException {
		
		Map<String, List<String>> result = scanTextFilesInZip(new ByteArrayInputStream(in), fileProperties);
		if (FixZip.hasProblem(in))
			result.put(" -- warning --", Arrays.asList(new String[] { "This zip file doesn't match the zip spec.", 
					"It is likely that the submit server will only show the first entry in the zip file.",
					"At the moment, WinZip is the only tool that is known to produce these badly formed zip files."}));
			
		return result;
	}
	public static Map<String, List<String>> scanTextFilesInZip(File f) throws IOException {
		return scanTextFilesInZip(new FileInputStream(f));
	}

	   public static SortedSet<String> scanTextFileNamesInZip(byte [] bytes) throws IOException {
	       return scanTextFileNamesInZip(new ByteArrayInputStream(bytes));
	   }
	       
	   public static SortedSet<String> scanTextFileNamesInZip(InputStream in) throws IOException {
	       SortedSet<String> result = new TreeSet<String>();
	        ZipInputStream zIn = new ZipInputStream(in);
	        while (true) { 
	            try {
	            ZipEntry z = zIn.getNextEntry();
	            if (z == null) break;
	            if (z.isDirectory())
	                continue;
	            if (z.getSize() > 100000) {
	                continue;
	            }
	            String name = z.getName();

	            int lastSlash = name.lastIndexOf('/');
	            String simpleName = name.substring(lastSlash + 1);
	            if (simpleName.isEmpty() || simpleName.charAt(0) == '.')
	                continue;
	            if (simpleName.charAt(0) == '.' || name.contains("CVS/"))
	                continue;
	            if (simpleName.endsWith("~"))
	                continue;
	            if (name.charAt(0) == '.' || name.contains("/."))
	                continue;

	            @CheckForNull String mimeType = mimeMap.getContentTypeFor(name);

	            if ("application/octet-stream".equals(mimeType))
	                continue;

	            if (!"text/plain".equals(mimeType)) {
	                int lastDot = name.lastIndexOf('.');
	                if (lastDot > 0) {
	                    String extension = name.substring(lastDot + 1);
	                    if (binaryFileExtensions.contains(extension))
	                        continue;
	                }
	            }
	            result.add(name);
	            } catch (Exception e) {
	                String err = dumpException(e);
	                result.add("** ERROR EXTRACTING ZIP ENTRY **");
	                break;
	            }
	        }
	        zIn.close();
	        return result;

	    }
	   
	public static Map<String, List<String>> scanTextFilesInZip(InputStream in) throws IOException {
	    return scanTextFilesInZip(in, null);
	}
	       
	
	public static Map<String, List<String>> scanTextFilesInZip(InputStream in, 
	        @CheckForNull DisplayProperties displayProperties) throws IOException {
	    if (displayProperties == null)
	        displayProperties = new DisplayProperties();
		TreeMap<String, List<String>> result = new TreeMap<String, List<String>>();
		List<String> submitDisplay = null;
		ZipInputStream zIn = new ZipInputStream(in);
		while (true) { 
			try {
			ZipEntry z = zIn.getNextEntry();
			if (z == null) break;
			if (z.isDirectory())
				continue;
			if (z.getSize() > 100000) {
			    System.out.println("Discarding " + z.getName() + ", size " + z.getSize());
				continue;
			}
			String name = z.getName();
			if (name.equals(".submitDisplay")) {
			    submitDisplay =  getText(zIn);
			}
			int lastSlash = name.lastIndexOf('/');
			String simpleName = name.substring(lastSlash + 1);
			
			if (simpleName.isEmpty() || simpleName.charAt(0) == '.')
				continue;
			if (simpleName.charAt(0) == '.' || name.contains("CVS/"))
				continue;
			if (simpleName.endsWith("~"))
			    continue;
			if (name.charAt(0) == '.' || name.contains("/."))
				continue;

			@CheckForNull String mimeType = mimeMap.getContentTypeFor(name);

			if ("application/octet-stream".equals(mimeType))
				continue;

			if (!"text/plain".equals(mimeType)) {
				int lastDot = name.lastIndexOf('.');
				if (lastDot > 0) {
					String extension = name.substring(lastDot + 1);
					if (binaryFileExtensions.contains(extension))
						continue;
				}
			}

			List<String> contents = getText(zIn);
			if (contents != null) {
				result.put(name, contents);
			}
			} catch (Exception e) {
				String err = dumpException(e);
				result.put("** ERROR EXTRACTING ZIP ENTRY **", Arrays.asList(err.split("\n")));
				break;
				
			}
		}
		zIn.close();
		if (submitDisplay != null) {
		    final HashMap<String,Integer> rank = new HashMap<String,Integer>();
		    int pos = 0;
		    for(String s : submitDisplay) {
		        if (s.startsWith("#")) continue;
		        String [] parts = s.split("[ ]*=[ ]*");
		        if (parts.length == 0) continue;
		        rank.put(parts[0], pos++);
		        if (parts.length == 2)
		            displayProperties.put(parts[0], parts[1]);
		        else
		            displayProperties.put(parts[0]);
		            
		    }
		}
		return displayProperties.build(result);
	}
	
	public static String dumpException(Throwable t) {
		if (t == null)
			return "";
		StringWriter w = new StringWriter();
		PrintWriter out = new PrintWriter(w);
		Throwable e = t;
		while (e != null) {
			e.printStackTrace(out);
			e = e.getCause();
			if (e != null)
				out.println("Caused by:");
		}
		out.close();
		return w.toString();
	}

	public static @CheckForNull List<String> getText(InputStream in) throws IOException {
		BufferedInputStream bIn = new BufferedInputStream(in);
		byte[] check = new byte[400];
		bIn.mark(check.length);
		int size = bIn.read(check);
		if (size == 0)
			return null;

		int countHighBytes = 0;
		for(int i = 0; i < size; i++) {
			int b = check[i] & 0xff;
			if (b < ' ' && b != '\r' && b != '\n' && b != '\t')
				return null;
			if (b > 0x7f)
				countHighBytes++;
		}
		if (countHighBytes > size/4)
			return null;
		bIn.reset();
		BufferedReader reader = new BufferedReader(new InputStreamReader(bIn, "UTF-8"));
		ArrayList<String> result = new ArrayList<String>();
		while(true) {
			String s = reader.readLine();
			if (s == null)
				break;
			if (result.size() > 5000)
				return Arrays.asList(
						String.format(
						"File contains  %,d lines; not included in source listing", result.size()));
			if (s.length() > 500)
				result.add(s.substring(500) + "... (elided)");
			else
				result.add(s);
		}
		return result;
	}

}

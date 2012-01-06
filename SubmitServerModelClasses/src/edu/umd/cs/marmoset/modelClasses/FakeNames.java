package edu.umd.cs.marmoset.modelClasses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FakeNames {
	
	static final ArrayList<String> fakeNames = new ArrayList<String>();
	
	static {
		
		InputStream in = FakeNames.class.getResourceAsStream("fakeNames.txt");
		try {
		    BufferedReader r = new BufferedReader(new InputStreamReader(in));
		while (true) {
			String s = r.readLine();
			if (s == null)
				break;
			fakeNames.add(s);
		}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
		    IO.closeInputStreamAndIgnoreIOException(in);
		}
		    
		System.out.println("Loaded " + fakeNames.size() + " fake names");
	}
	
	static public String getFullname(Object key) {
		int x = key.hashCode();
		int idx = Math.abs(x % fakeNames.size());
		return fakeNames.get(idx);
	}
	
	static public String getAccount(Object key) {
        return String.format("acct%04d", Math.abs(key.hashCode() % 1000));
    }
	static int getDivider(String fullname) {
		return fullname.lastIndexOf(' ');
	}
	static public String getFirstname(Object key) {
		String fullname = getFullname(key);
		return fullname.substring(0, getDivider(fullname));
	}
	static public String getLastname(Object key) {
		String fullname = getFullname(key);
		int pos = getDivider(fullname)+1;
		return fullname.substring(pos);
	}

}

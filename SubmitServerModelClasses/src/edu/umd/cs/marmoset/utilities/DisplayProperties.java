package edu.umd.cs.marmoset.utilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;

public class DisplayProperties {

	static public class FileProperties {
		final String s;

		private FileProperties(String s) {
			this.s = s.toLowerCase();
		}

		public boolean isReadonly() {
			return s.contains("readonly");
		}

		public boolean isCollapsed() {
			return s.contains("collapsed");
		}

		public boolean isLast() {
			return s.contains("last");
		}
		
		public boolean isComplete() {
			return s.contains("complete");
		}

		public boolean isHidden() {
			return s.contains("hidden");
		}

		@Override
		public String toString() {
			return s;
		}

		static final FileProperties NONE = new FileProperties("");
		static final FileProperties HIDDEN = new FileProperties("hidden");

		static FileProperties build(@CheckForNull String s) {
			if (s == null || s.isEmpty())
				return NONE;
			return new FileProperties(s);
		}
	}

	static public class Entry {
		String name;
		String endsWith;
		Pattern regex;
		int rank;
		FileProperties properties;

		Entry(String s, int rank, FileProperties properties) {
			try {
				if (s.startsWith("*."))
					endsWith = s.substring(1);
				else if (s.startsWith("/") && s.endsWith("/")
						&& s.length() >= 2)
					regex = Pattern.compile(s.substring(1, s.length() - 1));
				else
					name = s;
			} catch (RuntimeException e) {
				name = s;
			}
			if (properties.isLast())
				rank += 4 * UNRANKED;
			this.rank = rank;
			this.properties = properties;

		}
		


		public boolean isPattern() {
			return regex != null || endsWith != null;
		}

		public boolean matches(String filename) {
			if (name != null)
				return name.equals(filename);
			if (endsWith != null)
				return filename.endsWith(endsWith);

			return regex.matcher(filename).matches();
		}

		public boolean endsWith(String filename) {
			if (name != null)
				return filename.endsWith(name);
			if (endsWith != null)
				return filename.endsWith(endsWith);
			Matcher m = regex.matcher(filename);
			while (m.find()) {
				if (m.end() == filename.length())
					return true;
			}
			return false;
		}

		@Override
		public String toString() {
			if (name != null)
				return String.format("%12s %2d %s", name, rank, properties);
			if (endsWith != null)
				return String.format("%12s %2d %s", "*" + endsWith, rank,
						properties);
			return String.format("%12s %2d %s", regex.pattern(), rank, properties);
		}
	}

	int nextRank = 0;
	
	int context = 5;
	boolean complete = false;

	final static int UNRANKED = 100000;

	final ArrayList<Entry> entries = new ArrayList<Entry>();

	boolean hasPattern = false;
	final Entry UNMATCHED = new Entry("/.*/", 2 * UNRANKED, FileProperties.NONE);
	final Entry HIDDEN = new Entry("/.*/", 2 * UNRANKED, FileProperties.HIDDEN);

	final Entry JAVA_TESTS = new Entry("Tests.java", UNRANKED,
			FileProperties.NONE);

	final Map<String, Entry> matched = new HashMap<String, Entry>();

	public void initialize(List<String> contents) {
		for (String s : contents) {
			if (s.startsWith("#"))
				continue;
			s = s.trim();
			String[] parts = s.split("[ ]*[=\t][ ]*");
			if (parts.length == 0)
				continue;
			if (parts.length == 2) {
				if (parts[0].equals("$context")) {
					try {
						context = Integer.parseInt(parts[1]);
					} catch (RuntimeException e) {
						
					}
				} else  
					put(parts[0], parts[1]);
			} else if (parts.length == 1) {
				if (parts[0].equals("$complete"))
					complete = true;
				else put(parts[0],null);
			}
		}
	}
	public Entry getEntry(String filename) {
		Entry e = matched.get(filename);
		if (e != null)
			return e;
		e = getEntry0(filename);
		matched.put(filename, e);
		return e;
	}

	public Iterable<Entry> getEntries() {
		return entries;
	}

	public int getContext(String filename) {
		return context;
	}
	public boolean isComplete(String filename) {
		if (complete) return true;
		Entry e = getEntry(filename);
		return e.properties.isComplete();
	}
	public Entry getEntry0(String filename) {
		for (Entry e : entries)
			if (e.matches(filename))
				return e;
		for (Entry e : entries)
			if (e.endsWith(filename))
				return e;
		if (hasPattern)
			return HIDDEN;
		if (JAVA_TESTS.endsWith(filename))
			return JAVA_TESTS;

		return UNMATCHED;
	}

	public boolean isEmpty() {
		return entries.isEmpty();
	}

	private void put(String filename) {
		put(filename, null);
	}

	private void put(String filename, @CheckForNull String properties) {
		Entry e = new Entry(filename, nextRank++,
				FileProperties.build(properties));
		if (e.isPattern())
			hasPattern = true;
		entries.add(e);
	}

	public Map<String, List<String>> build(
			Map<String, List<String>> sourceContents) {
		if (sourceContents.isEmpty())
			return sourceContents;
		TreeMap<String, List<String>> sorted = new TreeMap<String, List<String>>(
				fileComparator());
		for (Map.Entry<String, List<String>> e : sourceContents.entrySet()) {
			String filename = e.getKey();
			Entry entry = getEntry(filename);
			if (entry == null || !entry.properties.isHidden())
				sorted.put(filename, e.getValue());
		}
		return sorted;
	}

	static int getSuffixStart(String s) {
		int lastSlash = s.lastIndexOf('/');
		int lastDot = s.lastIndexOf('.', lastSlash + 1);
		if (lastDot < 0)
			return s.length();
		return lastDot;
	}

	static int getSuffixRank(String suffix) {
		if (suffix.startsWith(".h"))
			return 1;
		return 2;
	}

	public Comparator<String> fileComparator() {
		return new Comparator<String>() {

			@Override
			public int compare(String arg0, String arg1) {
				Entry e0 = getEntry(arg0);
				Entry e1 = getEntry(arg1);
				int result = e0.rank - e1.rank;
				if (result != 0)
					return result;
				int i0 = getSuffixStart(arg0);
				int i1 = getSuffixStart(arg1);
				result = arg0.substring(0, i0).compareTo(arg1.substring(0, i1));
				if (result != 0)
					return result;
				String s0 = arg0.substring(i0).toLowerCase();
				String s1 = arg1.substring(i1).toUpperCase();
				result = getSuffixRank(s0) - getSuffixRank(s1);
				if (result != 0)
					return result;

				return arg0.compareTo(arg1);
			}
		};
	}

}

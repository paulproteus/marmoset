/**
 * Marmoset: a student project snapshot, submission, testing and code review
 * system developed by the Univ. of Maryland, College Park
 * 
 * Developed as part of Jaime Spacco's Ph.D. thesis work, continuing effort led
 * by William Pugh. See http://marmoset.cs.umd.edu/
 * 
 * Copyright 2005 - 2011, Univ. of Maryland
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

/*
 * Created on May 2, 2005
 */
package edu.umd.cs.buildServer.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to extract arguments from a string. Arguments can be separated using
 * whitespace, or may be quoted using double quote characters.
 *
 * @author Bill Pugh
 * @author David Hovemeyer
 */
public class ArgumentParser {
	static final Pattern ARG_REGEX_PATTERN = Pattern
			.compile("\"(?:[^\"\\\\]|\\\\.)*\"|\\S+");

	// Fields
	private Matcher m;
	private String next;

	/**
	 * Constructor.
	 *
	 * @param value
	 *            the String to be parsed
	 */
	public ArgumentParser(String value) {
		m = ARG_REGEX_PATTERN.matcher(value);
	}

	/**
	 * Return whether or not the string contains another argument.
	 */
	public boolean hasNext() {
		fetchNext();
		return next != null;
	}

	/**
	 * Get the next argument. Must call hasNext() first to ensure that there
	 * actually is another argument.
	 */
	public String next() {
		fetchNext();
		if (next == null)
			throw new IllegalStateException();
		String result = next;
		next = null;
		return result;
	}

	private void fetchNext() {
		if (next == null) {
			if (m.find())
				next = m.group(0);
		}
	}
}

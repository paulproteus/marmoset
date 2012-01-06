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

package edu.umd.cs.buildServer.tester;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipFile;


/**
 * Find trusted code bases on the system classpath.
 */
final class TrustedCodeBaseFinder {
	private final JavaTester tester;

	/**
	 * Constructor.
	 *
	 * @param tester
	 *            the JavaTester
	 */
	TrustedCodeBaseFinder(JavaTester tester) {
		this.tester = tester;
	}

	private List<TrustedCodeBase> trustedCodeBaseList = new LinkedList<TrustedCodeBase>();

	/**
	 * Build the list of trusted code bases.
	 */
	public void execute() {
		StringTokenizer tok = new StringTokenizer(
				System.getProperty("java.class.path"), File.pathSeparator);
		while (tok.hasMoreTokens()) {
			String element = tok.nextToken();
			inspectEntry(element);
		}
	}

	/**
	 * Get collection of trusted codebase objects.
	 *
	 * @return
	 */
	public Collection<TrustedCodeBase> getCollection() {
		return trustedCodeBaseList;
	}

	/**
	 * Look at a classpath entry to decide whether or not it is a trusted code
	 * base.
	 *
	 * @param entry
	 *            the classpath entry
	 */
	private void inspectEntry(String entry) {
		if (entry.endsWith(File.separator + "junit.jar")) {
			// JUnit
			if (!hasJUnit)
			  addTrustedCodeBase("buildserver.junit.jar.file", entry);
			hasJUnit = true;
		} else {
			// See if this codebase has the testrunner in it
			if (elementContainsClass(entry, Tester.class.getName()))
				addTrustedCodeBase("buildserver.tester.codebase", entry);
		}
	}

	private boolean hasJUnit = false;

	/**
	 * Determine if the given classpath entry contains the named class
	 *
	 * @param entry
	 *            the classpath entry
	 * @param className
	 *            the class name
	 * @return true if the class is defined in the entry
	 */
	private boolean elementContainsClass(String entry, String className) {
		String fileName = className.replace('.', '/') + ".class";
		File f = new File(entry);
		if (f.isDirectory()) {
			return new File(f, fileName).isFile();
		} else if (f.isFile()) {
			ZipFile z = null;
			try {
				z = new ZipFile(f);
				return (z.getEntry(fileName) != null);
			} catch (IOException e) {
				// Ignore
			} finally {
				try {
					if (z != null)
						z.close();
				} catch (IOException ignore) {
					// Ignore
				}
			}
		}
		return false;
	}

	private void addTrustedCodeBase(String property, String value) {
		this.tester.getLog().debug(
				"Trusted code base: " + property + "=" + value);
		trustedCodeBaseList.add(new TrustedCodeBase(property, value));
	}
}

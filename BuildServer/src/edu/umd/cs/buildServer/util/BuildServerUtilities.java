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
 * Created on Mar 25, 2005
 */
package edu.umd.cs.buildServer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestProperties;

/**
 * @author jspacco
 *
 */
public class BuildServerUtilities {

	/**
	 * @param inputStream
	 * @param out
	 */
	private static void readFromStream(final InputStream inputStream,
			final StringBuffer out) {
		Thread t = new Thread() {
			@Override
			public void run() {

				try {

					BufferedReader reader = new BufferedReader(
							new InputStreamReader(inputStream));
					String line;
					while ((line = reader.readLine()) != null) {
						out.append(line + "\n");
					}
				} catch (IOException e) {
					System.err
							.println("Exception getting output stream from proc process:"
									+ e);
				} finally {
					try {
						inputStream.close();
					} catch (IOException e) {
						assert true;
					}
				}
			}
		};
		t.start();
	}

	public static void main(String args[]) throws Exception {
		File dir = new File("/workspace/BuildServer/bs1.localhost/build/obj");
		TestProperties testProperties = new TestProperties();
		testProperties
				.load(new File(
						"/workspace/BuildServer/bs1.localhost/testfiles/test.properties"));
		List<File> list = listNonCloverClassFilesInDirectory(dir,
				testProperties);
		for (File f : list) {
			System.out.println(f);
		}
	}

	public static List<File> listNonCloverClassFilesInDirectory(File dir,
			final TestProperties testProperties) {
		List<File> results = new ArrayList<File>();
		listDirContents(dir, new FileFilter() {
			@Override
			public boolean accept(File file) {
				String absolutePath = file.getAbsolutePath();
				if (CodeCoverageResults.isJUnitTestSuite(absolutePath))
					return false;
				// Skip the classfiles containing JUnit tests since covering
				// test cases isn't interesting
				for (String testClass : TestOutcome.getDynamicTestTypes()) {
					String className = testProperties.getTestClass(testClass);
					if (className != null) {
						// Make the classname look like a path
						className = new File(className).getName().replace('.',
								File.separatorChar);

						// If this is one of the test classes, then skip it!
						if (absolutePath.contains(className))
							return false;
					}
				}

				// XXX MAJOR HACK: have to figure out what to exclude from
				// properties file
				if (file.getName().contains("TestingSupport"))
					return false;
				// Skip anything that looks like a clover classfile
				if (file.getName().matches(".*class")
						&& !file.getName().contains("CLOVER"))
					return true;
				return false;
			}
		}, results);
		return results;
	}

	public static List<File> listClassFilesInDirectory(File dir) {
		List<File> results = new ArrayList<File>();
		listDirContents(dir, new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.getName().matches(".*class"))
					return true;
				return false;
			}
		}, results);
		return results;
	}

	/**
	 * Appends all the files in a given directory or its subdirectories that
	 * match the given filter.
	 *
	 * @param dir
	 *            the directory
	 * @param filter
	 *            the filter determines whether a file should be added to the
	 *            results list
	 * @param results
	 *            the list to append the matching files to
	 */
	private static void listDirContents(File dir, FileFilter filter,
			List<File> results) {
		File[] fileArr = dir.listFiles();
		for (int ii = 0; ii < fileArr.length; ii++) {
			File file = fileArr[ii];
			if (file.isDirectory()) {
				listDirContents(file, filter, results);
			}
			if (file.isFile()) {
				if (filter.accept(file))
					results.add(file);
			}
		}
	}

	public static File createTempDirectory() throws IOException {
		File tmpDir = File.createTempFile("prefix", "suffix");
		tmpDir.delete();
		tmpDir.mkdirs();
		return tmpDir;
	}

	public static List<File> listDirContents(File dir) {
		List<File> result = new LinkedList<File>();
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				listDirContentsHelper(file, result);
			} else {
				result.add(file);
			}
		}
		return result;
	}

	private static void listDirContentsHelper(File dir, List<File> result) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					listDirContentsHelper(file, result);
				} else {
					result.add(file);
				}
			}
		}
	}
}

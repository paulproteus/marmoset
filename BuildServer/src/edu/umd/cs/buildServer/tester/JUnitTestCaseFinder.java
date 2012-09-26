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
 * Created on Mar 9, 2005
 */
package edu.umd.cs.buildServer.tester;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.util.ClassPath;
import org.apache.bcel.util.SyntheticRepository;
import org.apache.log4j.Logger;

import edu.umd.cs.buildServer.BuildServer;

/**
 * Use BCEL to inspect a Java class to see if it implements JUnit tests.
 *
 * @author David Hovemeyer
 * @author Nat Ayewah
 */
public class JUnitTestCaseFinder implements Iterable<JUnitTestCase> {
	private JavaClass inspectedClass;
	private List<JUnitTestCase> testCaseList;
	private List<String> classPathEntryList;

	private Logger log;

	private Logger getLog() {
		if (log != null)
			return log;
		log = Logger.getLogger(BuildServer.class);
		return log;
	}

	/**
	 * Constructor. Class to inspect must be specified before findTestCases() is
	 * called.
	 */
	public JUnitTestCaseFinder() {
		this.testCaseList = new LinkedList<JUnitTestCase>();
		this.classPathEntryList = new LinkedList<String>();
	}

	/**
	 * Add a classpath entry. This will allow loading of the inspected class
	 * from precisely the codebase desired. Added entries should be specified in
	 * decreasing order of priority (i.e., the first one added has highest
	 * precedence), and will have precedence over system classpath entries.
	 *
	 * @param entry
	 *            classpath entry; filename of a jar or zip file, or the name of
	 *            a directory
	 */
	public void addClassPathEntry(String entry) {
		classPathEntryList.add(entry);
	}

	/**
	 * Set class to inspect.
	 *
	 * @param classFile
	 *            class file containing the class to inspect
	 * @throws ClassFormatException
	 * @throws IOException
	 */
	public void setInspectedClass(File classFile) throws ClassFormatException,
			IOException {
		setInspectedClass(new ClassParser(classFile.getPath()).parse());
	}

	/**
	 * Set class to inspect.
	 *
	 * @param inspectedClass
	 *            the class to inspect
	 */
	public void setInspectedClass(JavaClass inspectedClass) {
		initRepository();
		this.inspectedClass = inspectedClass;
		Repository.addClass(inspectedClass);
	}

	/**
	 * Set class to inspect.
	 *
	 * @param className
	 *            class name of the class to inspect; will be loaded from the
	 *            Repository
	 * @throws ClassNotFoundException
	 */
	public void setInspectedClass(String className)
			throws ClassNotFoundException {
		initRepository();
		try {
		this.inspectedClass = Repository.lookupClass(className);
		} catch (org.apache.bcel.classfile.ClassFormatException e) {
		    log.error("Error parsing class " + className , e);
		    throw new ClassNotFoundException("BCEL is unable to parse class " + className, e);
		}
	}

	/**
	 * Find JUnit test cases (if any). Throws an exception if the inspection
	 * fails.
	 *
	 * Also includes support for JUnit4 Tests. If inspectedClass extends
	 * TestCase, then it is treated as a JUnit 3 test. Otherwise it is treated
	 * as a JUnit 4 test.
	 *
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void findTestCases() throws ClassNotFoundException {
		if (inspectedClass == null)
			throw new IllegalStateException("Class to inspect has not been set");

		getLog().trace("Looking for test cases in " + inspectedClass);
		for (String entry : classPathEntryList) {
			getLog().trace("classpath entry to look for JUnit tests: " + entry);
		}

		JavaClass junitTestCaseClass = Repository
				.lookupClass("junit.framework.TestCase");

		if (Repository.instanceOf(inspectedClass, junitTestCaseClass))
			findJUnit3TestCases();
		else
			findJUnit4TestCases();
	}

	private void findJUnit3TestCases() {

		Method[] methodList = inspectedClass.getMethods();
		for (Method method : methodList) {
			if (method.isAbstract() || method.isNative())
				continue;

			// Test methods must be public
			if (!method.isPublic())
				continue;

			// Ignore constructors
			if (method.getName().equals("<init>"))
				continue;

			// Ignore methods that don't have a name that starts with test
			if (!method.getName().startsWith("test"))
				continue;

			// Test methods must take no params and must return void
			if (!method.getSignature().equals("()V"))
				continue;

			testCaseList.add(new JUnitTestCase(inspectedClass, method, null));
		}
	}

	private void findJUnit4TestCases() {

		for(Method method : inspectedClass.getMethods()) {
			for(AnnotationEntry a : method.getAnnotationEntries()) {
				if (a.getAnnotationType().equals("Lorg/junit/Test;"))
					testCaseList.add(new JUnitTestCase(inspectedClass, method, null));
			}
		}

	}

	/**
	 * Return an Iterator over all of the {@link JUnitTestCase}s found in the
	 * inspected class.
	 *
	 * @return Iterator over JUnitTestCase objects
	 */
	@Override
	public Iterator<JUnitTestCase> iterator() {
		return testCaseList.iterator();
	}

	/**
	 * Get the Collection of {@link JUnitTestCase}s found in the inspected
	 * class.
	 *
	 * @return Collection of JUnitTestCase objects
	 */
	public Collection<JUnitTestCase> getTestCaseCollection() {
		return testCaseList;
	}

	/**
	 * Get the JavaClass object representing the inspected class.
	 *
	 * @return the JavaClass object representing the inspected class
	 */
	public JavaClass getInspectedClass() {
		return inspectedClass;
	}

	private void initRepository() {
		StringBuffer reposClassPath = new StringBuffer();

		// Add user-specified classpath entries
		for (Iterator<String> i = classPathEntryList.iterator(); i.hasNext();) {
			if (reposClassPath.length() > 0)
				reposClassPath.append(File.pathSeparatorChar);
			reposClassPath.append(i.next());
		}

		// Add system classpath entries
		if (reposClassPath.length() > 0) {
			reposClassPath.append(File.pathSeparatorChar);
		}
		reposClassPath.append(ClassPath.getClassPath());

		// Create new SyntheticRepository, and make it current
		ClassPath classPath = new ClassPath(reposClassPath.toString());
		// XXX VERY IMPORTANT:
		// We need to clear the cache because the Repository caches classfiles
		// and
		// the BuildServer runs in a loop in the same JVM invocation.
		Repository.clearCache();
		SyntheticRepository repos = SyntheticRepository.getInstance(classPath);
		Repository.setRepository(repos);
	}

	private static void lookupClassFile(String[] args) throws Exception {
		JUnitTestCaseFinder testCaseFinder = new JUnitTestCaseFinder();

		for (int ii = 1; ii < args.length; ii++) {
			testCaseFinder.addClassPathEntry(args[ii]);
		}
		testCaseFinder.setInspectedClass(args[0]);
		testCaseFinder.findTestCases();

	}

	public static void main(String args[]) throws Exception {
		if (args.length < 1) {
			System.err.println("Usage: " + JUnitTestCaseFinder.class.getName()
					+ " <class file>");
			System.exit(1);
		}

		System.out.println("first call");
		lookupClassFile(args);

		System.err.println("Waiting...");
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		reader.readLine();

		lookupClassFile(args);
	}
}

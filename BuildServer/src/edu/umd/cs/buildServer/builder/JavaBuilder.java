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
 * Created on Jan 20, 2005
 */
package edu.umd.cs.buildServer.builder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.dom4j.DocumentException;

import edu.umd.cs.buildServer.BuilderException;
import edu.umd.cs.buildServer.CompileFailureException;
import edu.umd.cs.buildServer.ConfigurationKeys;
import edu.umd.cs.buildServer.MissingConfigurationPropertyException;
import edu.umd.cs.buildServer.ProjectSubmission;
import edu.umd.cs.buildServer.inspection.CodeMetricsComputation;
import edu.umd.cs.buildServer.tester.TestRunner;
import edu.umd.cs.buildServer.util.BuildServerUtilities;
import edu.umd.cs.buildServer.util.CombinedStreamMonitor;
import edu.umd.cs.buildServer.util.Untrusted;
import edu.umd.cs.marmoset.modelClasses.CodeMetrics;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestProperties;
import edu.umd.cs.marmoset.modelClasses.TestPropertyKeys;

/**
 * Build a Java submission.
 *
 * @author David Hovemeyer
 */
public class JavaBuilder extends Builder implements TestPropertyKeys {
	/**
	 * Constructor.
	 *
	 * @param testProperties
	 *            TestProperties loaded from project jarfile's test.properties
	 * @param projectSubmission
	 *            the ProjectSubmission to build
	 * @param directoryFinder
	 *            DirectoryFinder used to locate build and testfiles directories
	 */
	public JavaBuilder(TestProperties testProperties,
			ProjectSubmission projectSubmission,
			DirectoryFinder directoryFinder,
			SubmissionExtractor submissionExtractor) {
		super(testProperties, projectSubmission, directoryFinder,
				submissionExtractor);
	}

	/*
	 * Get the directory prefix leading to the Java project. Returns an empty
	 * string if the project is in the root directory of the submission zipfile.
	 *
	 * Right now, the only thing we do is to look for an Eclipse ".project"
	 * file. If we find it, that is where the project is.
	 */
	@Override
	protected String getProjectPathPrefix() throws IOException {
		String prefix = "";
		ZipFile z = new ZipFile(getProjectSubmission().getZipFile());
		try {
			Enumeration<? extends ZipEntry> e = z.entries();
			while (e.hasMoreElements()) {
				ZipEntry entry = e.nextElement();
				String entryName = entry.getName();
				// XXX Note that we're only looking for something that ends with
				// .project
				// so it would be really easy for this algorithm to mess up!
				// Also, note that the order in which files come out of the
				// zipfile is
				// extremely important! E.g. if the order is
				//
				// Images/.project
				// .project
				//
				// Then we will get the wrong prefix ("Images") instead of an
				// empty path.
				if (entryName.endsWith(".project")) {
					prefix = entryName.substring(0, entryName.length()
							- ".project".length());
					break;
				}
			}
		} finally {
			try {
				z.close();
			} catch (IOException ignore) {
				// Ignore
			}
		}
		return prefix;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see edu.umd.cs.buildServer.Builder#inspectSubmission()
	 */
	@Override
	protected CodeMetrics inspectSubmission() throws BuilderException,
			CompileFailureException {
		if (getProjectSubmission().getConfig().getConfig().getOptionalBooleanProperty(
				ConfigurationKeys.SKIP_BUILD_INFO)) {
			getLog().info("Skipping build information step");
			return null;
		}

		// perform the compile with debugging turned off so that we don't have a
		// linenumber or variable map and the md5sum of the classfiles will be the same.
		doCompile(false, "-g:none" );

		// Now get a list of all the class files
		File outputDir = getProjectSubmission().getBuildOutputDirectory();

		// get a list of the classfiles
		List<File> classFileList = BuildServerUtilities
				.listClassFilesInDirectory(outputDir);

		for (File file : classFileList) {
			getLog().trace("classfile to inspect: " + file);
		}

		// Now get a list of the source files
		Set<File> sourceFileList = new LinkedHashSet<File>();
		// convert from Strings to files
		for (Iterator<String> ii = getSourceFiles().iterator(); ii.hasNext();) {
			String filePath = ii.next();
			File file = new File(getDirectoryFinder().getBuildDirectory(),
					filePath);
			sourceFileList.add(file);
		}

		try {
			CodeMetrics codeMetrics = new CodeMetrics();
			codeMetrics.setMd5sumClassfiles(classFileList);
			codeMetrics.setMd5sumSourcefiles(sourceFileList);
			int sz = CodeMetricsComputation.computeCodeSegmentSize(
					outputDir, classFileList,
					outputDir.getAbsolutePath());
			codeMetrics.setCodeSegmentSize(sz);
			return codeMetrics;
		} catch (IOException e) {
			getLog().error("Unable to compute md5sum due to IOException!", e);
		} catch (NoSuchAlgorithmException e) {
			getLog().error("md5 algorithm not found!", e);
		} catch (ClassNotFoundException e) {
			getLog().error(
					"Unable to find and load one of the classes in "
							+ outputDir, e);
		}
		return null;
	}

	/**
	 * Compile the project.
	 * @param generateCodeCoverage
	 *            If true, then compile in the "inst-src" directory containing
	 *            classfiles instrumented by Clover rather than the raw
	 *            classfiles extracted from the projectSubmission. <b>NOTE:</b>
	 *            It's now <b>REQUIRED</b> that all source files are rooted in a
	 *            "src" directory!
	 * @param options
	 *            Additional options passed to javac, such as -g:none that keeps
	 *            debugging information out of the classfile.
	 *
	 * @throws BuilderException
	 *             thrown when the compile fails for unexpected reasons (i.e.
	 *             IOException)
	 * @throws CompileFailureException
	 *             thrown when the compile fails for expected reasons (i.e.
	 *             syntax errors, etc.)
	 */
	private void doCompile(boolean generateCodeCoverage, String... options)
			throws BuilderException, CompileFailureException {
		// CODE COVERAGE:
		// Use the programmic interface to Clover to instrument code for
		// coverage
		if (generateCodeCoverage && Clover.isAvailable()) {
			// TODO Put this clover database in the student's build directory
			// TODO Also clean up this file when we're done with it!
			String cloverDBPath;
			try {
				cloverDBPath = getProjectSubmission().getConfig().getConfig()
						.getRequiredProperty(CLOVER_DB);
			} catch (MissingConfigurationPropertyException e) {
				throw new BuilderException(e);
			}

			File cloverDB = new File(cloverDBPath);
			if (cloverDB.exists()) {
				if (!cloverDB.delete())
					getLog().warn(
							"Unable to delete old clover DB at " + cloverDBPath);
			}
			String[] cliArgs = {
					"-source",
					getTestProperties().getJavaSourceVersion(),
					"-i",
					cloverDBPath,
					"-s",
					getProjectSubmission().getSrcDirectory().getAbsolutePath(),
					"-d",
					getProjectSubmission().getInstSrcDirectory()
							.getAbsolutePath() };
			String coverageMarkupCmd = " ";
			for (int ii = 0; ii < cliArgs.length; ii++) {
				coverageMarkupCmd += cliArgs[ii] + " ";
			}
			getLog().trace("Clover instrumentation args: " + coverageMarkupCmd);
			int result = Clover.cloverInstrMainImpl(cliArgs);
			if (result != 0) {
				throw new BuilderException(
						"Clover was unable to instrument the source code in "
								+ getProjectSubmission().getSrcDirectory()
										.getAbsolutePath());
			}
		}

		if (getSourceFiles().isEmpty())
			throw new CompileFailureException("Project "
					+ getProjectSubmission().getZipFile().getPath()
					+ " contains no source files", "");

		// Create compiler output directory
		File outputDir = getProjectSubmission().getBuildOutputDirectory();
		if (!outputDir.isDirectory() && !outputDir.mkdir()) {
			throw new BuilderException(
					"Could not create compiler output directory "
							+ outputDir.getPath());
		}

		// Determine Java -source value to use.
		String javaSourceVersion = getTestProperties().getJavaSourceVersion();

		// Determine the classpath to be used for compiling.
		StringBuffer cp = new StringBuffer();
		cp.append(getProjectSubmission().getTestSetup().getAbsolutePath());
		appendJUnitToClassPath(cp);
		if (generateCodeCoverage)
			appendCloverToClassPath(cp);

		// Specify javac command line arguments.
		LinkedList<String> args = new LinkedList<String>();
		args.add("javac");
		// Specify classpath
		args.add("-classpath");
		args.add(cp.toString());
		// Generate compiled class files in the output directory
		args.add("-d");
		args.add(outputDir.getAbsolutePath());
		// Specify Java source version.
		args.add("-source");
		args.add(javaSourceVersion);
		// add optional args
		if (options != null) {
			args.addAll(Arrays.asList(options));
		}
		// // Compile all source files found in submission

		// XXX Code now MUST be in a "src" directory!
		if (generateCodeCoverage) {
			List<String> newSourceFileList = new LinkedList<String>();
			for (Iterator<String> ii = getSourceFiles().iterator(); ii
					.hasNext();) {
				String originalSourceFile = ii.next();
				String newSourceFile = originalSourceFile.replaceAll("^src",
						INSTRUMENTED_SRC_DIR);
				newSourceFileList.add(newSourceFile);
			}
			args.addAll(newSourceFileList);
		} else {
			// TODO rewrite the source files into the appropriate directory
			// anyway
			args.addAll(getSourceFiles());
		}

		if (getLog().isEnabledFor(Level.DEBUG)) {
			StringBuffer buf = new StringBuffer();
			for (Iterator<String> i = args.iterator(); i.hasNext();) {
				buf.append(i.next() + " ");
			}
			getLog().debug("Javac command: " + buf.toString());
		}

		// Compile all source files found in submission
		// args.addAll(getSourceFileList());

		try {
			Process javac = Untrusted.execute(
					args.toArray(new String[args.size()]), null,
					getDirectoryFinder().getBuildDirectory());

			// Capture stdout and stderr from the process
			CombinedStreamMonitor monitor = new CombinedStreamMonitor(
					javac.getInputStream(), javac.getErrorStream());
			monitor.start();

			// Wait for process to execute, and for all process output
			// to be read
			int exitCode = javac.waitFor();
			monitor.join();

			// If compile failed, collect output messages
			// and throw a CompileFailureException
			if (exitCode != 0) {
				setCompilerOutput(monitor.getCombinedOutput());

				throw new CompileFailureException("Compile failed for project "
						+ getProjectSubmission().getZipFile().getPath(),
						this.getCompilerOutput());
			}

			// Looks like compilation succeeded.
			// Sleep for a few seconds to try to workaround some of
			// the mysterious "file not found" problems we've been
			// seeing when trying to execute the project.
			// (These may be NFS-related.)
			pause(1000);
			

		} catch (IOException e) {
			throw new BuilderException("Couldn't invoke java", e);
		} catch (InterruptedException e) {
			throw new BuilderException("Javac wait was interrupted", e);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see edu.umd.cs.buildServer.Builder#compileProject()
	 */
	@Override
	protected void compileProject() throws BuilderException,
			CompileFailureException {

		// Don't use instrumented source directories when this compilation is
		// for the
		// inspection step. Otherwise FindBugs reports lots of things introduced
		// by Clover.
		if (isInspectionStepCompilation())
			doCompile(false, "-g");
		else
			doCompile(getProjectSubmission().isPerformCodeCoverage(), "-g");
	}


	public static void appendJUnitToClassPath(StringBuffer buf) {
		File f = getJUnitJar();
		addFileToClasspath(buf, f);
	}

	private static void addFileToClasspath(StringBuffer buf, File f) {
		if (f == null) return;
		buf.append(File.pathSeparatorChar);
		buf.append(f.getAbsolutePath());
	}
	public static void appendCloverToClassPath(StringBuffer buf) {
		File f = getCloverJar();
		addFileToClasspath(buf, f);
	}
	public static void appendBuildServerToClasspath(StringBuffer buf) {
		addFileToClasspath(buf, getBuildServerJar());
		addFileToClasspath(buf, getSubmitServerModelClasses());
		addFileToClasspath(buf, getLog4jJar());
		addFileToClasspath(buf, getDom4jJar());
	}
	public static File getJUnitJar() {
		return getCodeBase(TestCase.class);
	}
	public static File getBuildServerJar() {
		return getCodeBase(TestRunner.class);
	}
	public static File getCloverJar() {
		return Clover.getCloverJar();
	}

	public static File getLog4jJar() {
		return getCodeBase(org.apache.log4j.Logger.class);
	}
	public static File getDom4jJar() {
		return getCodeBase(DocumentException.class);
	}
	public static File getSubmitServerModelClasses() {
		return getCodeBase(TestOutcome.class);
	}

	public static File getCodeBase(Class<?> c) {
		ClassLoader cl = c.getClassLoader();
		String classFileName = c.getName().replace('.', '/') + ".class";
		URL u = cl.getResource(classFileName);
		File f = null;
		try {
			String path = u.toString();
			if (path.startsWith("jar:")) {
				path = u.getPath();
				int i = path.lastIndexOf("!/" + classFileName);
				if (i >= 0)
					return new File(new URL(path.substring(0, i)).toURI());
			} else {
				int i = path.lastIndexOf("/" + classFileName);
				if (i >= 0)
					return new File(new URL(path.substring(0, i)).toURI());
			}
		} catch (MalformedURLException e) {
			assert true;

		} catch (URISyntaxException e) {
			assert true;
		}
		return null;
	}

}

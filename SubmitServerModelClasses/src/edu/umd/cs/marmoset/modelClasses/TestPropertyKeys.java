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

package edu.umd.cs.marmoset.modelClasses;


/**
 * Keys for test properties.
 *
 * @see TestProperties
 * @author David Hovemeyer
 */
public interface TestPropertyKeys {
	/**
	 * Default value for process timeout.
	 */
	public static final int DEFAULT_PROCESS_TIMEOUT = 60;

	/**
	 * Default value for max process drain bytes.
	 */
	public static final int DEFAULT_MAX_DRAIN_OUTPUT_IN_BYTES = 1024*1024;

	/**
	 * Default Java source version.
	 */
	public static final String DEFAULT_JAVA_SOURCE_VERSION = "1.6";

	/**
	 * Default make utility.
	 */
	public static final String DEFAULT_MAKE_COMMAND = "/usr/bin/make";

	/**
	 * Default Makefile name.
	 */
	public static final String DEFAULT_MAKEFILE_FILENAME = "Makefile";

	/**
	 * Prefix for test properties specifying names of public, release, secret,
	 * tests.
	 */
	public static final String TESTCLASS_PREFIX = "test.class.";
	public static final String TESTCASES_PREFIX = "test.cases.";


   
	/**
	 * Property names to specify individual test timeout.
	 */
	public static final String[] TEST_TIMEOUT =
			{"test.timeout.testCase", "test.timeout", "test.timeout.testProcess", "timeout"};

    public static final String[] BUILD_TIMEOUT =
            {"build.timeout"};

	/**
	 * Property names to specify max number of bytes to be read from
	 * a test process.
	 */
	public static final String[] MAX_DRAIN_OUTPUT_IN_BYTES =
			{"test.output.maxBytes", "max.output.bytes"};

	/**
	 * Property name to specify a source version.
	 */
	public static final String[] SOURCE_VERSION =
			{"build.sourceVersion", "java_source_version"};

	/**
	 * Property name to specify that the test process(es) should
	 * be run in the testfiles directory.
	 */
	public static final String[] RUN_IN_TESTFILES_DIR =
			{"test.runInInstructorDir", "cwd.testfiles.dir"};

	/**
	 * Path of make utility.
	 */
	public static final String MAKE_COMMAND = "build.make.command";

	/**
	 * Filename of the makefile.
	 */
	public static final String MAKE_FILENAME = "build.make.file";

    /**
     * Filename of the student-written makefile (if any).
     */
    public static final String STUDENT_MAKE_FILENAME = "build.student.make.file";

    public static final String ADDITIONAL_SOURCE_FILE_EXTENSIONS = "display.source.extensions";
	/**
	 * Project language.
	 */
	public static final String [] BUILD_FRAMEWORK = new String[] { "build.language",
	    "build.framework", "test.framework",
	    "test.language"};

	/**
	 * Property name to specify whether we want to perform code coverage.
	 */
	public static final String PERFORM_CODE_COVERAGE = "test.performCodeCoverage";

    /**
     * LD_LIBRARY_PATH
     */
    public static final String LD_LIBRARY_PATH = "LD_LIBRARY_PATH";

    /**
     * Key for finding the VM_ARGS.
     */
    public static final String VM_ARGS = "test.vmArgs";

    public static final String JAVA="java";
    public static final String C="c";
    public static final String RUBY="ruby";
    public static final String OCAML="ocaml";

}

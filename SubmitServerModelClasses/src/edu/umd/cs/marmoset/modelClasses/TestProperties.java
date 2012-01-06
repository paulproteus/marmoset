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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import edu.umd.cs.marmoset.utilities.FileNames;

/**
 * Test properties: loaded from the test.properties file in
 * the project jarfile.
 *
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class TestProperties implements TestPropertyKeys {
	private Properties testProperties;

    // Shared
    private String language;
    private int testTimeoutInSeconds;
    private int maxDrainOutputInBytes;
    private String ldLibraryPath;
    private String additionalSourceFileExtensions;

    public String getAdditionalSourceFileExtensions() {
		return additionalSourceFileExtensions;
	}

	public void setAdditionalSourceFileExtensions(
			String additionalSourceFileExtensions) {
		this.additionalSourceFileExtensions = additionalSourceFileExtensions;
	}

	// Java-only
    private boolean performCodeCoverage;
    private String javaSourceVersion;
    private boolean testRunnerInTestfileDir;
    private String vmArgs;

    // Makefile-based only
    private String makeCommand;
    private String makefileName;
    private String studentMakefileName;

	/**
	 * Constructor.
	 * Creates empty test properties.
	 */
	public TestProperties() {
		this.testProperties = new Properties();
	}

	/**
	 * Load test properties from a file.
	 *
	 * @param fileName name of the test properties file
	 * @throws FileNotFoundException
	 * @throws IOException
     * @throws MissingRequiredTestPropertyException
	 */
	public void load(String fileName)
    throws FileNotFoundException, IOException, MissingRequiredTestPropertyException
    {
		initializeTestProperties(new BufferedInputStream(new FileInputStream(fileName)));
	}

    public void load(ZipInputStream zipIn)
    throws IOException, MissingRequiredTestPropertyException
    {
        while (true) {
            ZipEntry entry=zipIn.getNextEntry();
            if (entry==null) break;
            if (entry.isDirectory())
                continue;
            if (!entry.getName().endsWith("test.properties"))
                continue;
            initializeTestProperties(zipIn);
        }
    }

	/**
	 * Load test properties from a file.
	 *
	 * @param file file storing test properties
	 * @throws IOException
	 * @throws FileNotFoundException
     * @throws MissingRequiredTestPropertyException
	 */
	public void load(File file)
    throws FileNotFoundException, IOException, MissingRequiredTestPropertyException
    {
	    initializeTestProperties(new BufferedInputStream(new FileInputStream(file)));
	}

    /**
     * Initialize the test properties, setting any mandatory test properties.
     * Throws an exception in mandatory test properties are missing.
     *
     * @param is The input stream storing the test properties.
     * @throws IOException
     * @throws MissingRequiredTestPropertyException If any mandatory test properties are missing.
     */
    private void initializeTestProperties(InputStream is)
    throws IOException, MissingRequiredTestPropertyException
    {
        testProperties.load(is);

        // TODO This method should perform validation, i.e. make sure that properties
        // that are required for Java are set when they should be set, etc.  We
        // should fail as soon as we have a test.properties file that doesn't make sense
        // (at load time) rather than waiting until we try to load a property that is broken
        setLanguage(getRequiredStringProperty(BUILD_LANGUAGE));
        setPerformCodeCoverage(getOptionalBooleanProperty(PERFORM_CODE_COVERAGE, false));
        setMaxDrainOutputInBytes(getOptionalIntegerProperty(MAX_DRAIN_OUTPUT_IN_BYTES, DEFAULT_MAX_DRAIN_OUTPUT_IN_BYTES));
        setJavaSourceVersion(getOptionalStringProperty(SOURCE_VERSION, DEFAULT_JAVA_SOURCE_VERSION));
        setTestRunnerInTestfileDir(getOptionalBooleanProperty(RUN_IN_TESTFILES_DIR, false));
        setLdLibraryPath(getOptionalStringProperty(LD_LIBRARY_PATH));
        setVmArgs(getOptionalStringProperty(VM_ARGS));

        setAdditionalSourceFileExtensions(getOptionalStringProperty(ADDITIONAL_SOURCE_FILE_EXTENSIONS));

        setMakeCommand(getOptionalStringProperty(MAKE_COMMAND, DEFAULT_MAKE_COMMAND));
        setMakefileName(getOptionalStringProperty(MAKE_FILENAME));
        setStudentMakefileName(getOptionalStringProperty(STUDENT_MAKE_FILENAME));

        // If no individual test timeout is specified, then use the default.
        // Note that we ignore test.timeout.testProcess since we're not timing out
        // the entire process anymore.
        setTestTimeoutInSeconds(getOptionalIntegerProperty(TEST_TIMEOUT, DEFAULT_PROCESS_TIMEOUT));
    }

	/**
	 * Set a test property value.
	 *
	 * @param name  property name
	 * @param value property value
	 */
	public void setProperty(String name, String value) {
		if (value!=null)
		    testProperties.setProperty(name, value);
	}

    public boolean isJava() {
        return getLanguage().equalsIgnoreCase(JAVA);
    }

    public boolean isMakefileBased() {
        return getLanguage().equalsIgnoreCase(C) ||
            getLanguage().equalsIgnoreCase(RUBY) ||
            getLanguage().equalsIgnoreCase(OCAML);
    }

	/**
	 * Get the project source language.
	 *
	 * @return the project source language: "java", "c", etc.
	 */
	public String getLanguage() {
		if (language == null)
		    throw new IllegalStateException("Should be impossible; missing build.language in test.properties!");
		return language.toLowerCase(Locale.US);
	}

    public void setLanguage(String language) {
        this.language = language.toLowerCase(Locale.US);
    }

	/**
	 * Get the "test class" for the given test type.
	 * The meaning of the test class depends on what kind
	 * of project is being tested (Java, C, etc.)
	 *
	 * @param testType the type of test (TestOutcome.PUBLIC, TestOutcome.RELEASE, etc.)
	 * @return the test class for the test type, or null if no test class
	 *         is defined for the test type
	 */
	public String getTestClass(String testType) {
		return  getOptionalStringProperty(TestPropertyKeys.TESTCLASS_PREFIX + testType);

	}

	/**
	 * Get an integer property from the loaded test properties.
	 *
	 * @param property     the property name
	 * @param defaultValue default value
	 * @return the property value
	 */
	private int getOptionalIntegerProperty(String property, int defaultValue) {
		try {
			String value = testProperties.getProperty(property);
			if (value == null)
				return defaultValue;
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Get an integer property from the loaded test properties.
	 *
	 * @param propertyNameList list of property names (this assumes all of the names
	 *                         mean the same thing)
	 * @param defaultValue     the default value
	 * @return the property value
	 */
	private int getOptionalIntegerProperty(String[] propertyNameList, int defaultValue) {
		try {
			String value = getOptionalStringProperty(propertyNameList);
			if (value == null)
				return defaultValue;
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}

	}

	/**
	 * Get a boolean property from the loaded test properties.
	 *
	 * @param property     name of the property
	 * @param defaultValue value to return if property is not defined
	 * @return boolean value of the property
	 */
	private boolean getOptionalBooleanProperty(String property, boolean defaultValue) {
		String value = getOptionalStringProperty(property);
		if (value != null)
            return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes");
        return defaultValue;
	}

	/**
	 * Get a boolean property from the loaded test properties.
	 *
	 * @param propertyNameList list of property names (this assumes all of the names
	 *                         mean the same thing)
	 * @param defaultValue     value to return if property is not defined
	 * @return boolean value of the property
	 */
	private boolean getOptionalBooleanProperty(String[] propertyNameList, boolean defaultValue) {
		String value = getOptionalStringProperty(propertyNameList);
        if (value != null)
            return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes");
        return defaultValue;
	}

	/**
	 * Get a required string-valued property from the loaded test properties.
	 *
	 * @param property the property name
	 * @return the value of the property
	 */
	private String getRequiredStringProperty(String property) throws MissingRequiredTestPropertyException{
		String value = testProperties.getProperty(property);
		if (value == null)
			throw new MissingRequiredTestPropertyException("test.properties is missing required property " + property);
		return value;
	}

	/**
	 * Get a required string-valued property from the loaded test properties.
	 *
	 * @param propertyNameList list of property names (this assumes all of the names
	 *                         mean the same thing)
	 * @return the value of the property
	 */
	private String getRequiredStringProperty(String[] propertyNameList) throws MissingRequiredTestPropertyException{
		if (propertyNameList.length == 0)
			throw new IllegalArgumentException("empty property name list");
		String value = getOptionalStringProperty(propertyNameList);
		if (value == null)
			throw new MissingRequiredTestPropertyException("test.properties is missing required property " + propertyNameList[0]);
		return value;
	}

	/**
	 * Get an optional string-valued property from the loaded
	 * test propertes.
	 *
	 * @param property the property name
	 * @return the property value, or null if the property was not defined
	 */
	public String getOptionalStringProperty(String property) {
		return testProperties.getProperty(property);
	}

    private String getOptionalStringProperty(String property, String defaultValue) {
        String result=getOptionalStringProperty(property);
        if (result!=null)
            return result;
        return defaultValue;
    }

	/**
	 * Get an optional string-valued property from the loaded
	 * test propertes.
	 *
	 * @param propertyNameList list of property names (this assumes all of the names
	 *                         mean the same thing)
	 * @return the property value, or null if the property was not defined
	 */
	private String getOptionalStringProperty(String[] propertyNameList) {
		for (int i = 0; i < propertyNameList.length; ++i) {
			String value = getOptionalStringProperty(propertyNameList[i]);
			if (value != null)
				return value;
		}
		return null;
	}
    private String getOptionalStringProperty(String[] propertyNameList, String defaultValue) {
        String result=getOptionalStringProperty(propertyNameList);
        if (result!=null)
            return result;
        return defaultValue;
    }


    public boolean isPerformCodeCoverage() {
        return performCodeCoverage;
    }
    public void setPerformCodeCoverage(boolean performCodeCoverage) {
        this.performCodeCoverage = performCodeCoverage;
        setProperty(PERFORM_CODE_COVERAGE, Boolean.toString(this.performCodeCoverage));
    }
    public String getStudentMakeFile() {
        return getOptionalStringProperty(STUDENT_MAKE_FILENAME);
    }
    public int getTestTimeoutInSeconds() {
        return testTimeoutInSeconds;
    }
    public void setTestTimeoutInSeconds(int testTimeout) {
        this.testTimeoutInSeconds=testTimeout;
        setProperty(TEST_TIMEOUT[0], Integer.toString(this.testTimeoutInSeconds));
    }
    /**
     * @return Returns the maxDrain.
     */
    public int getMaxDrainOutputInBytes() {
        return maxDrainOutputInBytes;
    }
    /**
     * @param maxDrain The maxDrain to set.
     */
    public void setMaxDrainOutputInBytes(int maxDrainOutputInBytes) {
        this.maxDrainOutputInBytes = maxDrainOutputInBytes;
        setProperty(MAX_DRAIN_OUTPUT_IN_BYTES[0], Integer.toString(this.maxDrainOutputInBytes));
    }
    /**
     * @return Returns the javaSourceVersion.
     */
    public String getJavaSourceVersion() {
        return javaSourceVersion;
    }
    /**
     * @param javaSourceVersion The javaSourceVersion to set.
     */
    public void setJavaSourceVersion(String javaSourceVersion) {
        this.javaSourceVersion = javaSourceVersion;
        setProperty(SOURCE_VERSION[0], this.javaSourceVersion);
    }

    /**
     * @return Returns the testRunnerInTestfileDir.
     */
    public boolean isTestRunnerInTestfileDir()
    {
        return testRunnerInTestfileDir;
    }

    /**
     * @param testRunnerInTestfileDir The testRunnerInTestfileDir to set.
     */
    public void setTestRunnerInTestfileDir(boolean testRunnerInTestfileDir)
    {
        this.testRunnerInTestfileDir = testRunnerInTestfileDir;
        setProperty(RUN_IN_TESTFILES_DIR[0], Boolean.toString(this.testRunnerInTestfileDir));
    }

    public String getLdLibraryPath() {
        return ldLibraryPath;
    }
    /**
     * @param ldLibraryPath The ldLibraryPath to set.
     */
    public void setLdLibraryPath(String ldLibraryPath)
    {
        this.ldLibraryPath = ldLibraryPath;
        setProperty(LD_LIBRARY_PATH, this.ldLibraryPath);
    }

    /**
     * @return Returns the vmArgs.
     */
    public String getVmArgs()
    {
        return vmArgs;
    }

    /**
     * @param vmArgs The vmArgs to set.
     */
    public void setVmArgs(String vmArgs)
    {
        this.vmArgs = vmArgs;
        setProperty(VM_ARGS, this.vmArgs);
    }

    public String getMakeCommand()
    {
        return makeCommand;
    }

    /**
     * @param makeCommand The makeCommand to set.
     */
    public void setMakeCommand(String makeCommand)
    {
        this.makeCommand = makeCommand;
        setProperty(MAKE_COMMAND, this.makeCommand);
    }

    /**
     * @return Returns the makefileName.
     */
    public String getMakefileName()
    {
        return makefileName;
    }

    /**
     * @param makefileName The makefileName to set.
     */
    public void setMakefileName(String makefileName)
    {
        this.makefileName = makefileName;
        setProperty(MAKE_FILENAME, this.makefileName);
    }

    /**
     * @return Returns the studentMakefileName.
     */
    public String getStudentMakefileName()
    {
        return studentMakefileName;
    }

    /**
     * @param studentMakefileName The studentMakefileName to set.
     */
    public void setStudentMakefileName(String studentMakefileName)
    {
        this.studentMakefileName = studentMakefileName;
        setProperty(STUDENT_MAKE_FILENAME, this.studentMakefileName);
    }

    public boolean isSourceFile(String name) {
    		String simpleName = FileNames.trimSourceFileName(name);
    		if (name.equals(studentMakefileName) ||
    				name.equals(makefileName))
    			return true;
    		int lastDot = simpleName.lastIndexOf('.');
		if (lastDot > 0 && additionalSourceFileExtensions != null) {
			String suffix = simpleName.substring(lastDot+1);
			String allowed [] = additionalSourceFileExtensions.split(" ,");
			if (Arrays.asList(allowed).contains(suffix))
				return true;
		}
		return false;
    }
}

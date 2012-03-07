package edu.umd.cs.marmoset.modelClasses;

import static edu.umd.cs.marmoset.modelClasses.TestPropertyKeys.DEFAULT_JAVA_SOURCE_VERSION;
import static edu.umd.cs.marmoset.modelClasses.TestPropertyKeys.DEFAULT_MAX_DRAIN_OUTPUT_IN_BYTES;
import static edu.umd.cs.marmoset.modelClasses.TestPropertyKeys.LD_LIBRARY_PATH;
import static edu.umd.cs.marmoset.modelClasses.TestPropertyKeys.MAX_DRAIN_OUTPUT_IN_BYTES;
import static edu.umd.cs.marmoset.modelClasses.TestPropertyKeys.PERFORM_CODE_COVERAGE;
import static edu.umd.cs.marmoset.modelClasses.TestPropertyKeys.RUN_IN_TESTFILES_DIR;
import static edu.umd.cs.marmoset.modelClasses.TestPropertyKeys.SOURCE_VERSION;
import static edu.umd.cs.marmoset.modelClasses.TestPropertyKeys.VM_ARGS;

import java.util.Properties;

import edu.umd.cs.marmoset.modelClasses.TestOutcome.TestType;

public class JUnitTestProperties extends TestProperties {
    
    // Java-only
    private boolean performCodeCoverage;
    private String javaSourceVersion;
    private boolean testRunnerInTestfileDir;
    private String vmArgs;

    public JUnitTestProperties(Properties testProperties) {
        super(Framework.JUNIT, testProperties);
        setPerformCodeCoverage(getOptionalBooleanProperty(PERFORM_CODE_COVERAGE, false));
        setJavaSourceVersion(getOptionalStringProperty(SOURCE_VERSION, DEFAULT_JAVA_SOURCE_VERSION));
        setTestRunnerInTestfileDir(getOptionalBooleanProperty(RUN_IN_TESTFILES_DIR, true));
        setVmArgs(getOptionalStringProperty(VM_ARGS));
        
        setMaxDrainOutputInBytes(getOptionalIntegerProperty(MAX_DRAIN_OUTPUT_IN_BYTES, DEFAULT_MAX_DRAIN_OUTPUT_IN_BYTES));
        setLdLibraryPath(getOptionalStringProperty(LD_LIBRARY_PATH));
        

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
    public String getTestClass(TestType testType) {
        return  getOptionalStringProperty(TestPropertyKeys.TESTCLASS_PREFIX + testType);

    }
    
    @Override
    public boolean isPerformCodeCoverage() {
        return performCodeCoverage;
    }
    private void setPerformCodeCoverage(boolean performCodeCoverage) {
        this.performCodeCoverage = performCodeCoverage;
        setProperty(PERFORM_CODE_COVERAGE, Boolean.toString(this.performCodeCoverage));
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

}

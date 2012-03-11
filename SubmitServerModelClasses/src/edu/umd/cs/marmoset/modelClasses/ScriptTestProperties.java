package edu.umd.cs.marmoset.modelClasses;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import edu.umd.cs.marmoset.modelClasses.TestOutcome.TestType;

public class ScriptTestProperties extends TestProperties {
   
    
    final Map<String, ExecutableTestCase> testCases;
    public ScriptTestProperties(Properties testProperties) {
        this(Framework.SCRIPT, testProperties);
    }
    protected ScriptTestProperties(Framework framework, Properties testProperties) {
        super(framework, testProperties);
        testCases = ExecutableTestCase.parse(this);
    }
    public Collection<ExecutableTestCase> getExecutableTestCases() {
        return testCases.values();
    }

    public Iterable<String> getTestNames(TestType testType) {
       String names =  getOptionalStringProperty(TestPropertyKeys.TESTCASES_PREFIX + testType.toString());
       if (names == null)
           names =  getOptionalStringProperty(TestPropertyKeys.TESTCLASS_PREFIX + testType.toString());
       if (names == null)
           return null;
       return Arrays.asList(names.split("[,\\s]+"));
           
    }
   
}

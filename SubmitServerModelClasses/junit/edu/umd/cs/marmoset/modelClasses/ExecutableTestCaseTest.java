package edu.umd.cs.marmoset.modelClasses;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;
import edu.umd.cs.marmoset.modelClasses.ExecutableTestCase.InputKind;
import edu.umd.cs.marmoset.modelClasses.ExecutableTestCase.OutputKind;
import edu.umd.cs.marmoset.modelClasses.ExecutableTestCase.Property;
import edu.umd.cs.marmoset.modelClasses.TestOutcome.TestType;

public class ExecutableTestCaseTest extends TestCase {
    
    
    
    public void testSimpleScriptProperties() throws IOException {
        String testProps = "simpleTest.properties";
        Map<String, ExecutableTestCase> cases =  getTestProperties(testProps);
        ExecutableTestCase tt = cases.get("tt");
        check(tt, TestType.PUBLIC, InputKind.STRING,  "True True", OutputKind.STRING, "True",
                "python main.py");
    }


    public void testComplicatedScriptProperties() throws IOException {
        String testProps = "complicatedTest.properties";
        testComplicatedScriptProperties(testProps);
    }
      
    public void testComplicatedScriptPropertiesImplicitDefault() throws IOException {
        String testProps = "complicatedTestImplicitDefault.properties";
        testComplicatedScriptProperties(testProps);
    }
        
    public void testComplicatedScriptProperties(String testProps) throws IOException {
        Map<String, ExecutableTestCase> cases =  getTestProperties(testProps);
        ExecutableTestCase p1 = cases.get("p1");
        check(p1, TestType.PUBLIC, InputKind.FILE, "p1.in", OutputKind.FILE, "p1.out",
                "python p1.py");
        ExecutableTestCase p2 = cases.get("p2");
        check(p2, TestType.PUBLIC, InputKind.FILE, "p2.in", OutputKind.FILE, "p2.out",
                "python p2.py");
        ExecutableTestCase r1 = cases.get("r1");
        check(r1, TestType.RELEASE, InputKind.FILE, "r1.in",
                OutputKind.REFERENCE_IMPL,  null, "python2.7 r1.py", "python2.7 r1-instructor.py");
        ExecutableTestCase r2 = cases.get("r2");
        check(r1, TestType.RELEASE, InputKind.STRING, "input for r2",
                OutputKind.REFERENCE_IMPL,  null, "python2.7 r2.py", "python2.7 r2-instructor.py");
    }

  private   Map<String, ExecutableTestCase> getTestProperties(String testProps) throws IOException {
        Properties props = new Properties();
         props.load(this.getClass().getResourceAsStream(testProps));
        ScriptTestProperties scriptProps = new ScriptTestProperties(props);
        return ExecutableTestCase.parse(scriptProps);
    }
  
  private void check(ExecutableTestCase testCase,
          TestType testType,
          InputKind inputKind, 
          String input,
          OutputKind outputKind,
          String output,
          String exec) {
      check(testCase, testType, inputKind, input, outputKind, output, exec, null);
      
  }

  private void check(ExecutableTestCase testCase,
          TestType testType,
          InputKind inputKind, 
          String input,
          OutputKind outputKind,
          String output, String execSource, String  referenceExecSource) {
      assertEquals(inputKind, testCase.getInputKind());
      if (inputKind.hasValue())
          assertEquals(input, testCase.getProperty(Property.INPUT));
      assertEquals(outputKind, testCase.getOutputKind());
      if (outputKind.hasValue())
          assertEquals(output, testCase.getProperty(Property.EXPECTED));
      assertEquals(testType, testCase.getTestType());
      
      String [] exec = execSource.split("\\s+");
      String[] actualExec = testCase.getStrings(ExecutableTestCase.Property.EXEC);
      
      if (!Arrays.equals(exec, actualExec))
          throw new AssertionError("Expected " + Arrays.toString(exec) 
                  +", actual " + Arrays.toString(actualExec));
      if (outputKind == OutputKind.REFERENCE_IMPL) {
          String [] referenceExec = referenceExecSource.split("\\s+");
          String[] actualReferenceExec = testCase.getStrings(ExecutableTestCase.Property.REFERENCE_EXEC);
          if (!Arrays.equals(referenceExec, actualReferenceExec))
              throw new AssertionError("Expected " + Arrays.toString(referenceExec) 
                      +", actual " + Arrays.toString(actualReferenceExec));
      
      }
      
  }
 

}

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

package edu.umd.cs.marmoset.utilities;

import junit.framework.TestCase;

public class MarmosetUtlitiesTest extends TestCase
{

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(MarmosetUtlitiesTest.class);
    }
    
    private void compareStackTraces(StackTraceElement t1, StackTraceElement t2)
    {
        assertEquals(t1.getClassName(), t2.getClassName());
        assertEquals(t1.getMethodName(), t2.getMethodName());
        assertEquals(t1.getFileName(), t2.getFileName());
        assertEquals(t1.getLineNumber(), t2.getLineNumber());
    }
    
    private void createAndParseStackTrace(String declaringClass, String methodName, String fileName, int lineNumber)
    {
        StackTraceElement t=new StackTraceElement(declaringClass,methodName,fileName,lineNumber);
        System.out.println(t.toString());
        StackTraceElement trace=MarmosetUtilities.parseStackTrace(t.toString());
        compareStackTraces(t, trace);
    }
    
    public void testParseNormalStackTrace()
    throws Exception
    {
        createAndParseStackTrace("utilities.Utilities","doDijkstra", "Utilities.java",146);
    }
    public void testParseStackTraceWithUnknownSource()
    throws Exception
    {
        createAndParseStackTrace("java.security.AccessControlContext","checkPermission",null,-1);
    }
    public void testParseStackTraceForConstructors()
    throws Exception
    {
        createAndParseStackTrace("cs132.p1.Sudoku","<init>","Sudoku.java",51);
    }

}

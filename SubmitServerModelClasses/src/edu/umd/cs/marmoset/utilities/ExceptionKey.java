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

/**
 * @author jspacco
 */
package edu.umd.cs.marmoset.utilities;


public class ExceptionKey implements Comparable<ExceptionKey>
{
    /**
     * Returns true if the two exceptionKeys are in the same class and method.
     *  Essentially, this ignores the line numbers.
     * @param o The other ExceptionKey.
     * @return True if the two exceptionKeys are the same class and method; false otherwise.
     */
    @Override
	public int compareTo(ExceptionKey o)
    {
        ExceptionKey other=o;
        // If the filenames are different, then these are different objects
        int fileNameComparison=stackTraceElement.getFileName().compareTo(other.stackTraceElement.getFileName());
        if (fileNameComparison!=0)
            return fileNameComparison;
        // If the classnames are different, then these are different objects
        int classComparison=stackTraceElement.getClassName().compareTo(other.stackTraceElement.getClassName());
        if (classComparison!=0)
            return classComparison;
        // Otherwise, just check that the method names are the same; i.e. this will
        // ignore the lineNumbers
        return stackTraceElement.getMethodName().compareTo(other.stackTraceElement.getMethodName());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof ExceptionKey && compareTo((ExceptionKey)obj)==0;
    }
    
    @Override
    public int hashCode()
    {
        return MarmosetUtilities.hashString(stackTraceElement.getFileName() +
            stackTraceElement.getClassName() +
            stackTraceElement.getMethodName());
    }
    
    private StackTraceElement stackTraceElement;
    public ExceptionKey(String s) {
        stackTraceElement=MarmosetUtilities.parseStackTrace(s);
    }
    public ExceptionKey(StackTraceElement stackTraceElement) {
        this.stackTraceElement=stackTraceElement;
    }
    
    @Override
	public String toString() {
        return stackTraceElement.toString();
    }
    public StackTraceElement getStackTraceElement() {
        return stackTraceElement;
    }
}

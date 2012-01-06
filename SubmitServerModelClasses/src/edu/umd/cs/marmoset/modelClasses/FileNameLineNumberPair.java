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


public class FileNameLineNumberPair
{
    private final String fileName;
    private final int lineNumber;
    
    public FileNameLineNumberPair(String fileName, int lineNumber) {
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }
    public FileNameLineNumberPair(String fileName, String lineNumber) {
        this(fileName, Integer.parseInt(lineNumber));
    }
    public FileNameLineNumberPair(String fileName) {
        this(fileName, -1);
    }
    public String getFileName() {
        return fileName;
    }
    public int getLineNumber() {
        return lineNumber;
    }
    
    public final static FileNameLineNumberPair EMPTY = new FileNameLineNumberPair("NO FILE", -1);
}

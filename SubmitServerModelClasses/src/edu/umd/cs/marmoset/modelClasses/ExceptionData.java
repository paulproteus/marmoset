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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ExceptionData implements Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    public static byte[] toBytes(ExceptionData ed) 
    throws IOException
    {
    	ObjectOutputStream out=null;
    	try {
            if (ed == null) return null;
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bytes);
            out.writeObject(ed);
            out.close();
            return bytes.toByteArray();
        } finally {
        	if (out!=null) out.close();
        }
    }

    public static ExceptionData fromInputStream(InputStream is)
    throws ClassNotFoundException, IOException
    {
    	ObjectInputStream ois=null;
    	try {
            ois = new ObjectInputStream(is);
            ExceptionData result = (ExceptionData) ois.readObject();
            return result;
        } finally {
        	if (ois!=null) ois.close();
        }
    }
    
    public static byte[] toBytes(Throwable e)
    throws IOException
    {
        return toBytes(new ExceptionData(e));
    }

    public static ExceptionData fromBytes(byte[] bytes)
    throws IOException, ClassNotFoundException
    {
    	ObjectInputStream in=null;
    	try {
            if (bytes == null) return null;
            in = new ObjectInputStream(new ByteArrayInputStream(bytes));
            ExceptionData result = (ExceptionData) in.readObject();
            in.close();
            return result;
        } finally {
        	if (in!=null) in.close();
        }
    }

    final String exceptionClassName;

    final String exceptionMessage;

    final ExceptionData cause;

    final StackTraceElement[] stackTrace;

    public ExceptionData(Throwable e) {
        exceptionClassName = e.getClass().getName();
        exceptionMessage = e.getMessage();
        stackTrace = e.getStackTrace();
        if (e.getCause() == null)
            cause = null;
        else
            cause = new ExceptionData(e.getCause());
    }

    public String getClassName() {
        return exceptionClassName;
    }

    public String getMessage() {
        return exceptionMessage;
    }

    public ExceptionData getCause() {
        return cause;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }
}

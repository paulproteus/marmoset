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
 * Created on Aug 31, 2004
 */
package edu.umd.cs.buildServer;

/**
 * Exception thrown by the Builder if a project fails to compile.
 * 
 * @author David Hovemeyer
 */
public class CompileFailureException extends Exception {
	private static final long serialVersionUID = 3905804180017788472L;

	private String compilerOutput;

	/**
	 * Constructor.
	 * 
	 * @param msg
	 *            the failure message
	 */
	public CompileFailureException(String msg, String compilerOutput) {
		super(msg);
		this.compilerOutput = compilerOutput;
	}

	/**
	 * Constructor.
	 * 
	 * @param msg
	 *            the failure message
	 * @param t
	 *            the cause
	 */
	public CompileFailureException(String msg, Throwable t,
			String compilerOutput) {
		super(msg, t);
		this.compilerOutput = compilerOutput;
	}

	/**
	 * Get the compiler output.
	 * 
	 * @return the compiler output
	 */
	public String getCompilerOutput() {
		return compilerOutput;
	}
}

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
 * Exception thrown by a Builder if something other than a compiler error goes
 * wrong trying to build a project. Compiler errors are thrown as
 * CompileFailureExceptions.
 * 
 * <p>
 * Basically, a BuilderException is an internal error in the build server.
 * 
 * @author David Hovemeyer
 */
public class BuilderException extends Exception {
	private static final long serialVersionUID = 3691037664686323504L;

	/**
	 * Constructor
	 * 
	 * @param msg
	 *            message explaining the reason for failure
	 */
	public BuilderException(String msg) {
		super(msg);
	}

	/**
	 * Constructor
	 * 
	 * @param msg
	 *            message explaining the reason for failure
	 * @param reason
	 *            a Throwable conveying the reason for failure
	 */
	public BuilderException(String msg, Throwable reason) {
		super(msg, reason);
	}

	/**
	 * Constructor
	 * 
	 * @param e
	 *            An exception explaining the reason for failure.
	 */
	public BuilderException(Exception e) {
		super(e);
	}
}

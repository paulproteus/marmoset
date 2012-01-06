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
 * Created on Sep 4, 2004
 */
package edu.umd.cs.buildServer;

/**
 * Exception throw to indicate that the build server is missing a required
 * configuration property.
 * 
 * @author David Hovemeyer
 */
public class MissingConfigurationPropertyException extends Exception {
	private static final long serialVersionUID = 3256726195092666675L;

	/**
	 * Constructor.
	 * 
	 * @param msg
	 *            the message describing the exception
	 */
	public MissingConfigurationPropertyException(String msg) {
		super(msg);
	}
}

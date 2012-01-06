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
 * Created on Aug 23, 2004
 *
 */
package edu.umd.cs.submitServer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * @author jspacco
 * 
 */
public class ClientRequestException extends ServletException {

	protected int errorCode;

	public int getErrorCode() {
		return errorCode;
	}

	public ClientRequestException(int code, String message) {
		super(message);
		errorCode = code;
	}

	/**
	 * 
	 */
	public ClientRequestException() {
		super();
	}

	/**
	 * @param message
	 */
	public ClientRequestException(String message) {
		this(HttpServletResponse.SC_BAD_REQUEST, message);
	}

	/**
	 * @param cause
	 */
	public ClientRequestException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ClientRequestException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}

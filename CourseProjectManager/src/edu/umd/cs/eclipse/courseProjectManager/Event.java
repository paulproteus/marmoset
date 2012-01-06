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
 * Created on Jan 23, 2004
 */
package edu.umd.cs.eclipse.courseProjectManager;

import java.util.Date;

/**
 * An AutoCVS event to be logged.
 * 
 * @author David Hovemeyer
 */
public class Event {
	private Date date;
	private String message;
	private Throwable exception;
	private boolean isError;

	public Event(String message) {
		this(message, null);
	}

	public Event(String message, Throwable exception) {
		this.date = new Date();
		this.message = message;
		this.exception = exception;
	}

	public void setIsError(boolean isError) {
		this.isError = isError;
	}

	public boolean isError() {
		return isError;
	}

	public Date getDate() {
		return date;
	}

	public String getMessage() {
		return message;
	}

	public Throwable getException() {
		return exception;
	}
}

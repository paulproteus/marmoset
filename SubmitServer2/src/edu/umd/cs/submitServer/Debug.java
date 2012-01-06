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
 * Created on Jan 9, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer;

import java.io.IOException;
import java.io.PrintStream;

/**
 * @author jspacco
 * 
 */
public class Debug {
	public static final PrintStream err = System.err;
	public static final PrintStream out = System.out;

	public static final void println(String s) {
		err.println(s);
	}

	public static final void error(String s) {
		err.println(s);
	}

	public static final void warn(String s) {
		err.println(s);
	}

	/**
	 * @param string
	 * @param e
	 */
	public static void exception(String string, IOException e) {
		err.println(string + " " + e.toString());
	}

}

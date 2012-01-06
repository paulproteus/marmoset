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


public class FileNames {

	public static String trimSourceFileName(String entryName) {
		int lastSlash = getLastSlash(entryName);
		if (lastSlash >= 0) {
			entryName = entryName.substring(lastSlash + 1);
		}
		return entryName;
	}

	public static int getLastSlash(String entryName) {
		int forwardSlashPos = entryName.lastIndexOf('/');
		int backwardSlashPos = entryName.lastIndexOf('\\');
		return Math.max(forwardSlashPos, backwardSlashPos);
	}

}

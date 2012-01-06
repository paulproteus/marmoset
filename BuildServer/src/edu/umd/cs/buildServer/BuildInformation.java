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
 * Created on Mar 4, 2005
 */
package edu.umd.cs.buildServer;

/**
 * Extra information about the code being built. Currently we collect MD5SUMs
 * for Java code and do nothing for C code.
 * 
 * @author jspacco
 */
public class BuildInformation {
	private String md5sumClassfiles;
	private String md5sumSourcefiles;

	public BuildInformation() {
	}

	/**
	 * @return Returns the md5sum.
	 */
	public String getMd5sumClassfiles() {
		return md5sumClassfiles;
	}

	/**
	 * @return Returns the sourcefilesMd5sum.
	 */
	public String getMd5sumSourcefiles() {
		return md5sumSourcefiles;
	}

	/**
	 * @param sourcefilesMd5sum
	 *            The sourcefilesMd5sum to set.
	 */
	public void setMd5sumSourcefiles(String sourcefilesMd5sum) {
		this.md5sumSourcefiles = sourcefilesMd5sum;
	}

	/**
	 * @param classfilesMd5sum
	 *            The classfilesMd5sum to set.
	 */
	public void setMd5sumClassfiles(String classfilesMd5sum) {
		this.md5sumClassfiles = classfilesMd5sum;
	}
}

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

package edu.umd.cs.marmoset.codeCoverage;

public class Method extends Line<Method> {
	public Method(int lineNumber, int count) {
		super(lineNumber, count);
	}

	@Override
	public Method union(Method other) {
		if (!(getLineNumber() == other.getLineNumber()))
			throw new IllegalStateException("Trying to merge different line numbers: "
					+ getLineNumber() + " and " + other.getLineNumber());
		return new Method(getLineNumber(), getCount() + other.getCount());
	}

	@Override
	public Method intersect(Method other) {
		if (!(getLineNumber() == other.getLineNumber()))
			throw new IllegalStateException("Trying to merge different line numbers: "
					+ getLineNumber() + " and " + other.getLineNumber());
		return new Method(getLineNumber(), Math.min(getCount(), other.getCount()));
	}

	@Override
	public Method excluding(Method other) {
		if (!(getLineNumber() == other.getLineNumber()))
			throw new IllegalStateException("Trying to merge different line numbers: "
					+ getLineNumber() + " and " + other.getLineNumber());
		return new Method(getLineNumber(), excluding(getCount(), other.getCount()));
	}
}

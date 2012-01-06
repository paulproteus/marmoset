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

public abstract class Line<T extends Line<T>> {
	private final int lineNumber;

	private final int count;

	public Line(int lineNumber, int count) {
		this.lineNumber = lineNumber;
		this.count = count;
	}

	@Override
	public int hashCode() {
		return count * 1717 + lineNumber;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Line))
			return false;
		Line<?> line = (Line<?>) o;
		return lineNumber == line.lineNumber && count == line.count;
	}

	public int getCount() {
		return count;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public Integer getIntegerLineNumber() {
		return lineNumber;
	}

	public boolean isCovered() {
		return count > 0;
	}

	public abstract T union(T other);

	/**
	 * Intersect this line with another line. intersect() basically computes the
	 * "min" between two sets of coverage facts, i.e. if both lines are covered,
	 * it will return this line; otherwise it will check this line and the other
	 * line, in that order, and return the first one that is not covered.
	 * <p>
	 * The goal of the intersect method is to compare coverage data for sets of
	 * test cases, for example public and student tests, and figure out which
	 * lines are uncovered in at least one set of test cases.
	 *
	 * @param other
	 *            The other line.
	 * @return If both lines are covered, it will return this line; otherwise it
	 *         will check this line and the other line, in that order, and
	 *         return the first one that is not covered.
	 */
	public abstract T intersect(T other);

	public abstract T excluding(T other);

	protected static int excluding(int a, int b) {
		if (b > 0)
			return 0;
		return a;
	}
}

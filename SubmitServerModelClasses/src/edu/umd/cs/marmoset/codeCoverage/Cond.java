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

public class Cond extends Line<Cond> {
	private final int trueCount;

	private final int falseCount;

	public Cond(int lineNumber, int trueCount, int falseCount) {
		super(lineNumber, trueCount + falseCount);
		this.trueCount = trueCount;
		this.falseCount = falseCount;
	}

	@Override
	public int hashCode() {
		return trueCount * 4128 + falseCount * 9191 + super.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Cond))
			return false;
		Cond line = (Cond) o;
		return super.equals(line) && trueCount != line.trueCount && falseCount == line.falseCount;
	}

	public int getFalseCount() {
		return falseCount;
	}

	public int getTrueCount() {
		return trueCount;
	}

	@Override
	public Cond union(Cond o) {
		if (!(getLineNumber() == o.getLineNumber()))
			throw new IllegalStateException("Trying to merge different line numbers: "
					+ getLineNumber() + " and " + o.getLineNumber());
		return new Cond(getLineNumber(), getTrueCount() + o.getTrueCount(), getFalseCount()
				+ o.getFalseCount());
	}

	@Override
	public Cond intersect(Cond other) {
		if (!(getLineNumber() == other.getLineNumber()))
			throw new IllegalStateException("Trying to merge different line numbers: "
					+ getLineNumber() + " and " + other.getLineNumber());
		return new Cond(getLineNumber(), Math.min(getTrueCount(), other.getTrueCount()), Math.min(
				getFalseCount(), other.getFalseCount()));
	}

	@Override
	public Cond excluding(Cond other) {
		if (!(getLineNumber() == other.getLineNumber()))
			throw new IllegalStateException("Trying to merge different line numbers: "
					+ getLineNumber() + " and " + other.getLineNumber());
		return new Cond(getLineNumber(), excluding(getTrueCount(), other.getTrueCount()),
				excluding(getFalseCount(), other.getFalseCount()));
	}

	@Override
	public boolean isCovered() {
		// A conditional is only "covered" if both it's true and false branches
		// are executed
		return (falseCount > 0) && (trueCount > 0);
	}

}

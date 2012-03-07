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
 * Created on Jan 23, 2005
 */
package edu.umd.cs.buildServer.tester;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.Iterator;

import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

/**
 * Dump a saved TestOutcomeCollection to stdout.
 *
 * @author David Hovemeyer
 */
public class DumpTestOutcomes {
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("Usage: " + DumpTestOutcomes.class.getName()
					+ " <test outcome file>");
			System.exit(1);
		}

		String filename = args[0];

		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(filename));
			TestOutcomeCollection testOutcomeCollection = new TestOutcomeCollection();
			testOutcomeCollection.read(in);

			dump(testOutcomeCollection, System.out);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
	}

	/**
	 * @param testOutcomeCollection
	 * @param out
	 */
	public static void dump(TestOutcomeCollection testOutcomeCollection,
			PrintStream out) {
		for (Iterator<TestOutcome> i = testOutcomeCollection.iterator(); i
				.hasNext();) {
			TestOutcome testOutcome = i.next();

			out.println(testOutcome.getTestType()
					+ "("
					+ testOutcome.getTestNumber()
					+ "),"
					+ testOutcome.getTestName()
					+ ","
					+ testOutcome.getOutcome()
					+ (testOutcome.getTestType().equals(
							TestOutcome.TestType.RELEASE)
							|| testOutcome.getTestType().equals(
									TestOutcome.TestType.SECRET) ? ","
							+ testOutcome.getExceptionSourceCoveredElsewhere()
							+ "," + testOutcome.getCoarsestCoverageLevel() : ""));
			if (testOutcome.getOutcome().equals(TestOutcome.FAILED)
					|| testOutcome.getOutcome().equals(TestOutcome.ERROR)
					|| testOutcome.getOutcome().equals(TestOutcome.HUH)
					|| testOutcome.getOutcome().equals(TestOutcome.MISSING_COMPONENT)
					|| testOutcome.getOutcome().equals(TestOutcome.TIMEOUT)) {
				out.println("Long test result for failed test:");
				out.println(testOutcome.getLongTestResult());
			}
			if (testOutcome.getOutcome().equals(TestOutcome.UNCOVERED_METHOD)) {
				out.println("uncovered method: "
						+ testOutcome.getLongTestResult());
			}

			if (testOutcome.getTestType().equals(TestOutcome.TestType.FINDBUGS)) {
				out.println("findbugs: " + testOutcome.getLongTestResult());
				if (testOutcome.getShortTestResult().length() > 0) {
					out.println("\t" + testOutcome.getShortTestResult());
				}
			}
		}
	}
}

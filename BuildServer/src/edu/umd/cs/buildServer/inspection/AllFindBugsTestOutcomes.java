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

package edu.umd.cs.buildServer.inspection;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;

import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

public class AllFindBugsTestOutcomes {

	static class StudentProject {
		StudentProject(String studentRegistrationPK, String projectPK,
				long commitTimestamp) {
			this.studentRegistrationPK = studentRegistrationPK;
			this.projectPK = projectPK;
			this.commitTimestamp = commitTimestamp;
		}

		String studentRegistrationPK, projectPK;
		long commitTimestamp;
	}

	private HashMap<String, StudentProject> submissionToStudentProjectMap = new HashMap<String, StudentProject>();
	private PrintWriter out;

	public void readSubmissionToStudentProjectMap(String fileName)
			throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tuple = line.split(",");
				if (tuple.length != 4)
					throw new IOException("Bad tuple: " + line);
				submissionToStudentProjectMap
						.put(tuple[0], new StudentProject(tuple[1], tuple[2],
								Long.valueOf(tuple[3]).longValue()));
			}
		} finally {
			reader.close();
		}
	}

	public void setOutputFile(String outputFile) throws IOException {
		this.out = new PrintWriter(new FileWriter(outputFile));
	}

	private void readTestOutcomeCollection(String tocFile) throws IOException {

		if (!tocFile.endsWith(".out"))
			throw new IllegalArgumentException("Bad name for toc file: "
					+ tocFile);
		String submissionPK = tocFile.substring(0, tocFile.lastIndexOf(".out"));

		TestOutcomeCollection toc = new TestOutcomeCollection();
		toc.read(new ObjectInputStream(new BufferedInputStream(
				new FileInputStream(tocFile))));

		for (Iterator<TestOutcome> i = toc.iterator(); i.hasNext();) {
			TestOutcome testOutcome = i.next();

			StudentProject sproj = submissionToStudentProjectMap
					.get(submissionPK);
			if (sproj == null)
				throw new IllegalStateException(
						"No student project for submission " + submissionPK);

			out.print(sproj.studentRegistrationPK);
			out.print(",");
			out.print(sproj.projectPK);
			out.print(",");
			out.print(submissionPK);
			out.print(",");
			out.print(sproj.commitTimestamp);
			out.print(",");
			out.print(testOutcome.getTestType());
			out.print(",");
			out.print(testOutcome.getTestName());
			out.print(",");
			out.print(testOutcome.getOutcome());
			out.print(",");
			out.println(testOutcome.getExceptionClassName());
		}
	}

	private void done() {
		out.flush();
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			throw new IllegalArgumentException("No");
		}

		AllFindBugsTestOutcomes fb = new AllFindBugsTestOutcomes();
		fb.readSubmissionToStudentProjectMap(args[0]);
		fb.setOutputFile(args[1]);

		for (int i = 2; i < args.length; ++i) {
			fb.readTestOutcomeCollection(args[i]);
		}

		fb.done();
	}
}

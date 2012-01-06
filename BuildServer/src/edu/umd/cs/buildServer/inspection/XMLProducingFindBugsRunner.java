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
 * Created on April 18, 2005
 */
package edu.umd.cs.buildServer.inspection;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import edu.umd.cs.buildServer.ConfigurationKeys;
import edu.umd.cs.buildServer.MissingConfigurationPropertyException;
import edu.umd.cs.buildServer.util.ArgumentParser;
import edu.umd.cs.buildServer.util.DevNullOutputStream;
import edu.umd.cs.buildServer.util.IO;
import edu.umd.cs.buildServer.util.MonitorThread;
import edu.umd.cs.buildServer.util.TextOutputSink;

/**
 * FindBugsRunner that saves the captured BugCollection to a file in a specified
 * directory.
 * 
 * @author David Hovemeyer
 */
public class XMLProducingFindBugsRunner extends AbstractFindBugsRunner {

	@Override
	protected String[] getExtraFindBugsOptions() {
		try {
			ArrayList<String> options = new ArrayList<String>();
			String fbOptions = getProjectSubmission().getConfig().getConfig()
					.getOptionalProperty("findbugs.options");
			if (fbOptions != null) {
				ArgumentParser argParser = new ArgumentParser(fbOptions);
				while (argParser.hasNext()) {
					options.add(argParser.next());
				}
			}

			options.add("-outputFile");
			options.add(getOutputFile().getPath());

			return options.toArray(new String[options.size()]);
		} catch (MissingConfigurationPropertyException e) {
			projectSubmission.getLog().error(
					"Error generating FindBugs command line", e);
			return null;
		}
	}

	private File getOutputFile() throws MissingConfigurationPropertyException {
		StringBuffer outputDir = new StringBuffer();
		outputDir.append(projectSubmission.getConfig().getConfig().getRequiredProperty(
				ConfigurationKeys.FINDBUGS_OUTPUT_DIRECTORY));

		String submissionPK = projectSubmission.getSubmissionPK();
		int len = submissionPK.length();
		if (len >= 2 && isDigit(submissionPK.charAt(len - 1))
				&& isDigit(submissionPK.charAt(len - 2))) {
			// Use the hierarchical directory organization to avoid putting
			// a huge number of output files in the same directory.
			// We use the *last* two digits of the submission pk,
			// rather than the first two, because they are much more
			// evenly distributed.
			outputDir.append(File.separatorChar);
			outputDir.append(submissionPK.charAt(len - 1));
			outputDir.append(File.separatorChar);
			outputDir.append(submissionPK.charAt(len - 2));
		}

		File outputFile = new File(outputDir.toString(), submissionPK + ".xml");

		return outputFile;
	}

	@Override
	protected Thread createStdoutMonitor(InputStream in) {
		return IO.monitor(in, new DevNullOutputStream());
	}

	@Override
	protected Thread createStderrMonitor(InputStream err) {
		return new MonitorThread(err, new TextOutputSink());
	}

	@Override
	protected void inspectFindBugsResults(Thread stdoutMonitor,
			Thread stderrMonitor) {
		String errorOutput = ((MonitorThread) stderrMonitor).getOutputSink()
				.getOutput();
		if (!errorOutput.equals("")) {
			projectSubmission.getLog().warn(
					"Error output from FindBugs process:\n" + errorOutput);
		}
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

}

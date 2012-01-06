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
 * Created on May 1, 2005
 */
package edu.umd.cs.buildServer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Thread to monitor the stdout or stderr of a process and append its output to
 * a TextOutputSink. The amount of output read is limited to
 * MAX_NUM_BYTES_OUTPUT.
 */
public class MonitorThread extends Thread {
	private LimitedInputStream in;
	private BufferedReader reader;
	private TextOutputSink outputSink;

	/**
	 * Constructor.
	 * 
	 * @param in
	 *            the InputStream to monitor
	 */
	public MonitorThread(InputStream in, TextOutputSink outputSink) {
		this.in = new LimitedInputStream(in,
				CombinedStreamMonitor.MAX_NUM_BYTES_INPUT);
		this.reader = new BufferedReader(new InputStreamReader(this.in));
		this.outputSink = outputSink;
	}

	public TextOutputSink getOutputSink() {
		return outputSink;
	}

	public void setDrainLimit(int maxBytes) {
		in.setDrainLimit(maxBytes);
	}

	@Override
	public void run() {
		try {
			// Read lines of output from the stream
			String line;
			while ((line = reader.readLine()) != null) {
				outputSink.appendLine(line);
			}
		} catch (IOException e) {
			// Ignore
		} finally {
			// Attempt to drain all output from the stream,
			// and close it.
			try {
				in.drain();
			} catch (IOException e) {
				// Ignore
			}
			try {
				in.close();
			} catch (IOException e) {
				// Ignore
			}
		}
	}
}

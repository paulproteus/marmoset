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
 * Created on Sep 1, 2004
 */
package edu.umd.cs.buildServer.util;

import java.io.InputStream;

import edu.umd.cs.buildServer.BuildServer;

/**
 * Monitor two input streams asynchronously.
 * 
 * @author David Hovemeyer
 */
public class CombinedStreamMonitor {
	static final int MAX_NUM_BYTES_INPUT = 64 * 1024;

	// Fields
	private TextOutputSink outputSink;
	private MonitorThread stdoutMonitor, stderrMonitor;

	/**
	 * Constructor.
	 * 
	 * @param in
	 *            an input stream (usually the stdout from a Process)
	 * @param err
	 *            another input stream (usually the stderr from a Process)
	 */
	public CombinedStreamMonitor(InputStream in, InputStream err) {
		this.outputSink = new TextOutputSink();
		this.stdoutMonitor = new MonitorThread(in, outputSink);
		this.stderrMonitor = new MonitorThread(err, outputSink);
	}

	public void setDrainLimit(int maxBytes) {
		// FIXME: this should really be an overall limit, not just per-stream
		stdoutMonitor.setDrainLimit(maxBytes);
		stderrMonitor.setDrainLimit(maxBytes);
	}

	/**
	 * Start asynchronously reading data from the streams.
	 */
	public void start() {
		stdoutMonitor.start();
		stderrMonitor.start();
	}

	/**
	 * Wait for all data to be read.
	 * 
	 * @throws InterruptedException
	 *             if either reading threads is interrupted
	 */
	public void join() throws InterruptedException {
		stdoutMonitor.join(5000);
		if (stdoutMonitor.isAlive()) {
			BuildServer.instance().getLog()
					.warn("Had to interrupt stdoutMonitor");
			stdoutMonitor.interrupt();
		}
		stderrMonitor.join(5000);
		if (stderrMonitor.isAlive()) {
			BuildServer.instance().getLog()
					.warn("Had to interrupt stderrMonitor");
			stderrMonitor.interrupt();
		}
	}

	/**
	 * Get the combined output from both streams.
	 * 
	 * @return a String containing the combined output
	 */
	public String getCombinedOutput() {
		return outputSink.getOutput();
	}
}

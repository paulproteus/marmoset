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
 * Created on Feb 10, 2005
 */
package edu.umd.cs.buildServer.util;

import org.apache.log4j.Logger;


/**
 * Wait a fixed amount of time for a process to exit.
 * 
 * @author David Hovemeyer
 */
public final class ProcessExitMonitor extends Thread {
    
    private final Logger log;
	private final Process process;
	private volatile boolean exited;
	private volatile int exitCode;
	private final ProcessTree tree;

	/**
	 * Constructor.
	 * 
	 * @param process
	 *            the process to monitor for exit
	 * @param logger TODO
	 */
	public ProcessExitMonitor(Process process, Logger logger) {
		this.setDaemon(true);
		this.process = process;
		this.log = logger;
		this.exited = false;
		
		this.tree = new ProcessTree(logger);
		this.start();
	}


	@Override
	public void run() {
		try {
		    Thread.sleep(500);

		    tree.computeChildren();
			this.exitCode = process.waitFor();
			synchronized (this) {
				this.exited = true;
				notifyAll();
			}
		} catch (InterruptedException e) {
		    Untrusted.destroyProcessTree(process, tree, log);
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Wait given number of milliseconds for the process to exit. If it does
	 * not finish within that amount of time, kill the process;
	 * 
	 * @param millis
	 *            number of milliseconds to wait for the process to exit
	 * @return true if the process exited in the given amount of time, false
	 *         otherwise
	 * @throws InterruptedException
	 */
    public boolean waitForProcessToExit(long millis)
             {
        try {
            if (!exited) {
                Thread.sleep(100);
                synchronized (this) {
                    tree.computeChildren();
                    wait(millis);
                }
            }
        } catch (InterruptedException e) {
           interrupt();
        } finally {
            synchronized (this) {
                Untrusted.destroyProcessTree(process, tree, log);
            }
        }
        return exited;
	}

	/**
	 * @return Returns the exitCode.
	 */
	public int getExitCode() {
	    if (!exited)
	        throw new IllegalStateException();
		return exitCode;
	}
}

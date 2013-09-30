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

/**
 * @author jspacco
 */
package edu.umd.cs.marmoset.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * JProcess
 * Improved abstraction for a spawned Java process.
 * TODO Improved version that's quote-aware for command line arguments
 * @author jspacco
 */
final public class JProcess
{
    private String[] cmd;
    private Process process;
    private DrainThread stdoutThread;
    private DrainThread stderrThread;
    private Logger log;
    
    public String getCommand()
    {
        StringBuffer buf=new StringBuffer();
        for (String s : cmd) {
            buf.append(s);
            buf.append(" ");
        }
        return buf.toString();
    }
    
    public JProcess(String command)
    throws IOException
    {
        this(command.split("\\s+"));
    }
    
    public JProcess(String cmd[])
    throws IOException
    {
 
        log=Logger.getLogger(this.getClass());
        
        this.cmd=cmd;
        process=Runtime.getRuntime().exec(cmd);
        
        stdoutThread = new DrainThread(process.getInputStream());
        stdoutThread.start();
        
        stderrThread = new DrainThread(process.getErrorStream());
        stderrThread.start();
    }

    private static class DrainThread extends Thread
    {
        final StringBuffer buf=new StringBuffer();
        final InputStream is;
        private CountDownLatch done = new CountDownLatch(1);
        public DrainThread(InputStream is) {
            this.is=is;
        }
        
        public  String getOutput() {
        	try {
				done.await();
			} catch (InterruptedException e) {
			throw new RuntimeException(e);
			}
        
            return buf.toString();
        }
        
        @Override
		public void run()
        {
            BufferedReader reader=null;
            try {

                    reader=new BufferedReader(new InputStreamReader(is));
                    while (true) {
                        String line=reader.readLine();
                        if (line==null) break;
                        buf.append(line + "\n");
                    }
                    done.countDown();
                
            } catch (IOException e) {
                buf.append(e.toString());
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(reader);
                IOUtils.closeQuietly(is);
            }
        }
    }
    
    /**
     * TODO Implement the part of this that kills things.
     * @param waitTimeMillis Wait for specified number of millis; if the process doesn't return
     *      at that point then we'll just kill it.
     * @return The return code for the process.
     * @throws InterruptedException
     */
    public int waitFor(long waitTimeMillis)
    throws InterruptedException
    {
        // TODO Actually implement the timed wait...
        WaitThread waitThread=new WaitThread(process);
        waitThread.start();
        synchronized (waitThread) {
            // Only wait once; if the process is busy after that then kill it
            if (waitThread.isBusy())
                waitThread.wait(waitTimeMillis);
            if (waitThread.isBusy()) {
                destroyProcessGroup(process,log);
            }
            return waitThread.getReturnVal();
        }
    }
    
    private static class WaitThread extends Thread
    {
        private int returnVal;
        private Process process;
        private volatile boolean busy=true;
        WaitThread(Process process) {
            this.process=process;
        }
        
        @Override
		public void run() {
            try {
                int tmp =process.waitFor();
                synchronized (this) {
                	returnVal = tmp;
                    busy=false;
                    notifyAll();
                }
            } catch (InterruptedException e) {
                // ignore
            }
        }
        public synchronized int getReturnVal() {
            return returnVal;
        }
        /**
         * @return Returns the busy.
         */
        public synchronized boolean isBusy() {
            return busy;
        }
    }

    public String getErr()
    {
        return stderrThread.getOutput();
    }

    public String getOut()
    {
        return stdoutThread.getOutput();
    }
    
    public static void destroyProcessGroup(Process process, Logger log)
    {
        int pid=0;
        try {
            pid = getPid(process);
            
            log.debug("PID to be killed = " +pid);
            
            String command = "kill -9 -" +pid;
            
            String[] cmd = command.split("\\s+");
            
            Process kill = Runtime.getRuntime().exec(cmd);
            log.warn("Trying to kill the process group leader: " +command);
            kill.waitFor();
        } catch (IOException e) {
            // if we can't execute the kill command, then try to destroy the process
            log.warn("Unable to execute kill -9 -" +pid+ "; now calling process.destroy()");
        } catch (InterruptedException e) {
            log.error("kill -9 -" +pid+ " process was interrupted!  Now calling process.destroy()");
        } catch (IllegalAccessException e) {
            log.error("Illegal field access to PID field; calling process.destroy()", e);
        } catch (NoSuchFieldException e) {
            log.error("Cannot find PID field; calling process.destroy()", e);
        } finally {
            // call process.destroy() whether or not "kill -9 -<pid>" worked
            // in order to maintain proper internal state
            process.destroy();
        }
    }
    
    /**
     * Uses reflection to extract the pid, a private field of the private class UNIXProcess.
     * This will fail on any non-Unix platform that doesn't use UNIXProcess.  It may
     * fail if the UNIXProcess class changes at all.  It may fail anyway for unpredictable
     * reasons.
     * @param process The process
     * @return the pid of this process
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static int getPid(Process process)
    throws NoSuchFieldException, IllegalAccessException
    {
        Class<? extends Process> processClass = process.getClass();
        Field pidField = processClass.getDeclaredField("pid");
        pidField.setAccessible(true);
        return pidField.getInt(process);
    }
}

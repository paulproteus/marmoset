package edu.umd.cs.buildServer.util;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import edu.umd.cs.marmoset.utilities.MarmosetUtilities;

public class Untrusted {
    
    public static Process execute(String cmd[], String [] env, File cwd) throws IOException {
       return Runtime.getRuntime().exec(cmd, env, cwd);
    }
    public static Process execute(String exec, String [] env, File cwd) throws IOException {
        return Runtime.getRuntime().exec(exec, env, cwd);
     }

    

    /**
     * Uses the kill command to kill this process as a group leader with: <br>
     * kill -9 -&lt;pid&gt;
     * <p>
     * If kill -9 -&lt;pid&gt; fails, then this method will call
     * @param process
     */
    public static void destroyProcessGroup(Process process, Logger log)
    {
        int pid=0;
        try {
            pid = MarmosetUtilities.getPid(process);

            log.debug("PID to be killed = " +pid);

            //String command = "kill -9 -" +pid;
            String command = "kill -9 " +pid;

            String[] cmd = command.split("\\s+");

            Process kill = Untrusted.execute(cmd, new String[0], new File("/tmp"));
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

}

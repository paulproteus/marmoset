package edu.umd.cs.buildServer.util;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import edu.umd.cs.marmoset.utilities.MarmosetUtilities;

public class Untrusted {
    
    public static Process execute(File cwd, String... cmd) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.directory(cwd);
        return builder.start();
    }
   
    public static void destroyProcessTree(Process process, Logger log)  {
            
        destroyProcessTree(process, new ProcessTree(log), log);
    }
        

    public static void destroyProcessTree(Process process, ProcessTree tree,  Logger log) {
        int pid = 0;
        try {
            pid = MarmosetUtilities.getPid(process);

            log.info("Killing process tree for " + pid);
            tree.computeChildren();
            tree.killProcessTree(pid, 9);
        } catch (Exception e) {
            log.warn("Error trying to kill process tree for " + pid, e);
        }

        finally {
            // call process.destroy() whether or not "kill -9 -<pid>" worked
            // in order to maintain proper internal state
            process.destroy();
        }
    }

}

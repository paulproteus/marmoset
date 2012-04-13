package edu.umd.cs.buildServer.util;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

public class Untrusted {
    
    public static Process executeCombiningOutput(File cwd, String... cmd) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.redirectErrorStream(true);
        builder.directory(cwd);
        return builder.start();
    }
    public static Process execute(File cwd, String... cmd) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.directory(cwd);
        return builder.start();
    }
   
    public static void destroyProcessTree(Process process, Logger log)  {
            
        new ProcessTree(process, log).destroyProcessTree();
    }
        

   
}

package edu.umd.cs.buildServer.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

public class LoggingDevNullOutputStream extends OutputStream {
    
    PrintStream logTo = System.out;
    Set<RuntimeException> seen = new HashSet<RuntimeException>();

    private void log() {
        RuntimeException e = new RuntimeException("Wrote to dev null");
        if (seen.add(e))
            e.printStackTrace(logTo);
    }
    
    @Override
    public void write(int arg0) throws IOException {
        log();
    }

    @Override
    public void write(byte[] b) {
        log();
    }

    @Override
    public void write(byte[] b, int off, int len) {
        log();
    }

}

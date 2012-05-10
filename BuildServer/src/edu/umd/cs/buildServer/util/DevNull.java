package edu.umd.cs.buildServer.util;

import java.io.IOException;
import java.io.OutputStream;

public class DevNull extends OutputStream {

    @Override
    public void write(int arg0) throws IOException {
    }

    @Override
    public void write(byte[] b) {
    }

    @Override
    public void write(byte[] b, int off, int len) {
    }

}

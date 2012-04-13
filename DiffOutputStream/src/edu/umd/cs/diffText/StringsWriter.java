package edu.umd.cs.diffText;

import java.io.IOException;
import java.io.OutputStream;

public abstract class StringsWriter extends OutputStream {

    abstract protected void got(String s);

    private StringBuilder buf = new StringBuilder();
    private boolean skipLF;

    private IOException closed = null;

    private int line = 1;

    public int getLine() {
        return line;
    }

    @Override
    public void write(int b) throws IOException {
        if (closed != null)
            throw new IOException("Already closed", closed);
        if (b >= 0x80)
            throw new UnsupportedOperationException("Not currently handling non-ASCII");
        if (skipLF) {
            skipLF = false;
            if (b == '\n')
                return;
        }
        if (b == '\r' || b == '\n') {
            skipLF = b == '\r';
            try {
                got(buf.toString());
            } finally {
                line++;
                buf = new StringBuilder();
            }
            return;
        }
        buf.append((char) b);
    }

    @Override
    public void close() {
        if (buf.length() > 0)
            got(buf.toString());
        buf.setLength(0);
        if (closed == null)
            closed = new IOException("Stream closed");
    }

}

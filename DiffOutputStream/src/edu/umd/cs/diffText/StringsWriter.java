package edu.umd.cs.diffText;
import java.io.IOException;
import java.io.OutputStream;

public abstract class StringsWriter extends OutputStream {

    abstract protected void got(int line, String s);

    private StringBuilder buf = new StringBuilder();
    private boolean skipLF;

    private int line = 1;
    @Override
    public void write(int b) throws IOException {
        if (b >= 0x80)
            throw new IllegalArgumentException();
        if (skipLF) {
            skipLF = false;
            if (b == '\n')
                return;
        }
        if (b == '\r' || b == '\n') {
            skipLF = b == '\r';
            got(line++, buf.toString());
            buf = new StringBuilder();
            return;
        }
        buf.append((char)b);
    }

    @Override
    public void close() {
        if (buf.length() > 0)
            got(line++, buf.toString());
        buf.setLength(0);
    }

}

package edu.umd.cs.diffText;
import static junit.framework.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextDiff extends StringsWriter {

    public static void invokeMain(Class<?> c, InputStream input, OutputStream out)
            throws Throwable {
        Method main = c.getMethod("main", String[].class);
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        System.setIn(input);
        PrintStream oStream = new PrintStream(out);
        System.setOut(oStream);
        try {
             main.invoke(null, (Object) new String[0]);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        oStream.close();
        input.close();
        System.setIn(oldIn);
        System.setOut(oldOut);
        
    }

    public static void check(Class<?> testedMain, Class<?> referenceMain, String inputFile) throws Throwable {
        StringListWriter expectedWriter = new StringListWriter();
        File f = new File(inputFile);
        invokeMain(referenceMain, new FileInputStream(f), expectedWriter);
        Builder b = new Builder().expect(expectedWriter.getStrings()).trim().ignoreCase();
        invokeMain(testedMain, new FileInputStream(f), b.build());
    }
    public static void check(Class<?> testedMain, String inputFile, String expectedOutputFile) throws Throwable {
        Builder b = new Builder().expect(new File(expectedOutputFile)).trim().ignoreCase();
        invokeMain(testedMain, new FileInputStream(new File(inputFile)), b.build());
    }

    static class Builder {
        boolean ignoresCase;
        boolean trim;
        final ArrayDeque<Object> expect = new ArrayDeque<Object>();

        public Builder trim() {
            trim = true;
            return this;
        }

        public Builder ignoreCase() {
            ignoresCase = true;
            return this;
        }

        public Builder expect(String s) {
            expect.add(s);
            return this;
        }

        public Builder expect(String... strings) {
            for (String s : strings)
                expect.add(s);
            return this;
        }

        public Builder expect(Iterable<String> strings) {
            for (String s : strings)
                expect.add(s);
            return this;
        }

        public Builder expect(Pattern p) {
            expect.add(p);
            return this;
        }

        public Builder expect(File f) {
            expect.add(f);
            return this;
        }

        public TextDiff build() {
            return new TextDiff(new ArrayDeque<Object>(expect), trim,
                    ignoresCase);

        }

    }

    final boolean ignoresCase;
    final boolean trim;
    final boolean ignoreBlank;
    final ArrayDeque<Object> expect;

    TextDiff(ArrayDeque<Object> expect, boolean trim, boolean ignoresCase) {
        this.expect = expect;
        this.trim = trim;
        this.ignoresCase = ignoresCase;
        this.ignoreBlank = false;

    }

    private String normalize(String s) {
        if (trim)
            s = s.trim();
        if (ignoresCase)
            s = s.toLowerCase();
        return s;
    }

    private Object getExpected() {
        try {
        while (true) {
            Object o = expect.pollFirst();
            if (o == null)
                return null;
            Class<?> c = o.getClass();
            if (c == String.class) {
                return normalize((String) o);
            } else if (c == Pattern.class) {
                return c;
            } else if (c == BufferedReader.class) {
                BufferedReader r = (BufferedReader) o;
                String txt = r.readLine();
                if (txt == null)
                    continue;
                expect.addFirst(r);
                return normalize(txt);

            } else if (c == File.class) {
                BufferedReader r = new BufferedReader(new FileReader((File) o));
                expect.addFirst(r);
            } else {
                throw new IllegalStateException("Did not expect " + c);
            }
        }
        } catch(IOException e) {
            AssertionError ae =  new AssertionError("Error in checking output : " + e.getMessage());
            ae.initCause(e);
            throw ae;
        }

    }

    protected void got(int line, String txt) {
//        System.err.println("Got " + txt);
       txt = normalize(txt);
       Object o = getExpected();
       if (o == null) {
           fail("Didn't expect any more output but got " + txt);
       } else if (o instanceof String) {
//           System.err.println("Checking " + txt + " against " + o);
           assertEquals(o,txt);
       } else if (o instanceof Pattern) {
           Pattern p = (Pattern)o;
           Matcher m = p.matcher(txt);
           assertTrue(p + " matches " + txt, m.matches());
       } else {
           fail("Got " + o.getClass());
       }

    }
    
    @Override 
    public void close() {
        Object o = getExpected();
        if (o == null)
            return;
        fail("no more output; expected " + o);
        
    }

}

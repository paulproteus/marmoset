package edu.umd.cs.diffText;

import java.io.PrintWriter;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.junit.Test;

import edu.umd.cs.diffText.TextDiff.Option;

public class TextDiffTest {

    @Test
    public void testSimple() {
        TextDiff t = new TextDiff.Builder().expect("a", "b").build();
        
        PrintWriter out = new PrintWriter(t);
        
        out.println("a");
        out.println("b");
        out.close();
    }
    
    @Test
    public void testTrim() {
        TextDiff t = new TextDiff.Builder().expect("a", "b").set(Option.TRIM).build();
        
        PrintWriter out = new PrintWriter(t);
        
        out.println(" a ");
        out.println("b  \t");
        out.close();
    }
    @Test
    public void testIgnoreCase() {
        TextDiff t = new TextDiff.Builder().expect("a", "b").set(Option.IGNORE_CASE).build();
        
        PrintWriter out = new PrintWriter(t);
        
        out.println("A");
        out.println("B");
        out.close();
    }
    @Test
    public void testIgnoreBlankLines() {
        TextDiff t = new TextDiff.Builder().expect("a", "b").set(Option.IGNORE_BLANK_LINES).build();
        
        PrintWriter out = new PrintWriter(t);
        out.println();
        out.println();
        out.println("a");
        out.println();
        out.println("b");
        out.println();
        out.close();
    }
    @Test
    public void testIgnoreWhitespaceChange() {
        TextDiff t = new TextDiff.Builder().expect("a 1 2", "b 3 4").set(Option.IGNORE_WHITESPACE_CHANGE).build();
        
        PrintWriter out = new PrintWriter(t);

        out.println("a   1   2");

        out.println("b 3    4");
        out.close();
    }
    
    @Test(expected= AssertionFailedError.class)
    public void testSimpleFail() {
        TextDiff t = new TextDiff.Builder().expect("a", "b").build();
        
        PrintWriter out = new PrintWriter(t);
        
        out.println("a");
        out.println("c");
        out.close();
    }
    @Test(expected= AssertionFailedError.class)
    public void testFailTooMuchOutput() {
        TextDiff t = new TextDiff.Builder().expect("a", "b").build();
        
        PrintWriter out = new PrintWriter(t);
        
        out.println("a");
        out.println("b");
        out.println("c");
        out.close();
    }
    @Test(expected= AssertionFailedError.class)
    public void testFailTooLittleOutput() {
        TextDiff t = new TextDiff.Builder().expect("a", "b").build();
   
        PrintWriter out = new PrintWriter(t);
        
        out.println("a");
        out.close();
    }
    
    public void testWaitFor() {
        TextDiff t = new TextDiff.Builder().set(Option.WAIT_FOR, "XXX").expect("YYY", "XXX", "a", "b").build();
   
        PrintWriter out = new PrintWriter(t);
        
        out.println("ZZZ");
        out.println("42");
        out.println("XXX");
        out.println("a");
        out.println("b");
        out.close();
    }
    
    public void testOptions() {
        Assert.assertEquals(Option.IGNORE_BLANK_LINES, Option.valueOfAnyCase("ignoreBlankLines"));
        Assert.assertEquals(Option.IGNORE_BLANK_LINES, Option.valueOfAnyCase("ignoresBlankLines"));
        Assert.assertEquals(Option.IGNORE_BLANK_LINES, Option.valueOfAnyCase("ignores_Blank_Lines"));
        
    }


}

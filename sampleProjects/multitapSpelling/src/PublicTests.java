import junit.framework.TestCase;
import edu.umd.cs.diffText.TextDiff;

/**
 * This includes the tests cases provided in the 
 * <a href="http://code.google.com/codejam/contest/351101/dashboard#s=p2">
 * problem description</a>
 * 
 * The problems are provides both as individual unit tests, and as a test using
 * the diffOutputStream framework for comparing the output of the main method.
 */
public class PublicTests extends TestCase {

    public void testCase1() {
        assertEquals("44 444", MultiTapTextEntry.getMultitapSpelling("hi"));
    }

    public void testCase2() {
        assertEquals("999337777", MultiTapTextEntry.getMultitapSpelling("yes"));
    }

    public void testCase3() {
        assertEquals("333666 6660 022 2777",
                MultiTapTextEntry.getMultitapSpelling("foo bar"));
    }

    public void testCase4() {
        assertEquals("4433555 555666096667775553",
                MultiTapTextEntry.getMultitapSpelling("hello world"));
    }

    public void testTiny() throws Throwable {
        new TextDiff.Builder().trim().ignoreCase().check
        (MultiTapTextEntry.class, "C-tiny-practice.in.txt",
                "C-tiny-practice.expected.txt");
    }

}

import edu.umd.cs.diffText.TextDiff;
import junit.framework.TestCase;

public class ReleaseTests extends TestCase {

    public void testSmall() throws Throwable {
        TextDiff.check(MultiTapTextEntry.class, "C-small-practice.in.txt", "C-small-practice.expected.txt");
    }

    public void testLarge() throws Throwable {
        TextDiff.check(MultiTapTextEntry.class, "C-large-practice.in.txt", "C-large-practice.expected.txt");
    }
}

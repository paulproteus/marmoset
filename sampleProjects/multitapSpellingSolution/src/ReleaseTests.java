import edu.umd.cs.diffText.TextDiff;
import junit.framework.TestCase;

public class ReleaseTests extends TestCase {
    
    TextDiff.Builder builder;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        builder = TextDiff.withOptions().trim().ignoreCase();
    }
    
    public void testSmall() throws Throwable {
        builder.check(MultiTapTextEntry.class, "C-small-practice.in.txt", "C-small-practice.expected.txt");
    }

    public void testLarge() throws Throwable {
        builder.check(MultiTapTextEntry.class, "C-large-practice.in.txt", "C-large-practice.expected.txt");
    }
}

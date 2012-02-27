import junit.framework.TestCase;

/**
 * These are the release tests for the project. Normally, the release tests are
 * never distributed to students, and students are given only a limited
 * opportunity to see the names of the release tests.
 * 
 */

public class ReleaseTests extends TestCase {

    public void testOne() {
        assertEquals(1, Project0.square(1));
    }

    public void testNegativeOne() {
        assertEquals(1, Project0.square(-1));
    }

    public void testTwo() {
        assertEquals(4, Project0.square(2));
    }

    public void testNegativeTwo() {
        assertEquals(4, Project0.square(-2));
    }
   
    private void test(int x) {
        assertEquals("Checking " + x, x*x, Project0.square(x));
    }
    public void testUpToOneThousand() {
        for(int x = 0; x <= 1000; x++)
            test(x);
    }
    public void testDownToToNegativeOneThousand() {
        for(int x = 0; x >= -1000; x--)
            test(x);
    }
    public void testExtremeValues() {
        test(0x1000000);
        test(Integer.MIN_VALUE);
        test(Integer.MAX_VALUE);
    }

}

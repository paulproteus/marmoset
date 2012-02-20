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

    /**
     * An exhaustive test; this would seem to be prohibitive, but it runs in
     * less than a minute.
     */
    public void testExhaustive() {
        for (int i = Integer.MIN_VALUE;; i++) {
            assertEquals(i * i, Project0.square(i));
            if (i == Integer.MAX_VALUE)
                break;
        }

    }

}

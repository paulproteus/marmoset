import junit.framework.TestCase;

public class PublicTests extends TestCase {

    public void testIsOddFor0_2() {
        assertTrue(Warmup.isOdd(1));
        assertFalse(Warmup.isOdd(0));
        assertFalse(Warmup.isOdd(2));
    }

    public void testFirstDigitSimple() {
        assertEquals(0, Warmup.firstDigit(0));
        assertEquals(1, Warmup.firstDigit(1));
        assertEquals(9, Warmup.firstDigit(9));
    }

    public void testFirstDigitTeens() {
        assertEquals(1, Warmup.firstDigit(13));
        assertEquals(1, Warmup.firstDigit(14));
        assertEquals(1, Warmup.firstDigit(19));
    }

    public void testDistinctValues() {
        assertEquals(1, Warmup.distinctValues(1, 1, 1));
        assertEquals(3, Warmup.distinctValues(1, 2, 3));
    }

    public void testBitCount() {
        assertEquals(0, Warmup.bitCount(0));
        assertEquals(1, Warmup.bitCount(1));
        assertEquals(1, Warmup.bitCount(2));
        assertEquals(2, Warmup.bitCount(3));
    }

}

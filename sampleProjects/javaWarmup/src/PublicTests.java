
import junit.framework.TestCase;

/** Public tests for the Java warmup. 
 * 
 * Some simple test cases to test basic functionality, and help make sure you
 * understand what the method is supposed to do. These tests are incomplete.
 * 
 * Do not change this file! On the submit server, any changes you make to the public tests
 * are discarded. 
 * 
 */
public class PublicTests extends TestCase {
    
    public  void testIsOddFor0_2() {
        assertTrue(Warmup.isOdd(1));
        assertFalse(Warmup.isOdd(0));
        assertTrue(Warmup.isOdd(2));
    }
  
    
    /** Return the first non-zero digit of the base ten representation of x */
    public  void testFirstDigitSimple() {
        assertEquals(0, Warmup.firstDigit(0));
        assertEquals(1, Warmup.firstDigit(1));
        assertEquals(9, Warmup.firstDigit(9));
    }
    /** Return the first non-zero digit of the base ten representation of x */
    public  void testFirstDigitTeens() {
        assertEquals(1, Warmup.firstDigit(13));
        assertEquals(1, Warmup.firstDigit(14));
        assertEquals(1, Warmup.firstDigit(19));
    }
    
    public void testDistinctValues() {
        assertEquals(1, Warmup.distinctValues(1,1,1));
        assertEquals(3, Warmup.distinctValues(1,2,3));
    }
    public void testBitCount() {
        assertEquals(0, Warmup.bitCount(0));
        assertEquals(1, Warmup.bitCount(1));
        assertEquals(1, Warmup.bitCount(2));
        assertEquals(2, Warmup.bitCount(3));
    }
   
 
}

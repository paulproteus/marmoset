
import junit.framework.TestCase;


public class ReleaseTests extends TestCase {
    
    public void testIsOddForPositiveNumbers() {
        for(int i = 1; i < 2000; i++)
            assertEquals("Checking " + i, (i % 2) != 0, Warmup.isOdd(i));
    }
    public void testIsOddForNegativeNumbers() {
        for(int i = -1; i >= -2000; i--)
            assertEquals("Checking " + i, (i % 2) != 0, Warmup.isOdd(i));
    }
    
    public  void testFirstDigit() {
        assertEquals(1, Warmup.firstDigit(123456789));
        assertEquals(9, Warmup.firstDigit(987654321));
        assertEquals(5, Warmup.firstDigit(546372819));
        assertEquals(1, Warmup.firstDigit(1234567890));
        
    }
    
    public  void testFirstDigitMinus5() {
        assertEquals(5, Warmup.firstDigit(-5));
    }
    public  void testFirstDigitMinus5000() {
        assertEquals(5, Warmup.firstDigit(-5000));
    }
    public void testDistinctValues() {
        assertEquals(1, Warmup.distinctValues(1,1,1));
        assertEquals(1, Warmup.distinctValues(0,0,0));
        assertEquals(3, Warmup.distinctValues(3, 2, 1));
        assertEquals(2, Warmup.distinctValues(3, 2, 3));
        assertEquals(2, Warmup.distinctValues(-3, 2, -3));
    }
    public void testBitCount() {
        assertEquals(31, Warmup.bitCount(0x7fffffff));
        assertEquals(8,  Warmup.bitCount(0x11111111));
        assertEquals(16, Warmup.bitCount(0x35353535));
        assertEquals(8,  Warmup.bitCount(0xff));
        assertEquals(32, Warmup.bitCount(0xffffffff));
        assertEquals(1,  Warmup.bitCount(0x80000000));
    }
   
    public  void testFirstDigitIntegerMinValue() {
        assertEquals(2, Warmup.firstDigit(Integer.MIN_VALUE));
    }
    

    

 
}

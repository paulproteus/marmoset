package edu.umd.cs.marmoset.utilities;

import junit.framework.TestCase;

public class MarmosetUtilitiesTest extends TestCase {
    
    public void testLeftZeroPad() {
        assertEquals("1234567890123456", MarmosetUtilities.leftZeroPad("1234567890123456"));
        assertEquals("0234567890123456", MarmosetUtilities.leftZeroPad("234567890123456"));
        assertEquals("0034567890123456", MarmosetUtilities.leftZeroPad("34567890123456"));
        
    }

}

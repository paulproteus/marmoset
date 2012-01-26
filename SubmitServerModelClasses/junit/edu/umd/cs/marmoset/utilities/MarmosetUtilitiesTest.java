package edu.umd.cs.marmoset.utilities;

import junit.framework.TestCase;

public class MarmosetUtilitiesTest extends TestCase {
    
    public void testLeftZeroPad() {
        assertEquals("1234567890123456", MarmosetUtilities.leftZeroPad("1234567890123456"));
        assertEquals("0234567890123456", MarmosetUtilities.leftZeroPad("234567890123456"));
        assertEquals("0034567890123456", MarmosetUtilities.leftZeroPad("34567890123456"));
        assertEquals("0000000000000006", MarmosetUtilities.leftZeroPad("6"));
        assertEquals("0000000000000000", MarmosetUtilities.leftZeroPad("0"));
    }

    public void testToFullLengthHexString() {
        assertEquals("1234567890123456", MarmosetUtilities.toFullLengthHexString(0x1234567890123456L));
        assertEquals("0234567890123456", MarmosetUtilities.toFullLengthHexString(0x234567890123456L));
        assertEquals("0034567890123456", MarmosetUtilities.toFullLengthHexString(0x34567890123456L));
        assertEquals("0000000000000006", MarmosetUtilities.toFullLengthHexString(6));
        assertEquals("0000000000000000", MarmosetUtilities.toFullLengthHexString(0));
    }

}

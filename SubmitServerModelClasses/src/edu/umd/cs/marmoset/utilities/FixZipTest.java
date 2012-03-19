package edu.umd.cs.marmoset.utilities;

import junit.framework.TestCase;

public class FixZipTest extends TestCase {
    
    public void testGetDirectoryPrefix() {
        assertEquals("foo/project3/", FixZip.getDirectoryPrefix("foo/project3/.submit"));
        
    }

}

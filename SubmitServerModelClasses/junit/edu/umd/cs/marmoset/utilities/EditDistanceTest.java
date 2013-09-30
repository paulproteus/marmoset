package edu.umd.cs.marmoset.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import junit.framework.TestCase;
import edu.umd.cs.marmoset.utilities.EditDistance.Solution;

public class EditDistanceTest extends TestCase {

	static List<Character> makeList(String s) {
		ArrayList<Character> result = new ArrayList<Character>(s.length());
		for(int i = 0; i < s.length(); i++)
		result.add(s.charAt(i));
		return result;
	}
	static class CharacterDistance implements EditDistance.DistanceMetric<Character> {

		@Override
		public int costToInsert(Character newValue) {
			return 3;
		}

		@Override
		public int costToChange(Character oldValue, Character newValue) {
			if (oldValue.equals(newValue))
				return 0;
			if (Character.toLowerCase(oldValue) == Character.toLowerCase(newValue))
				return 1;
			return 4;
		}

		@Override
		public int costToRemove(Character oldValue) {
			return 2;
		}

        @Override
        public int minCostToInsert() {
           return 3;
        }

        @Override
        public int minCostToRemove() {
            return 2;
        }
	}
	EditDistance<Character> edit;

	@Override
	public void setUp() {
		edit = new EditDistance<Character>(new CharacterDistance());

	}

	public void testABCvAXC() {
		String first = "ABC";
		String second = "AXC";
		int expectedCost = 4;
		test(first, second, expectedCost);
	}
	public void testABCDEFvABDEF() {
		String first = "ABCDEF";
		String second = "ABDEF";
		int expectedCost = 2;
		test(first, second, expectedCost);
	}
	/**
	 * delete C
	 * Insert X
	 */
	public void test4() {
		String first = "CD";
		String second = "DX";
		int expectedCost = 5;
		test(first, second, expectedCost);
		BitSet whichAreNew = edit.whichAreNew(makeList(first), makeList(second));
		System.out.println(whichAreNew);
		assertFalse(whichAreNew.get(0));
		assertTrue(whichAreNew.get(1));
	}
	public void testABCDEFvABDXYZEF() {
		String first = "ABCDEF";
		String second = "ABDXYZEF";
		int expectedCost = 11;
		test(first, second, expectedCost);
	}
	public void testABCDEFvABCDEF() {
		String first = "ABCDEF";
		String second = "ABCDEF";
		int expectedCost = 0;
		test(first, second, expectedCost);
	}
	   public void testABCDEFvABCDEFXX() {
	        String first = "ABCDEF";
	        String second = "ABCDEFXX";
	        int expectedCost = 6;
	        test(first, second, expectedCost);
	    }


	private void test(String first, String second, int expectedCost) {
		Solution solution = edit.compute(makeList(first), makeList(second));
		 System.out.println(solution.edit);
		 assertEquals(expectedCost, solution.cost);
	}
	
	/** Need some public test data before I can activate this test */
	public void notTest() throws Exception {
        List<String> file1 = getFile("file1.txt");
        List<String> file2 = getFile("file2.txt");
        long start = System.nanoTime();

        Solution s = EditDistance.SOURCE_CODE_DIFF.compute(file1, file2);
        long finish = System.nanoTime();
        System.out.println((finish - start) / 1000000);
	    
	}
	
	
	
	public String getShown(String changed) {
	    BitSet b = bitSet(changed);
	    BitSet result = EditDistance.showLines(b, changed.length());
	    String shown = toString(result, changed.length());
	    return shown;
	}
	public void showAll(String changed) {
	    BitSet b = bitSet(changed);
	    BitSet result = EditDistance.showLines(b, changed.length());
	       
	    assertEquals(result + " for " + changed,changed.length(), result.cardinality());
	}
	public void testShowLines() {
	    showAll("101010");
	    showAll("1010101");
	    showAll("0010101");
	    showAll("0010100");
	    String result = getShown("000000000010000000000");
        assertEquals("000000011111110000000", result);
        assertEquals("00000001111111110000000", getShown("00000000001010000000000"));
	}
	public BitSet bitSet(String s) {
	    BitSet result = new BitSet();
	        for(int i = 0; i < s.length(); i++)
	            result.set(i, s.charAt(i) == '1');
	      return result;
	    }
	public String toString(BitSet b, int sz) {
	    StringBuffer result = new StringBuffer();
	    for(int i = 0; i < sz; i++)
	        result.append(b.get(i) ? '1' : '0');
	  
            return result.toString();
        }
	
	List<String> getFile(String name) throws IOException {
	    InputStream in = getClass().getResourceAsStream(name);
	    BufferedReader r = new BufferedReader(new InputStreamReader(in));
	    
	    ArrayList<String> result = new ArrayList<String>();
	    while (true) {
	        String s = r.readLine();
	        if (s == null) {
	            r.close();
	            return result;
	        }
	        result.add(s);
	    }
	}
	
	

}

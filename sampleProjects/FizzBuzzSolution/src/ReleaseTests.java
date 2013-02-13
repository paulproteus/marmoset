import junit.framework.TestCase;


public class ReleaseTests extends TestCase {

    
    public void test3() {
        assertEquals("Fizz", FizzBuzz.fizzBuzz(3));
    }
    public void test5() {
        assertEquals("Buzz", FizzBuzz.fizzBuzz(5));
    }
    public void test12() {
        assertEquals("Fizz", FizzBuzz.fizzBuzz(12));
    }
    public void test50() {
        assertEquals("Buzz", FizzBuzz.fizzBuzz(50));
    }
    public void test15() {
        assertEquals("FizzBuzz", FizzBuzz.fizzBuzz(15));
    }
    public void test30() {
        assertEquals("FizzBuzz", FizzBuzz.fizzBuzz(30));
    }
    
    public void test91() {
        assertEquals("91", FizzBuzz.fizzBuzz(91));
    }
    
    public void testOneToOneHundred() {
        for(int i = 1; i <= 100; i++) 
            assertEquals("Checking " + i, fizzBuzzReference(i), FizzBuzz.fizzBuzz(i));
    }
    
    private static String fizzBuzzReference(int i) {
        boolean multipleOfThree = i % 3 == 0;
        boolean multipleOfFive = i % 5 == 0;
        if (multipleOfThree && multipleOfFive)
            return "FizzBuzz";
        if (multipleOfThree)
            return "Fizz";
        if (multipleOfFive)
            return "Buzz";
        return Integer.toString(i);
    }
}

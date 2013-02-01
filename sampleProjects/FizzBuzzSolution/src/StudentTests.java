import junit.framework.TestCase;


public class StudentTests extends TestCase {

    /**
     * Write your own tests calling assertEquals. The first argument is the
     * value you expect, the second argument is the value you get from the code
     * under test
     */

    /** Test that the result of FizzBuzz.fizzBuzz(3) is "Fizz" */
    public void test3() {
        assertEquals("Fizz", FizzBuzz.fizzBuzz(3));
    }

    public void testSomethingElse() {
        // you should write your own tests

    }
}

/**
 * This is a very simple Java exercise, just to let you begin to understand how
 * Marmoset works, without really testing your programming skills.
 * 
 */
public class Project0 {

    /**
     * return the square of x.
     * 
     * You can just use "return x;" to see what happens when a submission passes
     * some test cases but not others
     */

    public static int square(int x) {

        return x*x;
    }
    
    public static boolean parseBoolean(String s) {
        switch (s.toLowerCase().trim()) {
        case "true" : return true;
        case "false" : return false;
        default: throw new IllegalArgumentException(s);
        }
    }

}

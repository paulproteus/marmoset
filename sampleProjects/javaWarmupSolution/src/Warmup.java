/**
 * A set of static methods you need to implement for this Java warmup.
 * 
 * Do not change the method names or arguments. If you do, the tests will fail.
 * 
 * In each method, replace the "throw new Unsupported..." statement with your
 * implementation. Those throw statements are there to indicate which functions
 * you haven't gotten around to implementing yet.
 * 
 */
public class Warmup {

    /** Return true if x is odd */
    public static boolean isOdd(int x) {
        return x % 2 != 0;
    }

    /**
     * Return the first digit of the base ten representation of x; will only
     * return 0 if x == 0
     */
    public static int firstDigit(int x) {
        while (true) {
            int y = x / 10;
            if (y == 0)
                break;
            x = y;
        }
        if (x < 0)
            return -x;
        return x;
    }

    /** Return the number of distinct values passed as arguments */
    public static int distinctValues(int x, int y, int z) {
        int result = 3;
        if (y == x)
            result--;
        if (z == x || z == y)
            result--;
        return result;
    }

    /** Return the number of 1 bits in the binary representation of x */
    public static int bitCount(int x) {
        int result = 0;
        while (x != 0) {
            if ((x & 1) != 0)
                result++;
            x >>>= 1;
        }
        return result;
    }

}

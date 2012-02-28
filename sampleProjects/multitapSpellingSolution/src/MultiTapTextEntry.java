import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * 
 * From <a href="http://code.google.com/codejam/contest/351101/dashboard#s=p2">
 * Google code jam 2010 qualification round, South Africa</a>
 * 
 * <p>Even before phones with keyboards and touch screens, you could send text on
 * mobile phones. One of the methods used was 
 * <a href="http://en.wikipedia.org/wiki/Multi-tap">multi-tap text entry.</a>
 * 
 * <p>The 2 key is labeled with abc; the 3 key with def; each key 2-9 is labeled
 * with 3 letters, except 7 which is labeled pqrs and 9 which is labeled wxyz.
 * 
 * <p><img src="http://upload.wikimedia.org/wikipedia/commons/thumb/4/43/Telephone-keypad.svg/275px-Telephone-keypad.svg.png"/>
 * 
 * <p>To insert the character E for instance, the program would press 33 (the digit
 * 3 since e appears on the 3 key; twice since it is the second letter listed
 * for 3). In order to insert two characters in sequence from the same key, the
 * user must pause before pressing the key a second time. The space character
 * ' ' should be printed to indicate a pause. For example, 2 2 indicates AA
 * whereas 22 indicates B.
 * 
 * <p>Each message will consist of only lowercase characters a-z and space
 * characters ' '. Pressing zero emits a space.
 * 
 * <p>Note that the Google code jam problem incorrectly describes this as T9 text
 * entry.
 * 
 */
public class MultiTapTextEntry {

    static Map<Character, String> mapping = new HashMap<Character, String>();

    static {

        String[] keyboard = { "", "", "abc", "def", "ghi", "jkl", "mno",
                "pqrs", "tuv", "wxyz" };
        for (int k = 0; k < keyboard.length; k++) {
            String encoding = "";
            String forLetter = keyboard[k];
            for (int i = 0; i < forLetter.length(); i++) {
                encoding += k;
                char c = forLetter.charAt(i);
                mapping.put(c, encoding);
            }
        }

        mapping.put(' ', "0");
    }

    /**
     * Return the multi-tap encoding of a message. Input is only lowercase
     * letters and spaces. Output should be only digits and spaces.
     */
    public static String getMultitapSpelling(String txt) {

        StringBuffer result = new StringBuffer();
        char lastKey = '#';
        for (int i = 0; i < txt.length(); i++) {
            char c = txt.charAt(i);
            String k = mapping.get(c);
            if (lastKey == k.charAt(0))
                result.append(' ');
            result.append(k);
            lastKey = k.charAt(0);

        }
        return result.toString();
    }

    /**
     * Read input and write output using the format described in the Google code
     * jam problem. The first line contains the number of test cases, each
     * following line contains a test case. The output is one line for each test
     * case, with the format
     * 
     * <pre>
     * Case #n: output
     * </pre>
     * 
     * where n is the test case number (starting at 1) and the output is the
     * result of encoding that test case.
     * 
     * If a file is provided on the command line, read that, otherwise read from
     * standard input.
     * 
     * Output is written to standard output.
     */
    public static void main(String args[]) throws Exception {
        Scanner s;
        if (args.length == 0)
            s = new Scanner(System.in);
        else
            s = new Scanner(new File(args[0]));
        int n = s.nextInt();
        s.nextLine(); // skip rest of line
        for (int i = 1; i <= n; i++) {
            String txt = s.nextLine();
            System.out.printf("Case #%d: %s%n", i, getMultitapSpelling(txt));
        }
    }

}

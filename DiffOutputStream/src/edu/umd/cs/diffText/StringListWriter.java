package edu.umd.cs.diffText;
import java.util.ArrayList;
import java.util.List;


public class StringListWriter extends StringsWriter {

    ArrayList<String> result = new ArrayList<String>();
    @Override
    protected void got(int line, String s) {
        result.add(s);
    }
    
    List<String> getStrings() {
        return result;
    }

}

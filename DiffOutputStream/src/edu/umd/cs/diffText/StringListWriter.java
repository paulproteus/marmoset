package edu.umd.cs.diffText;

import java.util.ArrayList;
import java.util.List;

public class StringListWriter extends StringsWriter {

    ArrayList<String> result = new ArrayList<String>();

    @Override
    protected void got(String s) {
        result.add(s);
    }

    public List<String> getStrings() {
        return result;
    }

}

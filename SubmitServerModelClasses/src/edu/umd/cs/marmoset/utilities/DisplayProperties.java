package edu.umd.cs.marmoset.utilities;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.CheckForNull;

public class DisplayProperties {
    
    static public class FileProperties {
        final String s;

        private FileProperties(String s) {
            this.s = s.toLowerCase();
        }
        public boolean isReadonly() {
            return s.contains("readonly");
        }

        public boolean isCollapsed() {
            return s.contains("collapsed");
        }

        public boolean isHidden() {
            return s.contains("hidden");
        }
        static final FileProperties NONE = new FileProperties("");
        static FileProperties build(@CheckForNull String s) {
            if (s == null || s.isEmpty())
                return NONE;
            return new FileProperties(s);
        }
    }

    int nextRank = 0;

    final static int UNRANKED = 1000000;

    Map<String, Integer> rank = new HashMap<String, Integer>();
    Map<String, FileProperties> properties = new HashMap<String, FileProperties>();

    public boolean isEmpty() {
        return rank.isEmpty();
    }

    public void put(String filename) {
        put(filename, null);
    }
    public void put(String filename, @CheckForNull String properties) {
        rank.put(filename, nextRank++);
        this.properties.put(filename,  FileProperties.build(properties));
    }

    public int getRank(String filename) {
        Integer rank = this.rank.get(filename);
        if (rank != null)
            return rank;
        return UNRANKED;
    }

    public Map<String, FileProperties> getProperties() {
        return properties;
    }

   
    public Map<String, List<String>> build(Map<String, List<String>> sourceContents) {
        if (isEmpty())
            return sourceContents;
        TreeMap<String, List<String>> sorted = new TreeMap<String, List<String>>(fileComparator());
        for (Map.Entry<String, List<String>> e : sourceContents.entrySet()) {
            String filename = e.getKey();
            FileProperties p = properties.get(filename);
            if (p == null) {
                System.out.println("Didn't find " + filename + " in " + properties.keySet());
                sorted.put(filename, e.getValue());
                continue;
            }
            if (!p.isHidden())
                sorted.put(filename, e.getValue());
        }
        return sorted;
    }

    public Comparator<String> fileComparator() {
        return new Comparator<String>() {

            @Override
            public int compare(String arg0, String arg1) {
                int result = getRank(arg0) - getRank(arg1);
                if (result != 0)
                    return result;
                return arg0.compareTo(arg1);
            }
        };
    }

}

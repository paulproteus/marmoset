package edu.umd.cs.marmoset.utilities;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        if (filename.endsWith("Tests.java"))
        		return UNRANKED;
        return UNRANKED*2;
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
            if (p == null || !p.isHidden())
                sorted.put(filename, e.getValue());
        }
        
        return sorted;
    }
    
    static int getSuffixStart(String s) {
        int lastSlash = s.lastIndexOf('/');
        int lastDot = s.lastIndexOf('.', lastSlash+1);
        if (lastDot < 0)
            return s.length();
        return lastDot;
    }
    
    static int getSuffixRank(String suffix) {
        if (suffix.startsWith(".h"))
            return 1;
        return 2;
    }

    public Comparator<String> fileComparator() {
        return new Comparator<String>() {

            @Override
            public int compare(String arg0, String arg1) {
                int result = getRank(arg0) - getRank(arg1);
                if (result != 0)
                    return result;
                int i0 = getSuffixStart(arg0);
                int i1 = getSuffixStart(arg1);
                result = arg0.substring(0,i0).compareTo(arg1.substring(0,i1));
                if (result != 0)
                    return result;
                String s0 = arg0.substring(i0).toLowerCase();
                String s1 = arg1.substring(i1).toUpperCase();
                result = getSuffixRank(s0) - getSuffixRank(s1);
                if (result != 0)
                    return result;
               
                return arg0.compareTo(arg1);
            }
        };
    }

}

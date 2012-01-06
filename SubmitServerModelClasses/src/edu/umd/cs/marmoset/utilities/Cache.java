package edu.umd.cs.marmoset.utilities;

import java.util.LinkedHashMap;
import java.util.Map;

public  class Cache <K,V> extends LinkedHashMap<K,V> {
    private static final long serialVersionUID = 1L;

    private static final int CACHE_SIZE = 250;

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() >= CACHE_SIZE;
    }
}
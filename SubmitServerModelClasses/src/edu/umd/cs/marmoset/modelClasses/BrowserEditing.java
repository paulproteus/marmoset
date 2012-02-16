package edu.umd.cs.marmoset.modelClasses;

public enum BrowserEditing {
    PROHIBITED, DISCOURAGED, ALLOWED;
    public static BrowserEditing valueOfAnyCase(String name) {
        return valueOf(name.toUpperCase());
    }
}
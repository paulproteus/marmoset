package edu.umd.cs.marmoset.modelClasses;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;

import edu.umd.cs.marmoset.modelClasses.TestOutcome.TestType;

public class ExecutableTestCase {

    public static Map<String, ExecutableTestCase> parse(
            ScriptTestProperties testProperties) {

        Map<String, ExecutableTestCase> testCases = new LinkedHashMap<String, ExecutableTestCase>();
        Map<TestType, ExecutableTestCase> kindDefaults = new HashMap<TestType, ExecutableTestCase>();
        ExecutableTestCase defaults = new ExecutableTestCase(null, null, null, 0);
        for (TestType testType : TestType.DYNAMIC_TEST_TYPES) {
            Iterable<String> names = testProperties.getTestNames(testType);
            if (names == null)
                continue;
            ExecutableTestCase kindOptions = new ExecutableTestCase(defaults,
                    null, testType.toString(), 0);
            kindDefaults.put(testType, kindOptions);
            int number = 1;
            for (String name : names) {
                ExecutableTestCase options = new ExecutableTestCase(kindOptions,
                        testType, name, number++);
                if (testCases.containsKey(name))
                    throw new IllegalArgumentException(
                            "Can't have two tests named " + name);
                testCases.put(name, options);
            }

        }
        try {
            for (String property : testProperties.getPropertyNames()) {
                if (!property.startsWith("test."))
                    continue;
                String value = testProperties
                        .getRequiredStringProperty(property);
                String parts[] = property.substring(5).trim().split("\\.");
                if (parts[0].equals("default") && parts.length == 2) {
                    defaults.set(parts[1], value);
                } else if (parts.length == 1 && isProperty(parts[0]) ) {
                    defaults.set(parts[0], value);
                } else if (parts[0].equals("cases") && parts.length == 3) {
                    TestType testType = TestType.valueOfAnyCase(parts[1]);
                    if (!kindDefaults.containsKey(testType))
                        throw new IllegalArgumentException(
                                "No tests definded for " + testType
                                        + ",  can't define " + property);
                    kindDefaults.get(testType).set(parts[2], value);
                } else if (parts[0].equals("case") && parts.length == 3) {
                    String name = parts[1];
                    if (!testCases.containsKey(name))
                        throw new IllegalArgumentException(
                                "No test definded for " + name
                                        + ",  can't define " + property);
                    testCases.get(name).set(parts[2], value);
                }

            }
        } catch (MissingRequiredTestPropertyException e) {
            throw new AssertionError(e);
        }
        return testCases;
    }

    public enum Property {
        EXEC, REFERENCE_EXEC, OPTIONS, INPUT, EXPECTED, POINTS;
        public static Property valueOfAnyCase(String name) {
            name = name.toUpperCase();
            if (name.equals("REFERENCEEXEC"))
                return REFERENCE_EXEC;
            return valueOf(name);
        }
    };

    public enum InputKind {
        NONE, STRING, FILE;
        public boolean hasValue() {
            return this == STRING || this == FILE;
        }
    };

    public enum OutputKind {
        NONE, STRING, FILE, REFERENCE_IMPL;
        public boolean hasValue() {
            return this == STRING || this == FILE;
        }
    };

    final @CheckForNull
    ExecutableTestCase defaults; // null only for defaults
    final TestType testType; // nonnull only for leaf test cases
    final String name;
    final int number;

    private EnumMap<Property, String> properties = new EnumMap<Property, String>(
            Property.class);

    private  InputKind inputKind;
    private  OutputKind outputKind;

    private ExecutableTestCase(ExecutableTestCase defaults, TestType testType,
            String name, int number) {
        this.defaults = defaults;
        this.testType = testType;
        this.name = name;
        this.number = number;
    }

    public boolean isLeafTestCase() {
        return testType != null;
    }
    public TestType getTestType() {
        return testType;
    }

    public String getName() {
        return name;
    }
    
    public int getNumber() {
        return number;
    }
    
    public InputKind getInputKind() {
        InputKind result = inputKind;
        if (result != null)
            return result;
        if (defaults != null)
            result = defaults.getInputKind();
        if (result != null) return result;
        if (isLeafTestCase())
            return InputKind.NONE;
        return null;
    }

    public OutputKind getOutputKind() {
        OutputKind result = outputKind;
        if (result != null)
            return result;
        if (defaults != null)
            result = defaults.getOutputKind();
        if (result != null) return result;
        if (isLeafTestCase())
            return OutputKind.NONE;
        return null;
    }

    public String getProperty0(Property p) {
        if (properties.containsKey(p))
            return properties.get(p);
        if (defaults != null)
            return defaults.getProperty0(p);
        return null;
    }
    public String getProperty(Property p) {
        String value = getProperty0(p);
        if (value == null && isLeafTestCase() && p == Property.EXEC)
            return name;
        if (value != null && value.contains("&"))
            value = value.replaceAll("&", name);
        return value;
    }
    
    public String [] getStrings(Property p) {
        String value = getProperty(p);
        if (value == null)
            throw new NoSuchElementException("No property " + p.toString());
        if (value.contains("\n") || value.contains("\r"))
            throw new IllegalStateException("property " + p + " may not contain newlines");
        return value.split("\\s+");

    }

   static boolean isProperty(String key) {
        key = key.trim();
        try {
            Property.valueOfAnyCase(key);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    void set(String key, String value) {
        key = key.trim();
        value = value.trim();
        Property p = Property.valueOfAnyCase(key);
        if (properties.containsKey(p))
            throw new IllegalArgumentException("Property " + value
                    + " already set");
        switch (p) {
        case INPUT:
            if (isQuotedString(value)) {
                value = stripQuotes(value);
                inputKind = InputKind.STRING;
            } else {
                inputKind = InputKind.FILE;
            }
            break;
        case EXPECTED:
            if (outputKind != null)
                throw new IllegalArgumentException("Can't set both output of  "
                        + value + " and a reference exec");
            if (isQuotedString(value)) {
                value = stripQuotes(value);
                outputKind = OutputKind.STRING;
            } else {
                outputKind = OutputKind.FILE;
            }
            break;
        case REFERENCE_EXEC:
            if (outputKind != null)
                throw new IllegalArgumentException("Can't set both output of  "
                        + outputKind + " and a reference exec");
            outputKind = OutputKind.REFERENCE_IMPL;
            break;
        default:
            assert true;

        }
        properties.put(p, value);

    }

    private boolean isQuotedString(String text) {
        int length = text.length();
        return length >= 2 && text.charAt(0) == '"'
                && text.charAt(length - 1) == '"';
    }

    private String stripQuotes(String text) {
        int length = text.length();
        return text.substring(1, length - 1);
    }

}

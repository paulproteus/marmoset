/**
 * Marmoset: a student project snapshot, submission, testing and code review
 * system developed by the Univ. of Maryland, College Park
 * 
 * Developed as part of Jaime Spacco's Ph.D. thesis work, continuing effort led
 * by William Pugh. See http://marmoset.cs.umd.edu/
 * 
 * Copyright 2005 - 2011, Univ. of Maryland
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

/**
 * Created on Jan 22, 2008
 *
 * @author langmead
 */

package edu.umd.cs.marmoset.utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class for storing and using some useful patterns for
 * validating input, especially HTTP request parameters.  I hope opted
 * to use broad regexes, addressing big categories of inputs well
 * enough to ensure they're hijinx-free.  I have not attempted to fit
 * the regexes all that tightly, on the assumption that this would not
 * be worth the extra work and maintenance.
 */
public final class MarmosetPatterns {

    private static final Map<String,Pattern> paramNameToPattern = new HashMap<String,Pattern>();

    /**
     * Add parameter-name/pattern pair to the private static map.
     * Check for duplicates.
     * @param s parameter name
     * @param p regex pattern
     */
    private static void addPattern(String s, Pattern p) {
        if(paramNameToPattern.containsKey(s)) {
            throw new Error("Already contains: " + s);
        }
        paramNameToPattern.put(s, p);
    }

    private final static Pattern tableName = Pattern.compile("[A-Za-z0-9_-]+");
    private final static Pattern attrName = Pattern.compile("[A-Za-z0-9._-]+");
    private final static Pattern attrList = Pattern.compile("[A-Za-z0-9,. _-]+");
    private final static Pattern integer = Pattern.compile("[0-9]+");
    private final static Pattern number = Pattern.compile("[0-9.eE+-]+");
    private final static Pattern numberOrEmpty = Pattern.compile("[0-9.eE+-]*");
    private final static Pattern digits = Pattern.compile("[0-9]*");
    private final static Pattern identifier = Pattern.compile("[$A-Za-z0-9._-]+");
    private final static Pattern identifierOrEmpty = Pattern.compile("[$A-Za-z0-9._-]*");
    private final static Pattern alphaNumeric = Pattern.compile("[A-Za-z0-9]*");
    private final static Pattern section = Pattern.compile("[A-Za-z0-9-]*");
    private final static Pattern identifierWithSpaces = Pattern.compile("[$A-Za-z0-9. _-]+");
    private final static Pattern identifierWithSpacesOrEmpty = Pattern.compile("[$A-Za-z0-9. _-]*");
    private final static Pattern filenameOrEmpty = Pattern.compile("[$:/\\\\A-Za-z0-9. _-]*");
    private final static Pattern email = Pattern.compile("[$A-Za-z0-9._+@-]+");
    private final static Pattern emailOrEmpty = Pattern.compile("[$A-Za-z0-9._+@-]*");
    private final static Pattern urlEncoded = Pattern.compile("[A-Za-z0-9.\\-*_+%]*");

    static {
        // HTTP parameters with no associated Regex
        addPattern("action", null);
        addPattern("age", null);
        addPattern("comment", null);
        addPattern("commitCvstag", null);
        addPattern("courseName", null);
        addPattern("courses", null);
        addPattern("classAccount", null);
        addPattern("cvsTagTimestamp", null);
        addPattern("description", null);
        addPattern("emailAddress", null);
        addPattern("firstname", null);
        addPattern("gender", null);
        addPattern("highSchoolCountry", null);
        addPattern("jarfileStatus", null);
        addPattern("lastname", null);
        addPattern("longTestResult", null);
        addPattern("oneTimePassword", null);
        addPattern("permissionType", null);
        addPattern("priority", null);
        addPattern("projectNumber", null);
        addPattern("semester", null);
        addPattern("shortTestResult", null);
        addPattern("submitClientTool", null);
        addPattern("submitClientVersion", null);
        addPattern("target", urlEncoded);
        addPattern("testName", null);
        addPattern("title", null);
        addPattern("url", null);
        addPattern("warningName", null);

        // Background data
        addPattern("aExamScore", null);
        addPattern("abExamScore", null);
        addPattern("AmericanIndian", null);
        addPattern("Asian", null);
        addPattern("Black", null);
        addPattern("Caucasian", null);
        addPattern("communityCollege", null);
        addPattern("LatinoLatina", null);
        addPattern("major", null);
        addPattern("EthnicRacialAssociation", null);
        addPattern("otherUMInstitution", null);
        addPattern("otherNonUMInstitution", null);
        addPattern("placementExam", null);
        addPattern("placementExamResult", null);
        addPattern("priorProgrammingExperience", null);

        // Parameters that have associated regular expressions
        addPattern("accountType", identifierOrEmpty);
        addPattern("authenticateType", identifierOrEmpty);
        addPattern("bestSubmissionPolicy", identifierOrEmpty);
        addPattern("loginName", identifierOrEmpty);
        addPattern("buildStatus", identifierOrEmpty);
        addPattern("campusUID", numberOrEmpty);
        addPattern("hybridTestType", identifierOrEmpty);
        addPattern("initialBuildStatus", identifier);
        addPattern("kindOfLatePenalty", identifier);
        addPattern("lateMultiplier", numberOrEmpty);
        addPattern("newPostDeadlineOutcomeVisibility", identifierOrEmpty);
        addPattern("optionalQuality", identifierOrEmpty);
        addPattern("postDeadlineOutcomeVisibility", identifierOrEmpty);
        addPattern("previousInitialBuildStatus", identifierOrEmpty);
        addPattern("releasePolicy", identifierOrEmpty);
        addPattern("sortKey", identifierOrEmpty);
        addPattern("sourceFileName", filenameOrEmpty);
        addPattern("stackTracePolicy", identifier);
        addPattern("term", integer);
        addPattern("testType", identifierOrEmpty);
        addPattern("section", section);
    }

    public static Pattern getPattern(String key) {
        return paramNameToPattern.get(key);
    }

    public static boolean isTableName(String s) {
        return tableName.matcher(s).matches();
    }

    public static Pattern tableName() {
        return tableName;
    }

    public static boolean isAttributeName(String s) {
        return attrName.matcher(s).matches();
    }

    public static Pattern attributeName() {
        return attrName;
    }

    public static boolean isAttributeList(String s) {
        return attrList.matcher(s).matches();
    }

    public static Pattern attributeList() {
        return attrList;
    }

    public static boolean isIdentifier(String s) {
        return identifier.matcher(s).matches();
    }

    public static Pattern identifier() {
        return identifier;
    }

    public static boolean isIdentifierOrEmpty(String s) {
        return identifierOrEmpty.matcher(s).matches();
    }

    public static Pattern identifierOrEmpty() {
        return identifierOrEmpty;
    }

    public static boolean isIdentifierWithSpaces(String s) {
        return identifierWithSpaces.matcher(s).matches();
    }

    public static Pattern identifierWithSpaces() {
        return identifierWithSpaces;
    }

    public static boolean isIdentifierWithSpacesOrEmpty(String s) {
        return identifierWithSpacesOrEmpty.matcher(s).matches();
    }

    public static Pattern identifierWithSpacesOrEmpty() {
        return identifierWithSpacesOrEmpty;
    }

    public static boolean isNumber(String s) {
        return number.matcher(s).matches();
    }

    public static Pattern number() {
        return number;
    }

    public static boolean isNumberOrEmpty(String s) {
        return numberOrEmpty.matcher(s).matches();
    }

    public static Pattern numberOrEmpty() {
        return numberOrEmpty;
    }

    public static boolean isFilenameOrEmpty(String s) {
        return filenameOrEmpty.matcher(s).matches();
    }

    public static Pattern filenameOrEmpty() {
        return filenameOrEmpty;
    }
}

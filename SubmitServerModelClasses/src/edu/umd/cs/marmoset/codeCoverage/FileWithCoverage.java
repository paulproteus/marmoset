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

package edu.umd.cs.marmoset.codeCoverage;

import static edu.umd.cs.marmoset.codeCoverage.CodeCoverageConstants.COND;
import static edu.umd.cs.marmoset.codeCoverage.CodeCoverageConstants.COUNT;
import static edu.umd.cs.marmoset.codeCoverage.CodeCoverageConstants.FALSE_COUNT;
import static edu.umd.cs.marmoset.codeCoverage.CodeCoverageConstants.METHOD;
import static edu.umd.cs.marmoset.codeCoverage.CodeCoverageConstants.NUM;
import static edu.umd.cs.marmoset.codeCoverage.CodeCoverageConstants.STMT;
import static edu.umd.cs.marmoset.codeCoverage.CodeCoverageConstants.TRUE_COUNT;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dom4j.Element;

/**
 * Stores code coverage information for a particular file. Tracks overall
 * coverage stats such as percent of methods, statements and conditionals
 * covered.
 * <p>
 * This code is ill-suited to handle partial line coverage, such as lines that
 * short-circuit, constructors or static initializers that only partially run,
 * etc. TODO extract an interface for FileWithCoverage
 *
 * @author jspacco
 *
 */
/**
 * @author jspacco
 *
 */
public class FileWithCoverage {
	/** The name of the file. */
	private final String shortName;

	private boolean anythingCovered = false;

	/** The full pathname of the file. */
	private final String fullPathName;

	private Map<Integer, Stmt> stmtMap;

	private Map<Integer, Method> methodMap;

	private Map<Integer, Cond> condMap;

	/**
	 * Constructs a new FileWithCoverage object for a file with the given name.
	 *
	 * @param name
	 *            The name of the source file for which this coverage
	 *            information was recorded.
	 */
	public FileWithCoverage(String name) {
		int index = name.lastIndexOf('/');
		if (index != -1) {
			this.shortName = name.substring(index + 1);
		} else
			this.shortName = name;

		this.fullPathName = name;
		// this.coveredLineMap = new HashMap<Integer, Line>();
		this.stmtMap = new HashMap<Integer, Stmt>();
		this.methodMap = new HashMap<Integer, Method>();
		this.condMap = new HashMap<Integer, Cond>();
	}

	/**
	 * Copy constructor. Makes a deep copy of the map of covered lines, but does
	 * not copy the individual lines because they are immutable.
	 *
	 * @param other
	 *            The other FileWithCoverage whose line coverage information
	 *            will be used for the new FileWithCoverage.
	 */
	public FileWithCoverage(FileWithCoverage other) {
		this(other.fullPathName);
		for (Stmt stmt : other.stmtMap.values()) {
			addLine(stmt);
		}
		for (Method method : other.methodMap.values()) {
			addLine(method);
		}
		for (Cond cond : other.condMap.values()) {
			addLine(cond);
		}
	}

	/**
	 * Returns the coverage stats. Right now this is computed on-the-fly while
	 * adding lines to a set of coverage information. However, we could compute
	 * this information as necessary.
	 *
	 * @return The coverage stats.
	 */
	public CoverageStats getCoverageStats() {
		CoverageStats result = new CoverageStats();
		for (Stmt stmt : stmtMap.values()) {
			result.addNewLine(stmt);
		}
		for (Method method : methodMap.values()) {
			result.addNewLine(method);
		}
		for (Cond cond : condMap.values()) {
			result.addNewLine(cond);
		}
		return result;
	}

	/**
	 * Adds a line object to the set of covered lines. Each Line coverage
	 * information object is mapped to its corresponding line number.
	 *
	 * @param line
	 *            The line object to be added.
	 */
	private void addLine(Method line) {
		if (line.getCount() > 0) anythingCovered = true;
		methodMap.put(line.getLineNumber(), line);
	}

	private void addLine(Cond line) {
		if (line.getTrueCount() > 0 || line.getFalseCount() > 0) anythingCovered = true;
		condMap.put(line.getLineNumber(), line);
	}

	private void addLine(Stmt line) {
		if (line.getCount() > 0) anythingCovered = true;
		stmtMap.put(line.getLineNumber(), line);
	}

	/**
	 * Processes an XML Element of type "line", producing a line coverage object
	 * which is then mapped to its corresponding line number.
	 *
	 * @param lineElement
	 *            The XML Element to be processed.
	 */
	void processLineNode(Element lineElement) {
		String type = lineElement.attributeValue(CodeCoverageConstants.TYPE);
		if (STMT.equals(type)) {
			// statement

			// System.out.println(STMT);
			int num = getIntAttribute(lineElement, NUM);
			int count = getIntAttribute(lineElement, COUNT);
			Stmt stmt = new Stmt(num, count);
			addLine(stmt);
		} else if (COND.equals(type)) {
			// conditional

			// System.out.println(COND);
			int num = getIntAttribute(lineElement, NUM);
			int truecount = getIntAttribute(lineElement, TRUE_COUNT);
			int falsecount = getIntAttribute(lineElement, FALSE_COUNT);
			Cond cond = new Cond(num, truecount, falsecount);
			addLine(cond);
		} else if (METHOD.equals(type)) {
			// method

			// System.out.println(METHOD);
			int num = getIntAttribute(lineElement, NUM);
			int count = getIntAttribute(lineElement, COUNT);
			Method method = new Method(num, count);
			addLine(method);
		} else {
			System.err.println("Unrecognized type: " + type);
		}
	}

	/**
	 * Retrieves the number of times a particular line number was covered.
	 * Returns -1 if the line was not instrumented to record coverage
	 * information (i.e. for non-executable statements such as comments, import
	 * statements, package declaractions, blank lines, etc).
	 *
	 * @param lineNumber
	 *            The line number.
	 * @return The number of times a particular line number was covered, or -1
	 *         if the line was not instrumented to record coverage information.
	 */
	public int getStmtCoverageCount(int num) {
		Integer lineNumber = num;
		Line<?> stmt = stmtMap.get(lineNumber);
		if (stmt != null)
			return stmt.getCount();
		// If it's not a stmt, then it must be a method
		Line<?> method = methodMap.get(lineNumber);
		if (method != null)
			return method.getCount();
		// If it's not a stmt or a method, the line doesn't exist, and we return
		// -1
		return -1;
	}

	public boolean isAnythingCovered() {
		return anythingCovered;
	}
	public  Line<?> getCoverage(int num) {
		Integer lineNumber = num;
		Line<?> line = condMap.get(lineNumber);
		if (line != null) return line;
		line =  stmtMap.get(lineNumber);
		if (line != null) return line;
		return methodMap.get(lineNumber);
	}
	public boolean isLineCovered(int num) {
		Line<?> line = getCoverage(num);
		if (line == null) return false;
		return line.isCovered();
	}

	/**
	 * Return the name of the source file this coverage represents.
	 *
	 * @return The name of the file.
	 */
	public String getShortFileName() {
		return shortName;
	}

	public String getFullPathName() {
		return fullPathName;
	}

	private static <T extends Line<T> > void union(T line, Map<Integer, T> map) {
		if (!map.containsKey(line.getIntegerLineNumber())) {
			// Lacking that line, so add it.
			map.put(line.getIntegerLineNumber(), line);
		} else {
			// We have coverage information for that line, so merge them.
			T thisLine = map.get(line.getIntegerLineNumber());
			T union = thisLine.union(line);
			map.put(line.getIntegerLineNumber(), union);
		}
	}

	private <T extends Line<T> > void intersect(T line, Map<Integer, T> map) {
		if (!map.containsKey(line.getIntegerLineNumber())) {
			// Lacking that line, so add it.
			map.put(line.getIntegerLineNumber(), line);
		} else {
			T thisLine = map.get(line.getIntegerLineNumber());
			T intersect = thisLine.intersect(line);
			map.put(line.getIntegerLineNumber(), intersect);
		}
	}

	private <T extends Line<T> > void excluding(T line, Map<Integer, T> map) {
		if (!map.containsKey(line.getIntegerLineNumber())) {
			// Lacking that line, so add it.
			map.put(line.getIntegerLineNumber(), line);
		} else {
			T thisLine = map.get(line.getIntegerLineNumber());
			T excluding = thisLine.excluding(line);
			map.put(line.getIntegerLineNumber(), excluding);
		}
	}

	/**
	 * Merge coverage information with another set of coverage information.
	 * Basically adds together all the lines.
	 *
	 * @param other
	 *            The other coverage file to merge with.
	 */
	void union(FileWithCoverage other) {
		// Merge the stmts
		for (Stmt stmt : other.stmtMap.values()) {
			union(stmt, stmtMap);
		}
		// Merge the methods
		for (Method method : other.methodMap.values()) {
			union(method, methodMap);
		}
		// Merge the conds
		for (Cond cond : other.condMap.values()) {
			union(cond, condMap);
		}
	}

	void intersect(FileWithCoverage other) {
		// Merge the stmts
		for (Stmt stmt : other.stmtMap.values()) {
			intersect(stmt, stmtMap);
		}
		// Merge the methods
		for (Method method : other.methodMap.values()) {
			intersect(method, methodMap);
		}
		// Merge the conds
		for (Cond cond : other.condMap.values()) {
			intersect(cond, condMap);
		}
	}

	void excluding(FileWithCoverage other) {
		// Merge the stmts
		for (Stmt stmt : other.stmtMap.values()) {
			excluding(stmt, stmtMap);
		}
		// Merge the methods
		for (Method method : other.methodMap.values()) {
			excluding(method, methodMap);
		}
		// Merge the conds
		for (Cond cond : other.condMap.values()) {
			excluding(cond, condMap);
		}
	}

	private static int getIntAttribute(Element lineElement, String name) {
		return Integer.parseInt(lineElement.attributeValue(name));
	}

	public CoverageLevel coarsestCoverage() {
		for (Method method : methodMap.values())
			if (method.isCovered()) return CoverageLevel.METHOD;
        for (Cond cond : condMap.values())
            if (cond.isCovered()) return CoverageLevel.BRANCH;
        for (Stmt stmt : stmtMap.values())
			if (stmt.isCovered()) return CoverageLevel.STATEMENT;

		return CoverageLevel.NONE;

	}

    public boolean isLineCoveredByMethod(int lineNumber)
    {
        Method method=methodMap.get(lineNumber);
        if (method!=null)
            return method.isCovered();
        return false;
    }

    public SortedSet<Integer> getUncoveredMethods() {
    		TreeSet<Integer> result = new TreeSet<Integer>();
    		for(Map.Entry<Integer, Method> e : methodMap.entrySet()) {
    			if (!e.getValue().isCovered())
    				result.add(e.getKey());
    		}
    		return result;
    }
}

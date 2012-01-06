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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * TODO instantiate with a factory
 * 
 * Right now we only support Clover. In theory someday we might support other
 * code coverage tools like Emma.
 * 
 * @author jspacco
 */
public class CodeCoverageResults implements CodeCoverageConstants, Iterable<FileWithCoverage> {
	/**
	 * Map from filenames to FileWithCoverage objects.
	 */
	private Map<String, FileWithCoverage> filesCovered = new HashMap<String, FileWithCoverage>();

	private static List<String> filesToSkip = new LinkedList<String>();
	
	private boolean excludingResult = false;
	
	public boolean isExcludingResult() {
		return excludingResult;
	}

	static {
		// XXX hack alert: static initializers aren't the best option;
		// would be nice to get this info from web.xml or a -D property
		filesToSkip.add("PublicTests.java");
		filesToSkip.add("ReleaseTests.java");
		filesToSkip.add("SecretTests.java");
		filesToSkip.add("StudentTests.java");
		filesToSkip.add("StudentWrittenTests.java");
		filesToSkip.add("TestingSupport.java");
        filesToSkip.add("SpiderTest.java");
        filesToSkip.add("TextUI.java");
	}
	

	public static void main(String[] args) throws Exception {
		CodeCoverageResults cloverXMLOutput = parseFile("/tmp/APROJECTS/cloverTest/clover.xml");
		File file = new File(
				"/tmp/APROJECTS/cloverTest/BuildServer16293/build/src/PokerHandEvaluator.java");
		// cloverXMLOutput.equals(false);
		file.exists();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cs.submitServer.codeCoverage.CodeCoverageResults#getFileWithCoverage(java.lang.String)
	 */
	public FileWithCoverage getFileWithCoverage(String filename) {
		for (String key : filesCovered.keySet()) {
		    // We want to be able to look for either just the filename (Foo.java)
            // or the entire package, possibly with a "src" directory prefix (src/foo/bar/Foo.java)
            // But we don't want searches for "Tree.java" to match against "EmptryTree.java"
            int lastIndexOf=key.lastIndexOf('/');
            if (lastIndexOf>0 && key.endsWith("/"+filename))
                return filesCovered.get(key);
            else if (key.equals(filename))
                return filesCovered.get(key);
        }
		return filesCovered.get(filename);
	}

	/**
	 * Get the overall coverage stats for this set of coverage results.
	 * 
	 * @return The overall coverageStats for this set of coverage results.
	 */
	public CoverageStats getOverallCoverageStats() {
		CoverageStats stats = new CoverageStats();
		stats.setExcludingResult(excludingResult);
		for (FileWithCoverage file : this) {
			// XXX HACK ALERT: I want to skip PublicTests.java,
			// ReleaseTests.java,
			// SecretTests.java and StudentTests.java. I'm guessing based on the
			// name of the file.
			//if (isJUnitTestSuite(file.getFullPathName()) || !file.isAnythingCovered())
            if (isJUnitTestSuite(file.getFullPathName()))
				continue;
			stats.merge(file.getCoverageStats());
		}
		return stats;
	}

	public CodeCoverageResults getCodeCoverageResultsForPackage(String packageName) {
		CodeCoverageResults results = new CodeCoverageResults();
		for (FileWithCoverage file : this) {
			if (file.getFullPathName().contains(packageName)) {
				results.addFileWithCoverage(file);
			}
		}
		return results;
	}

	public CoverageStats getOverallCoverageStatsForPackage(String packageName) {
		CoverageStats stats = new CoverageStats();
		for (FileWithCoverage file : this) {
			if (file.getFullPathName().contains(packageName)) {
				stats.merge(file.getCoverageStats());
			}
		}
		return stats;
	}

	/**
	 * Checks if this file appears to be a JUnitTestSuite based on its name.
	 * 
	 * @param fullPathName
	 *            The name of the file.
	 * @return True if the file appears to be a JUnitTestSuite; false otherwise.
	 */
	public static boolean isJUnitTestSuite(String fullPathName) {
		for (String pattern : filesToSkip) {
			if (fullPathName.contains(pattern))
				return true;
		}
		return false;
	}

	/**
	 * Constructs a new CloverXMLOutput object.
	 */
	public CodeCoverageResults() {
	}

	/**
	 * Copy Constructor. Creates a deep copy.
	 * 
	 * @param other
	 *            The other set of coverage results to copy.
	 */
	public CodeCoverageResults(CodeCoverageResults other) {
		if (other != null) {
			for (FileWithCoverage file : other) {
				addFileWithCoverage(new FileWithCoverage(file));
			}
		}
	}

	private void addFileWithCoverage(FileWithCoverage fileWithCoverage) {
		filesCovered.put(fileWithCoverage.getShortFileName(), fileWithCoverage);
	}

	public static CodeCoverageResults parseFile(String filename) throws IOException,
			DocumentException {
		File file = new File(filename);

		FileInputStream fis = new FileInputStream(file);

		SAXReader reader = new SAXReader();
		Document document = reader.read(fis);
		// should not throw an exception
		fis.close();
		return parseDocument(document);
	}

	public static CodeCoverageResults parseString(String xmlDocumentAsString) throws IOException,
			DocumentException {
		StringReader stringReader = new StringReader(xmlDocumentAsString);
		SAXReader reader = new SAXReader();
		Document document = reader.read(stringReader);
		// Closing a stringReader can't possibly throw an exception
		stringReader.close();
		return parseDocument(document);
	}

	private static CodeCoverageResults parseDocument(Document document) throws DocumentException {
		CodeCoverageResults cloverXMLOutput = new CodeCoverageResults();

		Element root = document.getRootElement();

		for (Iterator<?> ii = root.elementIterator(CodeCoverageConstants.PROJECT); ii.hasNext();) {
			Element projectElement = (Element) ii.next();
			for (Iterator<?> jj = projectElement.elementIterator(CodeCoverageConstants.PACKAGE); jj
					.hasNext();) {
				Element packageElement = (Element) jj.next();
				for (Iterator<?> kk = packageElement.elementIterator(CodeCoverageConstants.FILE); kk
						.hasNext();) {
					Element fileElement = (Element) kk.next();
					String sourceFileName = fileElement.attributeValue(CodeCoverageConstants.NAME);

					// Create a new source file that stores coverage
					// information.
					FileWithCoverage fileWithCoverage = new FileWithCoverage(sourceFileName);
					// System.out.println("attributes: "
					// +fileElement.attributeCount());

					for (Iterator<?> ll = fileElement.elementIterator(CodeCoverageConstants.LINE); ll
							.hasNext();) {
						Element lineElement = (Element) ll.next();
						// System.out.println("Name: " +node.getName());

						// Process the coverage information from this line node.
						fileWithCoverage.processLineNode(lineElement);
					}
					// add the file with its coverage information
					cloverXMLOutput.addFileWithCoverage(fileWithCoverage);
				}
			}
		}
		return cloverXMLOutput;
	}

	public void union(CodeCoverageResults other) {
		if (other.size() == 0) {
			return;
		}
		if (filesCovered.isEmpty()) {
			// If we lack code coverage information, perform a deep copy.
			for (FileWithCoverage file : other) {
				addFileWithCoverage(new FileWithCoverage(file));
			}
		} else {
			// Otherwise compute the union of each file.
			for (FileWithCoverage myFile : filesCovered.values()) {

				FileWithCoverage secondFile = other.getFileWithCoverage(myFile.getShortFileName());
				myFile.union(secondFile);
			}
		}
	}

	public void intersect(CodeCoverageResults other) {
		if (other.size() == 0) {
			filesCovered.clear();
		} else if (filesCovered.isEmpty()) {
			return;
		} else {
			// Otherwise compute the intersection for each file.
			for (FileWithCoverage myFile : filesCovered.values()) {
				FileWithCoverage secondFile = other.getFileWithCoverage(myFile.getShortFileName());
				myFile.intersect(secondFile);
			}
		}
	}

	public void excluding(CodeCoverageResults other) {
		excludingResult = true;
		if (other.size() == 0)
			return;
		if (filesCovered.isEmpty()) {
			return;
		} else {
			// Otherwise compute the intersection for each file.
			for (FileWithCoverage myFile : filesCovered.values()) {
				FileWithCoverage secondFile = other.getFileWithCoverage(myFile.getShortFileName());
				myFile.excluding(secondFile);
			}
		}
	}

	public CoverageLevel coarsestCoverageLevel() {
		CoverageLevel lvl = CoverageLevel.NONE;
		
		for (FileWithCoverage myFile : filesCovered.values()) 
			lvl = lvl.min(myFile.coarsestCoverage());
		return lvl;
	}

	@Override
	public Iterator<FileWithCoverage> iterator() {
		return filesCovered.values().iterator();
	}

	public int size() {
		return filesCovered.size();
	}
    
    /**
     * Does this set of codeCoverage results cover the given file at the given
     * line number?
     * TODO FileNames in CodeCoverageResults are tricky; are they always prepended
     *      with package names, how flexible is the lookup system for detecting the
     *      absence of a package name, etc.
     * @param fileName The name of the file.
     * @param lineNumber The line number of the file.
     * @return True if this set of code coverage results covers the given file at the
     *      given line number; false otherwise.  Returns false if the file is not covered
     *      by these coverage results.
     */
    public boolean coversFileAtLineNumber(String fileName, int lineNumber)
    {
        FileWithCoverage file = getFileWithCoverage(fileName);
        if (file!=null)
            return file.isLineCovered(lineNumber);
        // If for some reason we don't have coverage information for this file,
        // we will simply return false
        return false;
    }
}

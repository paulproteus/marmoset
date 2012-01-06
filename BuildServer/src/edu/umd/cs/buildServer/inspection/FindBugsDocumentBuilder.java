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

/*
 * Created on April 18, 2005
 */
package edu.umd.cs.buildServer.inspection;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.dom4j.io.SAXWriter;
import org.xml.sax.SAXException;

import edu.umd.cs.buildServer.XMLDocumentBuilder;
import edu.umd.cs.findbugs.FindBugs;
import edu.umd.cs.findbugs.SAXBugCollectionHandler;
import edu.umd.cs.findbugs.SortedBugCollection;

/**
 * Output monitor thread which builds a FindBugs BugCollection from an XML
 * document.
 *
 * @author David Hovemeyer
 */
class FindBugsDocumentBuilder extends XMLDocumentBuilder {

	static {
		FindBugs.setNoAnalysis();
	}
	private final SortedBugCollection bugCollection;

	/**
	 * Constructor.
	 *
	 * @param in
	 *            InputStream to read XML document from
	 * @param log
	 *            Log where diagnostic messages are sent
	 */
	public FindBugsDocumentBuilder(InputStream in, Logger log) {
		super(in, log);
		bugCollection = new SortedBugCollection();
	}

	@Override
	protected void documentFinished() {
		try {
			// Generate a BugCollection from the dom4j tree
			SAXBugCollectionHandler handler = new SAXBugCollectionHandler(
					bugCollection);
			SAXWriter saxWriter = new SAXWriter(handler);
			saxWriter.write(getDocument());
		} catch (SAXException e) {
			getLog().info(
					"Couldn't generate BugCollection from findbugs XML output",
					e);
		}
	}

	public SortedBugCollection getBugCollection() {
		return bugCollection;
	}


}

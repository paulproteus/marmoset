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
 * @author jspacco
 */
package edu.umd.cs.marmoset.modelClasses;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author jspacco
 * @deprecated Instead use the Submission.lookupAll...() methods that return List&lt;Submission&gt;
 */
@Deprecated
public class SubmissionCollection
{
	// TODO this class is used in 2 different ways:
    // as a collection of all student submissions for a project, and as a collection
    // of all student submissions for a particular student
    // I should make subclasses for either case
    protected List<Submission> submissions;
	
	/**
	 * Constructor. 
	 */
	public SubmissionCollection()
	{
		submissions = new ArrayList<Submission>();
	}
	
	/**
	 * Gets the number of submissions in this collection.
	 * 
	 * @return
	 */
	public int size()
	{
		return submissions.size();
	}
	
	public boolean isEmpty()
	{
	    return submissions.isEmpty();
	}
	
	public void add(Submission submission)
	{
		submissions.add(submission);
	}
	
	public Submission get(int index)
	{
	    return submissions.get(index);
	}
	
	public List<Submission> getCollection()
	{
		return submissions;
	}
	
	public Iterator<Submission> iterator()
	{
		return submissions.iterator();
	}
	
	public ListIterator<Submission> listIterator()
	{
	    return submissions.listIterator();
	}
	
	public ListIterator<Submission> listIterator(int index)
	{
	    return submissions.listIterator(index);
	}
}

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
 * Created on Jan 23, 2004
 */
package edu.umd.cs.eclipse.courseProjectManager;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

/**
 * The AutoCVSLogView displays a log of AutoCVS actions and diagnostics.
 * 
 * @author David Hovemeyer
 */
public class AutoCVSLogView extends ViewPart {
	private Text text;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		text = new Text(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		text.setEditable(false);
		text.append("Course Project Manager messages\n");

		// Append any existing events
		List<Event> oldEvents = AutoCVSPlugin.getPlugin().getEventLog()
				.getAllEvents();
		for (Iterator<Event> i = oldEvents.iterator(); i.hasNext();) {
			Event element = i.next();
			appendEvent(element);
		}
		AutoCVSPlugin.getPlugin().getEventLog().purge();

		// Install a listener for log events
		EventLog.Listener listener = new EventLog.Listener() {
			public void logEvent(final Event event) {
				// We don't know where we're going to get called
				// from, so update the text control using asyncExec().
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						// Show the event in the text control
						appendEvent(event);

						// Clean up old events
						AutoCVSPlugin.getPlugin().getEventLog().purge();
					}
				});
			}
		};
		AutoCVSPlugin.getPlugin().getEventLog().addListener(listener);
	}

	/**
	 * Append messages for an event to the text control.
	 * 
	 * @param event
	 *            the event
	 */
	private void appendEvent(Event event) {
		text.append("[" + event.getDate().toString() + "] ");
		if (event.isError())
			text.append("Error: ");
		text.append(event.getMessage() + "\n");
		Throwable e = event.getException();
		if (e != null) {
			text.append(e.toString());
			StackTraceElement[] traceList = e.getStackTrace();
			for (int i = 0; i < traceList.length; i++) {
				text.append("\t" + traceList[i].toString() + "\n");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}

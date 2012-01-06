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
import java.util.LinkedList;
import java.util.List;

/**
 * Log of AutoCVS events.
 * 
 * @author David Hovemeyer
 */
public class EventLog {
	private List<Event> eventList = new LinkedList<Event>();
	private List<Listener> listenerList = new LinkedList<Listener>();

	public interface Listener {
		public void logEvent(Event event);
	}

	public synchronized void addListener(Listener listener) {
		listenerList.add(listener);
	}

	public synchronized List<Event> getAllEvents() {
		List<Event> result = new LinkedList<Event>();
		result.addAll(eventList);
		return result;
	}

	/**
	 * Notify listeners of an event.
	 * 
	 * @param event
	 *            the event
	 */
	private void notifyListeners(Event event) {
		for (Iterator<Listener> iter = listenerList.iterator(); iter.hasNext();) {
			Listener element = iter.next();
			element.logEvent(event);
		}
	}

	public synchronized void purge() {
		eventList.clear();
	}

	/**
	 * Log an event.
	 * 
	 * @param event
	 *            the event
	 */
	public void logEvent(Event event) {
		eventList.add(event);
		notifyListeners(event);
		Debug.print(event.getMessage(), event.getException());
	}

	/**
	 * Log an error event.
	 * 
	 * @param event
	 *            the error event
	 */
	public synchronized void logError(Event event) {
		event.setIsError(true);
		logEvent(event);
	}

	/**
	 * Convenience function for logging an informative (non-error) messge.
	 * 
	 * @param message
	 *            the message
	 */
	public void logMessage(String message) {
		logEvent(new Event(message));
	}

	/**
	 * Convenience function for logging an error message.
	 * 
	 * @param message
	 *            the message
	 */
	public void logError(String message) {
		logError(new Event(message));
	}

	/**
	 * Convenience function for logging an exception.
	 * 
	 * @param message
	 *            the message
	 * @param e
	 *            the exception
	 */
	public void logError(String message, Throwable e) {
		logError(new Event(message, e));
	}
}

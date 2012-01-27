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

package edu.umd.cs.eclipse.courseProjectManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.TeamException;

/**
 * @author jspacco
 * 
 *         XXX Absolutely not thread-safe in any way, shape or form.
 */
class EclipseLaunchEventLog {
	static final String LOG_NAME = ".cpmLOG";
	private static final Map<IProject, EclipseLaunchEventLog> map = new HashMap<IProject, EclipseLaunchEventLog>();

	/**
	 * Only upload to server after at least this many events have been cached in
	 * the file
	 */
	private static final int MAX_CACHED_LAUNCH_EVENTS = Integer.getInteger(
			"max.cached.launch.events", 10);

	private IFile file;

	static final String ECLIPSE_LAUNCH_EVENT = "eclipseLaunchEvent";

	private File getLogFile() {
		return file.getRawLocation().toFile();
	}

	private EclipseLaunchEventLog(IProject project) {
		file = project.getFile(LOG_NAME);
		Debug.print(".cpmLOG is located at " + file.getRawLocation().toString());
	}

	private static EclipseLaunchEventLog getLog(IProject project) {
		if (!map.containsKey(project)) {
			map.put(project, new EclipseLaunchEventLog(project));
		}
		return map.get(project);
	}

	private void log(String event) throws IOException {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(getLogFile(), true));
			writer.write(event + "\n");
			writer.flush();
			writer.close();
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException ignore) {
				// ignore
			}
			try {
				file.refreshLocal(1, null);
			} catch (CoreException ignore) {
				// ignore
			}
		}
	}

	private boolean isEmpty() {
		Debug.print("!exists: " + !getLogFile().exists());
		Debug.print("length: " + getLogFile().length());
		return !getLogFile().exists() || getLogFile().length() == 0L;
	}

	private void clear() throws IOException {
		try {
			file.delete(true, null);
		} catch (CoreException e) {
			throw new IOException(e.getMessage());
		}
	}

	private String getEvents() throws IOException {
		if (isEmpty())
			return "";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					file.getContents()));
			StringBuffer result = new StringBuffer();
			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;
				result.append(line + "\n");
			}
			return result.toString();
		} catch (CoreException e) {
			throw new IOException(e.getMessage());
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException ignore) {
				// ignore
			}
		}
	}


	static void postEventLogToServer(IProject project) {
		// Log to the course project manager log (visible to users).
		AutoCVSPlugin
				.getPlugin()
				.getEventLog()
				.logMessage(
						new Date() + "\t"
								+ "Collecting data for the Marmoset Project.");

		// Look up the EclipseLaunchEventLog for this project. This creates one
		// if it
		// doesn't already exist.
		EclipseLaunchEventLog eclipseLaunchEventLog = EclipseLaunchEventLog
				.getLog(project);

		// NOTE: Don't try to upload if the user has disabled AutoCVS
		// or has shut off Eclipse launch event monitoring.
		IResource submitUserFile = project.findMember(AutoCVSPlugin.SUBMITUSER);

		// Don't log anything if we don't have a submitUser file,
		// autoCVS mode is disabled, or runlogging is disabled
		if (submitUserFile == null || !AutoCVSPlugin.hasAutoCVSNature(project)
				|| !AutoCVSPlugin.hasAutoRunLogNature(project)) {
			return;
		}

		Debug.print("Trying to upload directly to the SubmitServer using a .submitUser file");

		// Find the .submitProject file.
		// This file will only exist for projects controlled by Marmoset.
		IResource submitProjectFile = project
				.findMember(AutoCVSPlugin.SUBMITPROJECT);
		if (submitProjectFile == null) {
			Debug.print("Can't find .submit file!");
			AutoCVSPlugin
					.getPlugin()
					.getEventLog()
					.logMessage(
							"project "
									+ project.getName()
									+ " does not contain a "
									+ AutoCVSPlugin.SUBMITPROJECT
									+ " file.  This project is therefore probably not a "
									+ " Computer Science programming assignment that you checked out of CVS.  "
									+ " Thus you can ignore this message.");
			return;
		}

		try {
			Properties props = new Properties();
			FileInputStream fileInputStream = new FileInputStream(
					submitProjectFile.getRawLocation().toString());
			props.load(fileInputStream);
			fileInputStream.close();

			props.putAll(TurninProjectAction.getUserProperties(submitUserFile));
			Debug.print("Loaded the properties in the .submitUser file");

			// Nothing to report!
			if (eclipseLaunchEventLog.isEmpty())
				return;
			// Check for launch events that have been queued to the logfile.
			String allEvents = eclipseLaunchEventLog.getEvents();
			Debug.print("Grabbed queued events from logfile");

			Debug.print("Uploading!");

			uploadMessages(allEvents, props);

			// Now clear the .cpmLOG
			eclipseLaunchEventLog.clear();
		} catch (IOException e) {
			AutoCVSPlugin
					.getPlugin()
					.getEventLog()
					.logError(
							"Unable to post Eclipse launch events to the server: "
									+ e.getMessage()
									+ "\n"
									+ "This error is related to data gathering for the Marmoset project and should not "
									+ "affect your ability to work on your project.");
			Debug.print("Unable to log eclipse launch event to the server: "
					+ e.toString());
			// e.printStackTrace();
		}
	}

	/**
	 * Uploads a String containing eclipse launch events to the server.
	 * 
	 * @param launchEvents
	 *            the eclipse launch events to be uploaded
	 * @param classAccount
	 *            the class account of the user who generated the launch events
	 * @param oneTimePassword
	 *            the one-time password of the user who generated the launch
	 *            events
	 * @param url
	 *            the URL of the server that will accept the launch events
	 * @throws IOException
	 * @throws HttpException
	 */
	private static int uploadMessages(String launchEvents, Properties props)
			throws IOException, HttpException {
		// Replace the path with /eclipse/LogEclipseLaunchEvent.
		if (!props.containsKey("submitURL")) {
			props.put("submitURL",
					"https://submit.cs.umd.edu:8443/eclipse/LogEclipseLaunchEvent");
		}
		String submitURL = props.getProperty("submitURL");
		Debug.print("submitURL: " + props.getProperty("submitURL"));
		int index = submitURL.indexOf("/eclipse/");
		if (index == -1) {
			Debug.print("Cannot find submitURL in .submitUser file");
			throw new IOException("Cannot find submitURL in .submitUser file");
		}
		submitURL = submitURL.substring(0, index)
				+ "/eclipse/LogEclipseLaunchEvent";
		String version = System.getProperties().getProperty(
				"java.runtime.version");
		boolean useEasyHttps = version.startsWith("1.3")
				|| version.startsWith("1.2") || version.startsWith("1.4.0")
				|| version.startsWith("1.4.1") || version.startsWith("1.4.2_0")
				&& version.charAt(7) < '5';
		if (useEasyHttps) {
			if (submitURL.startsWith("https"))
				submitURL = "easy" + submitURL;
		}
		Debug.print("submitURL: " + submitURL);
		MultipartPostMethod filePost = new MultipartPostMethod(submitURL);

		// add filepart
		byte[] bytes = launchEvents.getBytes();
		filePost.addPart(new FilePart("eclipseLaunchEvent",
				new ByteArrayPartSource("eclipseLaunchEvent", bytes)));

		TurninProjectAction.addAllPropertiesButSubmitURL(props, filePost);

		filePost.addParameter("clientTime",
				Long.toString(System.currentTimeMillis()));

		HttpClient client = new HttpClient();
		client.setConnectionTimeout(5000);

		int status = client.executeMethod(filePost);
		if (status != HttpStatus.SC_OK) {
			System.err.println(filePost.getResponseBodyAsString());
			throw new HttpException("status is: " + status);
		}
		return status;
	}

	static void logEventToFile(String eventName, IProject project) {
		// If either the project doesn't have run-logging turned on
		// or it's not an enabled/disabled event then don't log the event
		if (!eventName.equals(AutoCVSPlugin.ENABLED)
				&& !eventName.equals(AutoCVSPlugin.DISABLED)
				&& !AutoCVSPlugin.hasAutoRunLogNature(project))
			return;

		// Get projectNumber from .submit file
		// In case the Eclipse name of the project differs from the name in the
		// database
		String projectNumber = getProjectNumber(project);

		// eclipseLaunchEvent timestamp md5sum projectName event
		String event = EclipseLaunchEventLog.ECLIPSE_LAUNCH_EVENT + "\t"
				+ System.currentTimeMillis() + "\t"
				+ computeChecksumOfCVSControlledFiles(project) + "\t"
				+ projectNumber + "\t" + eventName;

		EclipseLaunchEventLog eclipseLaunchEventLog = EclipseLaunchEventLog
				.getLog(project);
		try {

			// If we lack the proper .submitUser file
			// then append event to the .cpmLOG file, to be uploaded later.
			eclipseLaunchEventLog.log(event);
		} catch (IOException e) {
			AutoCVSPlugin
					.getPlugin()
					.getEventLog()
					.logError(
							"Unable to log Eclipse launch events to a log file: "
									+ e.getMessage()
									+ "\n"
									+ "This error is related to data gathering for the Marmoset project and should not "
									+ "affect your ability to work on your project.");
			Debug.print(e.toString());
			// e.printStackTrace();
		}
	}

	private static Map<String, String> projectNumberMap = new HashMap<String, String>();

	private static String getProjectNumber(IProject project) {
		if (!projectNumberMap.containsKey(project.getName())) {
			IResource submitProjectFile = project
					.findMember(AutoCVSPlugin.SUBMITPROJECT);
			if (submitProjectFile == null) {
				// If there is no .submit file, then the projectNumber is just
				// the name
				// of the project
				projectNumberMap.put(project.getName(), project.getName());
				return project.getName();
			}
			// Else try to load the properties file
			Properties props = new Properties();
			try {
				FileInputStream fileInputStream = new FileInputStream(
						submitProjectFile.getRawLocation().toString());
				props.load(fileInputStream);
				fileInputStream.close();
				projectNumberMap.put(project.getName(),
						props.getProperty("projectNumber"));
			} catch (IOException e) {
				// Can't load file for whatever reason
				projectNumberMap.put(project.getName(), project.getName());
			}
		}
		return projectNumberMap.get(project.getName());
	}

	private static String computeChecksumOfCVSControlledFiles(IProject project) {
		try {
			// Find all the files under CVS control
			Collection<IFile> fileList = TurninProjectAction
					.findFilesForSubmission(project);
			return md5sumAsHexString(fileList);
		} catch (NoSuchAlgorithmException e) {
			Debug.print("NoSuchAlgorithm " + e.getMessage(), e);
		} catch (IOException e) {
			Debug.print("IOException " + e.getMessage(), e);
		} catch (TeamException e) {
			Debug.print("TeamException " + e.getMessage(), e);
		} catch (CoreException e) {
			Debug.print("CoreException " + e.getMessage(), e);
		}
		return "cant_make_md5sum";
	}

	private static byte[] md5sum(Collection<IFile> fileList)
			throws NoSuchAlgorithmException, IOException, TeamException,
			CoreException {
		MessageDigest md5 = MessageDigest.getInstance("md5");
		byte[] bytes = new byte[2048];
		for (IFile file : fileList) {
			if (!file.exists()) {
				Debug.print("skipping "
						+ file.getName()
						+ " from the md5sum (this file "
						+ "was probably deleted and so exists according to CVS but is "
						+ "not physically present on the file system");
				continue;
			}
			InputStream is = file.getContents();
			try {
				int numRead;
				while ((numRead = is.read(bytes)) != -1) {
					md5.update(bytes, 0, numRead);
				}
			} finally {
				if (is != null)
					is.close();
			}
		}
		return md5.digest();
	}

	/**
	 * Converts an array of bytes into a hexadecimal string. TODO This method
	 * should go into a separate class of static utilities.
	 * 
	 * @param bytes
	 *            the array of bytes
	 * @return the hexadecimal string representation of the byte array
	 */
	private static String byteArrayToHexString(byte[] bytes) {
		return new BigInteger(1, bytes).toString(16);
	}

	private static String md5sumAsHexString(Collection<IFile> fileList)
			throws NoSuchAlgorithmException, IOException, TeamException,
			CoreException {
		return byteArrayToHexString(md5sum(fileList));
	}
}

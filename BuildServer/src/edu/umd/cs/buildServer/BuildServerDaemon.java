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
 * Created on Jan 22, 2005
 */
package edu.umd.cs.buildServer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;

import edu.umd.cs.buildServer.util.IO;
import edu.umd.cs.buildServer.util.ServletAppender;
import edu.umd.cs.marmoset.modelClasses.HttpHeaders;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.utilities.SystemInfo;

/**
 * Request projects to build from the SubmitServer, build them, perform quick
 * and release tests, and send the results back to the SubmitServer. This class
 * contains the main() method which runs the build server.
 *
 * @author David Hovemeyer
 */
public class BuildServerDaemon extends BuildServer implements ConfigurationKeys {
	private String configFile;

	/** Our HttpClient instance. */
	private HttpClient client;

	/**
	 * Constructor.
	 */
	public BuildServerDaemon() {
	}

	/**
	 * Set the name of the BuildServer configuration file.
	 *
	 * @param configFile
	 */
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see edu.umd.cs.buildServer.BuildServer#prepareToExecute()
	 */
	@Override
	protected void prepareToExecute() {
		this.client = new HttpClient();
		client.setConnectionTimeout(5000);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see edu.umd.cs.buildServer.BuildServer#initConfig()
	 */
	@Override
	public void initConfig() throws IOException {
		// TODO: Verify that all the important parameters are set, and FAIL
		// EARLY is necessary
		// TODO: Make getter methods in Config for the required params
		// (build.directory, test.files.directory, etc)
		getConfig().load(
				new BufferedInputStream(new FileInputStream(configFile)));

		// I'm setting the clover binary database in this method rather than in
		// the config.properties file because it always goes into /tmp and I
		// need
		// a unique name in case there are multiple buildServers on the same
		// host

		// TODO move the location of the Clover DB to the build directory.
		// NOTE: This requires changing the security.policy since Clover needs
		// to be able
		// to read, write and create files in the directory.
		//
		// String cloverDBPath =
		// getConfig().getRequiredProperty(BUILD_DIRECTORY) +"/myclover.db";
		String cloverDBPath = "/tmp/myclover.db."
				+ Long.toHexString(nextRandomLong());
		getConfig().setProperty(CLOVER_DB, cloverDBPath);
	}

	private String getWelcomeURL(){
        return getBuildServerConfiguration().getServletURL(SUBMIT_SERVER_WELCOME_PATH);
    }


	private String getRequestProjectURL(){
		return getBuildServerConfiguration().getServletURL(SUBMIT_SERVER_REQUESTPROJECT_PATH);
	}

	private String getTestSetupURL(){
		return getBuildServerConfiguration().getServletURL(SUBMIT_SERVER_GETTESTSETUP_PATH);
	}

	private String getReportTestResultsURL() {
		return getBuildServerConfiguration().getServletURL(SUBMIT_SERVER_REPORTTESTRESULTS_PATH);
	}

	/**
	 * Get a required header value. If the header value isn't specified in the
	 * server response, returns null.
	 *
	 * @param method
	 *            the HttpMethod representing the request/response
	 * @param headerName
	 *            the name of the header
	 * @return the value of the header, or null if the header isn't present
	 * @throws HttpException
	 */
	private String getRequiredHeaderValue(HttpMethod method, String headerName)
			throws HttpException {
		Header header = method.getResponseHeader(headerName);
		if (header == null || header.getValues().length != 1) {
			getLog().error(
					"Internal error: Missing header " + headerName
							+ " in submit server response");
			for(Header h : method.getResponseHeaders()) {
				getLog().error(
						"  have header " + h.getName());
			}
			return null;
		}
		return header.getValue();
	}
	/**
	 * Get a required header value. If the header value isn't specified in the
	 * server response, returns null.
	 *
	 * @param method
	 *            the HttpMethod representing the request/response
	 * @param headerName
	 *            the name of the header
	 * @return the value of the header, or null if the header isn't present
	 * @throws HttpException
	 */
	private String getRequiredHeaderValue(HttpMethod method, String headerName, String alternativeHeaderName)
			throws HttpException {
		Header header = method.getResponseHeader(headerName);
		if (header == null || header.getValues().length != 1) 
			header = method.getResponseHeader(alternativeHeaderName);
		if (header == null || header.getValues().length != 1) {
			getLog().error(
					"Internal error: Missing header " + headerName
							+ " in submit server response");
			for(Header h : method.getResponseHeaders()) {
				getLog().error(
						"  have header " + h.getName());
			}
			return null;
		}
		return header.getValue();
	}

    protected void doWelcome() throws MissingConfigurationPropertyException, IOException {
        System.out.println("Connecting to submit server");
        String url = getWelcomeURL();
        MultipartPostMethod method = new MultipartPostMethod(url);

       
        method.addParameter("hostname", getBuildServerConfiguration().getHostname());
        String supportedCourses = getBuildServerConfiguration().getSupportedCourses();
        method.addParameter("courses", supportedCourses);
        method.addParameter("load", SystemInfo.getSystemLoad());

        BuildServer.printURI(getLog(), method);

        int responseCode = client.executeMethod(method);
        System.out.println(method.getResponseBodyAsString());
        if (responseCode != HttpStatus.SC_OK) {

            getLog().error("HTTP server returned non-OK response: " + responseCode + ": " + method.getStatusText());
            getLog().error(" for URI: " + method.getURI());

            getLog().error("Full error message: " + method.getStatusText());
            throw new IOException(method.getStatusText());
        }
    }
    
	
	/*
	 * (non-Javadoc)
	 *
	 * @see edu.umd.cs.buildServer.BuildServer#getProjectSubmission()
	 */
	@Override
	protected ProjectSubmission getProjectSubmission()
			throws MissingConfigurationPropertyException, IOException {

		String url = getRequestProjectURL();
		MultipartPostMethod method = new MultipartPostMethod(
				url);		

		  String supportedCoursePKList 
		    = getBuildServerConfiguration().getSupportedCourses();

		
		String specificSubmission = getConfig().getOptionalProperty(
				DEBUG_SPECIFIC_SUBMISSION);
		String specificTestSetup = getConfig().getOptionalProperty(
				DEBUG_SPECIFIC_TESTSETUP);

		if (specificSubmission != null) {
			method.addParameter("submissionPK", specificSubmission);
			System.out.printf("Requesting submissionPK %s%n", specificSubmission);
		}
		if (specificTestSetup != null) {
		    method.addParameter("testSetupPK", specificTestSetup);
		    System.out.printf("Requesting testSetupPK %s%n", specificTestSetup);
		}
		
		method.addParameter("hostname",
				getBuildServerConfiguration().getHostname());
		method.addParameter("courses", supportedCoursePKList);
		method.addParameter("load", SystemInfo.getSystemLoad());

		BuildServer.printURI(getLog(), method);

		int responseCode = client.executeMethod(method);
		if (responseCode != HttpStatus.SC_OK) {
			if (responseCode == 503) {
				getLog().trace("Server returned 503 (no work)");
			} else {
				getLog().error(
						"HTTP server returned non-OK response: " + responseCode
								+ ": " + method.getStatusText());
				getLog().error(
						" for URI: " + method.getURI());
			
				getLog().error(
						"Full error message: "
								+ method.getResponseBodyAsString());
			}
			return null;
		}

		getLog().debug(
				"content-type: " + method.getResponseHeader("Content-type"));
		getLog().debug(
				"content-length: " + method.getResponseHeader("content-length"));
		// Ensure we have a submission PK.
		String submissionPK = getRequiredHeaderValue(method,
				HttpHeaders.HTTP_SUBMISSION_PK_HEADER);
		if (submissionPK == null)
			return null;

		// Ensure we have a project PK.
		String testSetupPK = specificTestSetup != null ? specificTestSetup : getTestSetupPK(method);
		if (testSetupPK == null)
			return null;

		// This is a boolean value specifying whether the project jar file
		// is NEW, meaning that it needs to be tested against the
		// canonical project solution. The build server doesn't need
		// to do anything with this value except pass it back to
		// the submit server when reporting test outcomes.
		String isNewTestSetup = getIsNewTestSetup(method);
		if (isNewTestSetup == null)
			return null;

		// Opaque boolean value representing whether this was a
		// "background retest".
		// The BuildServer doesn't need to do anything with this except pass it
		// back to the SubmitServer.
		String isBackgroundRetest = getRequiredHeaderValue(method,
				HttpHeaders.HTTP_BACKGROUND_RETEST);
		if (isBackgroundRetest == null)
			isBackgroundRetest = "no";

		ServletAppender servletAppender = (ServletAppender) getLog()
				.getAppender("servletAppender");
		if (isBackgroundRetest.equals("yes"))
			servletAppender.setThreshold(Level.FATAL);
		else
			servletAppender.setThreshold(Level.INFO);

		String kind = method.getResponseHeader(HttpHeaders.HTTP_KIND_HEADER).getValue();
		getLog().info(
                "Got submission " +submissionPK +  ", testSetup " + testSetupPK + ", kind: " + kind);
        
		ProjectSubmission projectSubmission = new ProjectSubmission(
				getBuildServerConfiguration(), getLog(), submissionPK, testSetupPK,
				isNewTestSetup, isBackgroundRetest);

		projectSubmission.setMethod(method);

		return projectSubmission;
	}

	/**
	 * @param method
	 * @return
	 * @throws HttpException
	 */
	@SuppressWarnings("deprecation")
	private String getIsNewTestSetup(MultipartPostMethod method)
			throws HttpException {
		return getRequiredHeaderValue(method,
				HttpHeaders.HTTP_NEW_TEST_SETUP, HttpHeaders.HTTP_NEW_PROJECT_JARFILE);
	}

	/**
	 * @param method
	 * @return
	 * @throws HttpException
	 */
	@SuppressWarnings("deprecation")
	private String getTestSetupPK(MultipartPostMethod method)
			throws HttpException {
		return getRequiredHeaderValue(method,
				HttpHeaders.HTTP_TEST_SETUP_PK_HEADER, HttpHeaders.HTTP_PROJECT_JARFILE_PK_HEADER);

	}

	@Override
	protected void downloadSubmissionZipFile(ProjectSubmission projectSubmission)
			throws IOException {
		IO.download(projectSubmission.getZipFile(),
				projectSubmission.getMethod());
	}

	@Override
	protected void downloadProjectJarFile(ProjectSubmission projectSubmission)
			throws MissingConfigurationPropertyException, HttpException,
			IOException, BuilderException {
		// FIXME: We should cache these

		MultipartPostMethod method = new MultipartPostMethod(
				getTestSetupURL());
		method.addParameter("testSetupPK", projectSubmission.getTestSetupPK());
		method.addParameter("projectJarfilePK", projectSubmission.getTestSetupPK());
		String supportedCourses = getBuildServerConfiguration().getSupportedCourses();
        method.addParameter("courses", supportedCourses);
        
		BuildServer.printURI(getLog(), method);

		try {
			int responseCode = client.executeMethod(method);
			if (responseCode != HttpStatus.SC_OK) {
				throw new BuilderException(
						"Could not download project test setup from " + getTestSetupURL() + ": " + responseCode
								+ ": " + method.getStatusText());
			}

			getLog().trace("Downloading test setup file");
			IO.download(projectSubmission.getTestSetup(), method);

			// We're passing the project_jarfile_pk so we don't need to read it
			// from
			// the headers

			// wait for a while in case the files have not "settled"
			// TODO: Verify that this is still necessary; should be OK unless
			// run on NFS
			pause(1000);

			getLog().trace("Done.");
		} finally {
			method.releaseConnection();
		}

	}

	@Override
	protected void releaseConnection(ProjectSubmission projectSubmission) {
		projectSubmission.getMethod().releaseConnection();
	}

	private void dumpOutcomes(ProjectSubmission projectSubmission) {
		try {
			// Can't dump outcomes if we don't have a test.properties file.
			if (projectSubmission.getTestProperties() == null)
				return;
			ObjectOutputStream out = null;
			File outputFile = new File(projectSubmission
					.getBuilderAndTesterFactory().getDirectoryFinder()
					.getBuildDirectory(), "daemonresults.out");
			try {
				out = new ObjectOutputStream(new FileOutputStream(outputFile));
				projectSubmission.getTestOutcomeCollection().write(out);
			} catch (IOException e) {
				System.err.println("Could not save test outcome collection in "
						+ outputFile.getPath());
				e.printStackTrace(); // OK, this is a command line app
			} finally {
				IOUtils.closeQuietly(out);
			}
		} catch (Exception e) {
			// XXX Prevent this from throwing exception
			getLog().warn(
					"Ignoring error in BuildServerDaemon.dumpOutcomes " + e);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * edu.umd.cs.buildServer.BuildServer#reportTestResults(edu.umd.cs.buildServer
	 * .ProjectSubmission)
	 */
	@Override
	protected void reportTestResults(ProjectSubmission projectSubmission)
			throws MissingConfigurationPropertyException {

		dumpOutcomes(projectSubmission);

		getLog().info(
				"Test outcome collection for "
						+ projectSubmission.getSubmissionPK()
						+ " for test setup "
						+ projectSubmission.getTestSetupPK() + " contains "
						+ projectSubmission.getTestOutcomeCollection().size()
						+ " entries");

		// Format the test outcome collection as bytes in memory
		ByteArrayOutputStream sink = new ByteArrayOutputStream();
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(sink);
		} catch (IOException ignore) {
			getLog().error("IOException creating ObjectOutputStream");
		}

		TestOutcomeCollection c = projectSubmission.getTestOutcomeCollection();

		// Print some info about the size of the collection
		getLog().info("Got TestOutcomeCollection; size: " + c.size());
		for (TestOutcome to : c.getAllOutcomes()) {
			// Truncate to avoid OutOfMemories
			to.truncateLongTestResult();
			// Most important size to print is the longResult len - it
			// can be really long
			int length = to.getLongTestResult().length();
			getLog().info(
					"  Outcome " + to.getTestNumber() + ": " + to.getTestName() + " = " + to.getOutcome() 
					+ (length > 0 ?  ", longResult len: " +  length : ""));

		}

		try {
			c.write(out);
		} catch (IOException ignore) {
			getLog().error("IOException writing to ObjectOutputStream", ignore);
		} catch (Error e) {
			// Can happen if the long test output is really long; we
			// truncate down to 64K (also the limit imposed by the
			// MySQL 'text' type) in order to avoid this, but we should
			// note it.
			getLog().error("While writing, caught Error", e);
			getLog().error("Rethrowing...");
			throw (e);
		}

		try {
			out.close();
		} catch (IOException ignore) {
			getLog().error("IOException closing ObjectOutputStream");
		}

		byte[] testOutcomeData = sink.toByteArray();
		String subPK = projectSubmission.getSubmissionPK();
		String jarfilePK = projectSubmission.getTestSetupPK();
		int outcomes = projectSubmission.getTestOutcomeCollection().size();
		getLog().info(
				"Test data for submission " + subPK + " for test setup "
						+ jarfilePK + " contains " + testOutcomeData.length
						+ " bytes from " + outcomes + " test outcomes");

		String hostname = getBuildServerConfiguration().getHostname();

		MultipartPostMethod method = new MultipartPostMethod(
				getReportTestResultsURL());

		method.addParameter("submissionPK", projectSubmission.getSubmissionPK());
		method.addParameter("testSetupPK", projectSubmission.getTestSetupPK());
		method.addParameter("projectJarfilePK", projectSubmission.getTestSetupPK());
		method.addParameter("newTestSetup",
				projectSubmission.getIsNewTestSetup());
		
		method.addParameter("newProjectJarfile", projectSubmission.getIsNewTestSetup());
		method.addParameter("isBackgroundRetest",
				projectSubmission.getIsBackgroundRetest());
		method.addParameter("testMachine", hostname);
		method.addParameter("hostname", hostname);
		method.addParameter("load", SystemInfo.getSystemLoad());
		String supportedCourses = getBuildServerConfiguration().getSupportedCourses();
        method.addParameter("courses", supportedCourses);
        

		
		// CodeMetrics
		if (projectSubmission.getCodeMetrics() != null) {
			getLog().debug(
					"Code Metrics: " + projectSubmission.getCodeMetrics());
			projectSubmission.getCodeMetrics().mapIntoHttpHeader(method);
		}
		method.addPart(new FilePart("testResults", new ByteArrayPartSource(
				"testresults.out", testOutcomeData)));
		printURI(method);

		try {
			getLog().debug(
					"Submitting test results for "
							+ projectSubmission.getSubmissionPK() + "...");

			int statusCode = client.executeMethod(method);
			if (statusCode == HttpStatus.SC_OK)
				getLog().debug(
						"Done submitting test results for submissionPK "
								+ projectSubmission.getSubmissionPK()
								+ "; statusCode=" + statusCode);
			else {

				getLog().error(
						"Error submitting test results for submissionPK "
								+ projectSubmission.getSubmissionPK() + ": "
								+ statusCode + ": " + method.getStatusText());
				getLog().error(method.getResponseBodyAsString());
				// TODO: Should we do anything else in case of an error?
			}
		} catch (HttpException e) {
			getLog().error(
					"Internal error: HttpException submitting test results", e);
			return;
		} catch (IOException e) {
			getLog().error(
					"Internal error: IOException submitting test results", e);
			return;
		} finally {
			getLog().trace("Releasing connection...");
			method.releaseConnection();
			getLog().trace("Done releasing connection");
		}
	}

	/**
	 * Command-line interface.
	 */
	public static void main(String[] args) {
		if (args.length < 1 || args.length > 4) {
			System.err.println("Usage: " + BuildServerDaemon.class.getName()
					+ " <config properties> [ once | <submissionPK>  [<testPK> [<log4j-level>]]");
			System.exit(1);
		}

		Protocol easyhttps = new Protocol("https",
				new EasySSLProtocolSocketFactory(), 443);
		Protocol.registerProtocol("easyhttps", easyhttps);

		final String configFile = args[0];
		BuildServerDaemon buildServer = new BuildServerDaemon();
		buildServer.setConfigFile(configFile);

		try {
			buildServer.initConfig();
			// Setting "once" on the command line forces the BuildServer to
			// run once and print all output to standard out.
			// Equivalent to setting "debug.donotloop=true" and
			// "log.directory=console"
			// in the config.properties file, without actually having to edit
			// the file.
			if (args.length > 1 && args[1].length() > 0) {
				buildServer.getConfig().setProperty(LOG_DIRECTORY, "console");
				buildServer.getConfig().setProperty(DEBUG_DO_NOT_LOOP, "true");
				buildServer.getConfig().setProperty(
						DEBUG_PRESERVE_SUBMISSION_ZIPFILES, "true");
				if (args[1].equalsIgnoreCase("once")) {
				    if (args.length == 3)
				        buildServer.getConfig().setProperty(
				                LOG4J_THRESHOLD, args[2]);
                        
				} else {
					try {
						Integer.parseInt(args[1]);
						buildServer.getConfig().setProperty(
								DEBUG_SPECIFIC_SUBMISSION, args[1]);
						if (args.length >= 3) {
							Integer.parseInt(args[2]);
							buildServer.getConfig().setProperty(
									DEBUG_SPECIFIC_TESTSETUP, args[2]);
							if (args.length >= 4)
		                        buildServer.getConfig().setProperty(
		                                LOG4J_THRESHOLD, args[3]);
						}
					} catch (NumberFormatException e) {
						throw new NumberFormatException("'" + args[1] + "'"
								+ " isn't a valid submissionPK");
					}
				}

			}

			buildServer.executeServerLoop();
			buildServer.getLog().info("Shutting down");
			timedSystemExit0();
		} catch (Exception e) {

			getBuildServerLog()
					.fatal("BuildServerDaemon got fatal exception; waiting for cron to restart me: ",
							e);
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void timedSystemExit0() {
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					assert true;
				}
				System.exit(0);
			}
		}, "force shutdown thread");
		t.setDaemon(true);
		t.start();
	}

	private static SecureRandom rng = new SecureRandom();

	private static long nextRandomLong() {
		synchronized (rng) {
			return rng.nextLong();
		}
	}
}

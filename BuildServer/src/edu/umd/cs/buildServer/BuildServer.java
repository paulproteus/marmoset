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
 * Created on Aug 30, 2004
 */
package edu.umd.cs.buildServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Properties;
import java.util.Random;

import javax.annotation.CheckForNull;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.SimpleLayout;

import edu.umd.cs.buildServer.builder.BuilderAndTesterFactory;
import edu.umd.cs.buildServer.util.LoadAverage;
import edu.umd.cs.buildServer.util.ServletAppender;
import edu.umd.cs.marmoset.modelClasses.MissingRequiredTestPropertyException;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcome.OutcomeType;
import edu.umd.cs.marmoset.modelClasses.TestOutcome.TestType;
import edu.umd.cs.marmoset.modelClasses.TestProperties;
import edu.umd.cs.marmoset.utilities.TestPropertiesExtractor;
import edu.umd.cs.marmoset.utilities.ZipExtractorException;

/**
 * A BuildServer obtains a project submission zipfile and a project jarfile,
 * builds the submission, tests the submission (if the build was successful),
 * and reports the test results.
 *
 * <p>
 * Subclasses implement how submissions and project jarfiles are obtained, and
 * how test results are reported.
 * </p>
 *
 * @author David Hovemeyer
 */
public abstract class BuildServer implements ConfigurationKeys {
	static BuildServer instance;

	// Name of build directory subdirectory where compiled class
	// files are generated.
	public static final String BUILD_OUTPUT_DIR = "obj";
	public static final String SOURCE_DIR = "src";
	public static final String INSTRUMENTED_SOURCE_DIR = "inst-src";

	private static final String LOG4J_FILE_CONFIG = "edu/umd/cs/buildServer/log4j-file.properties";
	private static final String LOG4J_CONSOLE_CONFIG = "edu/umd/cs/buildServer/log4j-console.properties";

	// Status codes from doOneRequest()
	private static final int NO_WORK = 0;
	private static final int SUCCESS = 1;
	private static final int COMPILE_FAILURE = 2;
	private static final int BUILD_FAILURE = 3;

	/**
	 * We will sleep at most 2^MAX_SLEEP seconds as the poll interval. We sleep
	 * for shorter periods when there has been work to do recently, longer
	 * periods if there hasn't been for a while.
	 */
	private static final int MAX_SLEEP = 4;

	/** Properties indicating how to connect to the submit server. */
	private Configuration config;

	/** Number of server loop iterations. */
	protected int numServerLoopIterations;

	/** Number of times submit server was polled and there was no work. */
	private int noWorkCount;

	/**
	 * Configuration information for the buildserver (what classes should be
	 * built, for what semester, how many iterations of the server loop, where
	 * is the logfile located, should we keep looping, etc). This class is
	 * designed to encapsulate all the useful configuration information about a
	 * buildServer as an MBean to manage buildServers while they are running.
	 * This class should replace the Configuration class.
	 */
	protected BuildServerConfiguration buildServerConfiguration;

	private Logger log;

	/**
	 * Constructor.
	 */
	public BuildServer() {
		this.config = new Configuration();
		this.noWorkCount = 0;

		if (BuildServer.instance != null)
			throw new IllegalStateException();
		BuildServer.instance = this;
	}

	/**
	 * Get the BuildServer instance.
	 *
	 * @return the BuildServer instance
	 */
	public static BuildServer instance() {
		return instance;
	}

	/**
	 * @return Returns the config.
	 */
	public Configuration getConfig() {
		return config;
	}

	/**
	 * @return Returns the log.
	 */
	public Logger getLog() {
		return log;
	}

	/**
	 * @return The BuildServerConfiguration.
	 */
	public BuildServerConfiguration getBuildServerConfiguration() {
		return buildServerConfiguration;
	}

	private final static Logger buildServerLog = Logger
			.getLogger("edu.umd.cs.buildServer.BuildServer");

	/**
	 * Static getter for the buildServer log.
	 *
	 * @return
	 */
	public static Logger getBuildServerLog() {
		return buildServerLog;
	}

	/**
	 * Write a URI of an HttpMethod to the Log.
	 *
	 * @param log
	 *            the Log
	 * @param method
	 *            the HttpMethod
	 */
	public static void printURI(Logger log, HttpMethod method) {
		try {
			log.trace("URI=" + method.getURI());
		} catch (URIException e) {
			log.error("Could not print URI for HttpMethod", e);
		}
	}

	/**
	 * Write a URI of an HttpMethod to the Log.
	 *
	 * @param method
	 *            the HttpMethod
	 */
	protected void printURI(HttpMethod method) {
		printURI(getLog(), method);
	}

	/**
	 * Populate the Properties object containing the configuration for the build
	 * server. Subclasses must override this to change the way the config
	 * properties are found.
	 *
	 * @param config
	 *            the Properties object containing the configuration
	 * @throws IOException
	 */
	public abstract void initConfig() throws IOException;

	protected void configureBuildServerForMBeanManagement() {
		buildServerConfiguration = new BuildServerConfiguration();
		// Try to configure a BuildServerConfiguration object
		try {
			buildServerConfiguration.loadAllProperties(getConfig());
			// Get MBeanServer
			MBeanServer platformMBeanserver = ManagementFactory
					.getPlatformMBeanServer();
			// Register the BuildServerMBean
			ObjectName buildServerName = new ObjectName(
					"edu.umd.cs.buildServer:id=BuildServerManager");
			platformMBeanserver.registerMBean(buildServerConfiguration,
					buildServerName);
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException(e);
		} catch (MBeanRegistrationException e) {
			throw new RuntimeException(e);
		} catch (NotCompliantMBeanException e) {
			throw new RuntimeException(e);
		} catch (InstanceAlreadyExistsException e) {
			throw new RuntimeException(e);
		} catch (MissingConfigurationPropertyException e) {
			// getLog().warn("Unable to configure (experimental) BuildServerConfiguration object");
			System.out.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * Return whether or not a servlet appender should be created when the
	 * server loop executes. By default, this returns true. Subclasses may
	 * override, for example when testing the BuildServer on the command line
	 * there is no server to append messages to.
	 *
	 * @return true if a servlet appender should be used
	 */
	protected boolean useServletAppender() {
		return true;
	}

	/**
	 * Execute the build server server loop.
	 *
	 * @throws IOException
	 * @throws SecurityException
	 */
	public void executeServerLoop() throws InterruptedException,
			MissingConfigurationPropertyException, SecurityException,
			IOException {
	    configureBuildServerForMBeanManagement();
		createWorkingDirectories();
		this.log = createLog(getBuildServerConfiguration(), useServletAppender());

		

		prepareToExecute();

		String supportedCourseList = getBuildServerConfiguration().getSupportedCourses();
		getLog().debug(
				"Executing server loop; can build " + supportedCourseList);
		
		doWelcome();
		int overloadCount = 0;
		while (continueServerLoop()) {

			if (LoadAverage.isOverloaded()) {
				overloadCount++;
				getLog().warn(
						"Build server overloaded, weighted load average" +
						" is " + LoadAverage.getWeightedLoadAverage());
				if (overloadCount < 4)
					continue;
			}
			overloadCount = 0;

			int rc = doOneRequest();
			log.trace("Done with request");

			// If there was no work, or if the project could
			// not be built due to an internal error,
			// sleep for a while. This will help avoid
			// thrashing the build server when nothing useful
			// can be done.
			if (rc == NO_WORK || rc == BUILD_FAILURE) {
				sleep();
			} else {
				noWorkCount = 0;
			}

			++numServerLoopIterations;
		}

		getLog().debug("Server loop finished");
	}

	/**
	 * Called before each iteration of the server loop to determine whether or
	 * not the loop should continue. Subclasses may override.
	 *
	 * @return true if the server loop should continue, false if not
	 */
	protected boolean continueServerLoop() {
		if (new File(buildServerConfiguration.getBuildServerWorkingDir(),"pleaseShutdown").exists())
			return false;
		if (config.getDebugProperty(DEBUG_DO_NOT_LOOP)
				|| config.getOptionalProperty(DEBUG_SPECIFIC_SUBMISSION) != null)
			return numServerLoopIterations == 0;
		else
			return true;
	}

	/**
	 * Create build, testfiles, jarcache, and log directories if they don't
	 * exist already.
	 *
	 * @throws MissingConfigurationPropertyException
	 * @throws IOException
	 *             if one of the directories couldn't be created
	 */
	private void createWorkingDirectories()
			throws MissingConfigurationPropertyException, IOException {
	    if (buildServerConfiguration == null)
	        throw new IllegalStateException("buildServerConfiguration not initialized");
		createDirectory(buildServerConfiguration.getBuildServerWorkingDir());
		makeDirectoryWorldReadable(buildServerConfiguration.getBuildServerWorkingDir());
		createDirectory(buildServerConfiguration.getTestFilesDirectory());
		createDirectory(buildServerConfiguration.getBuildDirectory());
		createDirectory(buildServerConfiguration.getJarCacheDirectory());
		createDirectory(buildServerConfiguration.getLogDirectory());
	}

	/**
	 * Create given directory if it doesn't already exist.
	 *
	 * @param dirName
	 *            name of the directory to create
	 * @throws IOException
	 *             if the directory couldn't be created
	 */
	private void createDirectory(@CheckForNull File dir) throws IOException {
	    if (dir == null)
	        return;
	    String dirName = dir.getAbsolutePath();
		if (dir.exists()) {
			if (dir.isFile()) {
				throw new IOException("Directory " + dirName
						+ " cannot be created because "
						+ "a file with the same name already exists");
			} else if (!dir.isDirectory()) {
				throw new IOException("Path " + dirName
						+ " exists, but does not seem to be a file "
						+ " or a directory!");
			}
		} else {
			if (!dir.mkdirs())
				throw new IOException("Directory " + dirName
						+ " does not exist, and couldn't create it");

			if (!dir.exists())
				throw new IOException("WTF? " + dir.toString());
		}
	}

	/**
	 * Makes a directory world-readable.
	 * <p>
	 * This will not work on Windows!
	 *
	 * @param dirName
	 *            The directory to make world-readable.
	 * @throws IOException
	 */
	private void makeDirectoryWorldReadable(File dir) throws IOException {
		// TODO Make sure that this is a non-Windows machine.
		
        if (dir.isDirectory()) {
            dir.setReadable(true, false);
            dir.setWritable(true, false);
            dir.setExecutable(true, false);
		}
	}

	/**
	 * Prepare to execute the server loop.
	 */
	protected abstract void prepareToExecute()
			throws MissingConfigurationPropertyException;

	/**
	 * Create the Log object.
	 *
	 * @param config
	 *            the build server Configuration
	 * @param useServletAppender
	 *            if true, add a ServletAppender
	 * @return the Log object
	 * @throws MissingConfigurationPropertyException
	 * @throws IOException
	 */
	public static Logger createLog(BuildServerConfiguration config,
			boolean useServletAppender)
			throws MissingConfigurationPropertyException, IOException {

		@CheckForNull File logDir = config.getLogDirectory();
		String configResource;

		if (logDir == null) {
			configResource = LOG4J_CONSOLE_CONFIG;
		} else {
			System.setProperty("buildserver.log.dir", logDir.getAbsolutePath());
			configResource = LOG4J_FILE_CONFIG;
		}

		Properties log4jProperties = new Properties();
		InputStream in = BuildServer.class.getClassLoader()
				.getResourceAsStream(configResource);
		if (in == null)
			throw new IOException("Could not read resource " + configResource);
		try {
			log4jProperties.load(in);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// Ignore
			}
		}

		String threshold = config.getConfig().getOptionalProperty(ConfigurationKeys.LOG4J_THRESHOLD);
		if (threshold != null)
		    log4jProperties.setProperty("log4j.appender.fileAppender.Threshold", threshold);
		PropertyConfigurator.configure(log4jProperties);

		Logger logger = Logger.getLogger(BuildServer.class.getName());

		if (useServletAppender) {
			// need to pass the config object to retrieve the SubmitServer
			// password
			ServletAppender servletAppender = new ServletAppender();
			servletAppender.setLayout(new SimpleLayout());
			servletAppender.setThreshold(Level.INFO);
			servletAppender.setName("servletAppender");
			servletAppender.setConfig(config);
			logger.addAppender(servletAppender);
		}

		return logger;
	}

	/**
	 * Get a single project from the submit server, and try to build and test
	 * it.
	 *
	 * @return a status code: NO_WORK if there was no work available, SUCCESS if
	 *         we downloaded, built, and tested a project successfully,
	 *         COMPILE_ERROR if the project failed to compile, BUILD_ERROR if
	 *         the project could not be built due to an internal error
	 */
	private int doOneRequest() throws MissingConfigurationPropertyException {

	    ProjectSubmission<?> projectSubmission = null;
		try {
			// Get a ProjectSubmission to build and test
		   projectSubmission  = getProjectSubmission();
			if (projectSubmission == null)
				return NO_WORK;

			long start = System.currentTimeMillis();
			int result;
			try {
				cleanWorkingDirectories();

				log.trace("About to download project");

				// Read the zip file from the response stream.
				downloadSubmissionZipFile(projectSubmission);

				log.trace("Done downloading project");

				log.warn("Preparing to  process submission "
						+ projectSubmission.getSubmissionPK()
						+ " for test setup "
						+ projectSubmission.getTestSetupPK());

				// Get the project jar file containing the provided classes
				// and the secret tests.
				downloadProjectJarFile(projectSubmission);
				// log.warn

				if (getDownloadOnly())
				    return NO_WORK;
				
				long started = System.currentTimeMillis();
				// Now we have the project and the testing jarfile.
				// Build and test it.
				try {
					buildAndTestProject(projectSubmission);

					// Building and testing was successful.
					// ProjectSubmission should have had its public, release,
					// secret and student
					// TestOutcomes added.
					addBuildTestResult(projectSubmission, TestOutcome.PASSED,
							"", started);
					result = SUCCESS;
				} catch (BuilderException e) {
					// treat as compile error
					getLog().info(
							"Submission " + projectSubmission.getSubmissionPK()
									+ " for test setup "
									+ projectSubmission.getTestSetupPK()
									+ " did not build", e);

					// Add build test outcome
					String compilerOutput = e.toString() + "\n";
					addBuildTestResult(projectSubmission, TestOutcome.FAILED,
							compilerOutput, started);

					getLog().warn(
							"Marking all classes of tests 'could_not_run' for "
									+ projectSubmission.getSubmissionPK()
									+ " and test-setup "
									+ projectSubmission.getTestSetupPK());
					// Add "cannot build submission" test outcomes for
					// the dynamic test types
					for(TestType testType : TestType.DYNAMIC_TEST_TYPES) 
                        addSpecialFailureTestOutcome(projectSubmission,
                                testType, "Compiler output:\n" + compilerOutput);
                        

					result = COMPILE_FAILURE;
				} catch (CompileFailureException e) {
					// If we couldn't compile, report special testOutcome
					// stating this fact
					log.info(
							"Submission " + projectSubmission.getSubmissionPK()
									+ " did not compile", e);

					// XXX [Nat] If the compile failed because of an
					// OutOfMemoryError, should we report anything?

					// Add build test outcome
					String compilerOutput = e.toString() + "\n"
							+ e.getCompilerOutput();
					addBuildTestResult(projectSubmission, TestOutcome.FAILED,
							compilerOutput, started);

					// Add "cannot build submission" test outcomes for
					// the dynamic test types
					for(TestType testType : TestType.DYNAMIC_TEST_TYPES) 
					    addSpecialFailureTestOutcome(projectSubmission,
								testType, "Compiler output:\n" + compilerOutput);

					result = COMPILE_FAILURE;
				}
			} finally {
				// Make sure the zip file is cleaned up.
				if (!getConfig().getDebugProperty(
						DEBUG_PRESERVE_SUBMISSION_ZIPFILES)
						&& !projectSubmission.getZipFile().delete()) {
					log.error("Could not delete submission zipfile "
							+ projectSubmission.getZipFile());
				}
			}

			// Send the test results back to the submit server
			reportTestResults(projectSubmission);

			long total = System.currentTimeMillis() - start;
			log.info("submissionPK " + projectSubmission.getSubmissionPK()
					+ " took " + (total / 1000) + " seconds to process");
			return result;

		} catch (HttpException e) {
			log.error("Internal error: BuildServer got HttpException", e);
			// Assume this wasn't our fault
			return NO_WORK;
		} catch (IOException e) {
			log.error("Internal error: BuildServer got IOException", e);
			// Assume this is an internal error
			return BUILD_FAILURE;
		} catch (BuilderException e) {
			log.error("Internal error: BuildServer got BuilderException", e);
			// This is a build failure
			return BUILD_FAILURE;
		} finally {
			if (projectSubmission != null) {
				releaseConnection(projectSubmission);
			}
		}
	}

	/**
	 * Ensure build and testfiles directories are completely empty before we
	 * commence building an testing a submission.
	 *
	 * @throws MissingConfigurationPropertyException
	 */
	private void cleanWorkingDirectories()
			throws MissingConfigurationPropertyException {
		cleanUpDirectory(getBuildServerConfiguration().getBuildDirectory());
		cleanUpDirectory(getBuildServerConfiguration().getTestFilesDirectory());
	}

	protected abstract void doWelcome() throws MissingConfigurationPropertyException, IOException;
	/**
	 * Get a ProjectSubmission object representing the submission to be built
	 * and tested.
	 *
	 * @return a ProjectSubmission, or null if the response didn't include all
	 *         of the required information
	 * @throws MissingConfigurationPropertyException
	 * @throws IOException
	 */
	protected abstract ProjectSubmission<?> getProjectSubmission()
			throws MissingConfigurationPropertyException, IOException;

	/**
	 * Download the submission zipfile into the build directory.
	 *
	 * @param projectSubmission
	 *            the ProjectSubmission
	 * @throws IOException
	 */
	protected abstract void downloadSubmissionZipFile(
			ProjectSubmission<?> projectSubmission) throws IOException;

	/**
	 * Release the connection used to contact the submit server.
	 *
	 * @param projectSubmission
	 *            the current ProjectSubmission
	 */
	protected abstract void releaseConnection(
			ProjectSubmission<?> projectSubmission);

	/**
	 * Download the project jarfile into the jarcache directory.
	 *
	 * @param projectSubmission
	 *            the current ProjectSubmission
	 * @throws MissingConfigurationPropertyException
	 * @throws HttpException
	 * @throws IOException
	 * @throws BuilderException
	 */
	protected abstract void downloadProjectJarFile(
			ProjectSubmission<?> projectSubmission)
			throws MissingConfigurationPropertyException, HttpException,
			IOException, BuilderException;

	/**
	 * Report test outcomes for a submission to the submit server.
	 *
	 * @param testOutcomeCollection
	 *            collection of test outcomes to report
	 * @param submissionPK
	 *            the PK of the submission
	 */
	protected abstract void reportTestResults(
			ProjectSubmission<?> projectSubmission)
			throws MissingConfigurationPropertyException;

	/**
	 * Add the outcome of the build test to the given test outcome collection.
	 *
	 * @param projectSubmission
	 *            the ProjectSubmission for which we're adding a build test
	 *            result
	 * @param passed
	 *            pass/fail status
	 * @param longDescription
	 *            compiler error messages (if any)
	 * @param started TODO
	 */
	private void addBuildTestResult(ProjectSubmission<?> projectSubmission,
			@OutcomeType String passed, String longDescription, long started) {
		TestOutcome outcome = new TestOutcome();
		outcome.setTestType(TestOutcome.TestType.BUILD);
		outcome.setTestName("Build Test");
		outcome.setOutcome(passed);
		outcome.setShortTestResult("Build test " + passed);
		outcome.setDetails(null);
		outcome.setLongTestResultCompressIfNeeded(longDescription);
		outcome.setTestNumber("0");
		outcome.setExceptionClassName("");

		outcome.setExecutionTimeMillis(System.currentTimeMillis() - started);

		projectSubmission.getTestOutcomeCollection().add(outcome);
	}

	/**
	 * Add a special failure test outcome indicating that a particular type of
	 * test could not be run because the project did not compile.
	 *
	 * @param projectSubmission
	 *            the ProjectSubmission
	 * @param testType
	 *            the test type
	 * @param longTestResult
	 *            compiler output from the failed build
	 */
	private void addSpecialFailureTestOutcome(
			ProjectSubmission<?> projectSubmission, TestType testType,
			String longTestResult) {
		TestOutcome outcome = new TestOutcome();

		outcome.setTestType(testType);
		outcome.setTestName("All " + testType + " tests");
		outcome.setOutcome(TestOutcome.COULD_NOT_RUN);
		outcome.setShortTestResult(testType
				+ " tests could not be run because the project did not compile");
		outcome.setLongTestResultCompressIfNeeded(longTestResult);
		outcome.setTestNumber("0");

		projectSubmission.getTestOutcomeCollection().add(outcome);

	}

	/**
	 * Build and run tests on given project submission.
	 *
	 * @param projectSubmission
	 *            the ProjectSubmission
	 * @throws CompileFailureException
	 *             if the project can't be compiled
	 * @throws BuilderException
	 * @throws IOException
	 */
	private <T extends TestProperties> void buildAndTestProject(ProjectSubmission<T> projectSubmission)
			throws CompileFailureException,
			MissingConfigurationPropertyException, IOException,
			BuilderException {
		// FIXME Should throw InternalBuildServerException instead of
		// BuilderException
		// Need to differentiate between problems with test-setup and bugs in my
		// servers
		File buildDirectory = getBuildServerConfiguration().getBuildDirectory();

		// Extract test properties and security policy files into build
		// directory
		TestPropertiesExtractor testPropertiesExtractor = null;
		try {
			testPropertiesExtractor = new TestPropertiesExtractor(
					projectSubmission.getTestSetup());
			testPropertiesExtractor.extract(buildDirectory);
		} catch (ZipExtractorException e) {
			throw new BuilderException(e);
		}

		// We absolutely have to have test.properties
		if (!testPropertiesExtractor.extractedTestProperties())
			throw new BuilderException(
					"Test setup did not contain test.properties");

		// Load test.properties
		File testPropertiesFile = new File(buildDirectory, "test.properties");
		T testProperties;
		try {
		    testProperties =  (T) TestProperties.load(testPropertiesFile);
		} catch (MissingRequiredTestPropertyException e) {
			throw new BuilderException(e.getMessage(), e);
		}

		// Set test properties in the ProjectSubmission.
		projectSubmission.setTestProperties(testProperties);

		// Create a BuilderAndTesterFactory, based on the language specified
		// in the test properties file
		BuilderAndTesterFactory<T> builderAndTesterFactory = projectSubmission
				.createBuilderAndTesterFactory();

		builderAndTesterFactory.buildAndTest(buildDirectory, testPropertiesExtractor);
	}

 
	/**
	 * Delete all files in given directory.
	 *
	 * @param dir
	 *            the directory
	 */
	protected static void cleanUpDirectory(File dir) {
		if (dir.isDirectory()) {
			File[] contents = dir.listFiles();
			if (contents != null) {
				for (int i = 0; i < contents.length; ++i) {
					deleteRecursive(contents[i]);
				}
			}
		}
	}

	/**
	 * Recursively delete file and all subdirectories (if any). This is done on
	 * a best-effort basis: no errors are reported if we can't delete something.
	 *
	 * @param file
	 *            the file or directory to delete
	 */
	private static void deleteRecursive(File file) {
		if (file.isDirectory()) {
			File[] contents = file.listFiles();
			if (contents != null) {
				for (int i = 0; i < contents.length; ++i)
					deleteRecursive(contents[i]);
			}
		}
		if (!file.delete())
			getBuildServerLog().warn(
					"Unable to delete " + file.getAbsolutePath());
	}

	Random random = new Random();
	/**
	 * Sleep for a while.
	 */
	private void sleep() throws InterruptedException {
		// Exponential decay: increase sleep time by a factor of
		// two each time submit server is polled and there is
		// no work (but capped at 2^MAX_SLEEP seconds).

		// TODO: We might want to add a random factor in here,
		// to prevent multiple build servers from getting
		// in lockstep.

		if (noWorkCount > MAX_SLEEP)
			noWorkCount = MAX_SLEEP;
		int sleepTime = (1 << noWorkCount) * 1000;
		log.trace("Sleeping for " + sleepTime + " milliseconds");
		System.gc();
		Thread.sleep(sleepTime + random.nextInt(sleepTime));

		++noWorkCount;
	}

	/**
	 * Sleeps for a given number of a millis. Logs an error if interrupted
	 * exception happens but doesn't throw the exception since it should never
	 * happen.
	 *
	 * @param millis
	 *            number of millis to sleep for
	 */
	protected void pause(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			log.error("Someone interrupted the pause() method", e);
		}
	}

	/*
	 * This is here because BuildServer used to be a concrete class. Now the
	 * daemon-mode functionality is in BuildServerDaemon.
	 */
	public static void main(String[] args) throws Exception {
		BuildServerDaemon.main(args);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see edu.umd.cs.buildServer.BuildServerMBean#getNumServerLoopIterations()
	 */
	public int getNumServerLoopIterations() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @return Returns the shutdownRequested.
	 */
	public boolean getDoNotLoop() {
		return config.getDebugProperty(DEBUG_DO_NOT_LOOP);
	}

	/**
	 * @param shutdownRequested
	 *            The shutdownRequested to set.
	 */
	public void setDoNotLoop(boolean shutdownRequested) {
		config.setProperty(DEBUG_DO_NOT_LOOP, "true");
	}
	public boolean getDownloadOnly() {
		return config.getDebugProperty(DOWNLOAD_ONLY);
	}
	
	public void setDownloadOnly(boolean downloadOnly) {
		config.setProperty(DOWNLOAD_ONLY, "true");
	}
}

// vim:ts=4

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
 * Created on Jan 22, 2004
 */
package edu.umd.cs.eclipse.courseProjectManager;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.CommandOutputListener;
import org.eclipse.team.internal.ccvs.core.client.Commit;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.listeners.IUpdateMessageListener;
import org.eclipse.team.internal.ccvs.core.client.listeners.UpdateListener;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Facade for CVS operations.
 * 
 * @author David Hovemeyer
 * @author jspacco
 */
@SuppressWarnings("restriction")
public class CVSOperations {
	// Operation failure reasons
	private static final int FAILURE_REASON_START = -100001; // Don't conflict
																// with CVS
																// status codes
	public static final int UNKNOWN_FAILURE = FAILURE_REASON_START + -1;
	public static final int FAILED_UP_TO_DATE_CHECK = FAILURE_REASON_START + -2;
	public static final int UNRESOLVED_MERGE_CONFLICT = FAILURE_REASON_START
			+ -3;
	public static final int FILE_IN_THE_WAY = FAILURE_REASON_START + -4;

	private static final String CPM_LOG = ".cpmLOG";

	private static String finalStatusCode(int code) {
		switch (code) {
		case UNKNOWN_FAILURE:
			return "UNKNOWN_FAILURE";
		case FAILED_UP_TO_DATE_CHECK:
			return "FAILED_UP_TO_DATE_CHECK";
		case UNRESOLVED_MERGE_CONFLICT:
			return "UNRESOLVED_MERGE_CONFLICT";
		case FILE_IN_THE_WAY:
			return "FILE_IN_THE_WAY";
		default:
			return "UNKNOWN STATUS CODE FOR CVS FAILURE REASONS";
		}
	}

	/*
	 * Uniform interface for a CVS operation ready to execute. It will be
	 * executed in the context of a WorkspaceModifyOperation, and thrown errors
	 * will be handled (and displayed to the user) to the best of our ability.
	 */
	private interface Operation {
		/**
		 * Execute the operation.
		 * 
		 * @param parent
		 *            parent Shell, null if none
		 * @param monitor
		 *            progress monitor
		 * @throws CoreException
		 * @throws InvocationTargetException
		 */
		public void execute(Shell parent, IProgressMonitor monitor)
				throws CoreException, InvocationTargetException,
				InterruptedException;

		/**
		 * Get descriptive name for the operation. This will be the task name
		 * used for the progress monitor, and it will also be used if an error
		 * dialog is required.
		 * 
		 * @return the operation name
		 */
		public String getOperationName();

		/**
		 * Get descriptive message describing the task performed by the
		 * operation.
		 * 
		 * @return the message describing the operation
		 */
		public String getMessage();

		public IResource[] getResources();

		public IProject getProject();
	}

	/**
	 * Hook for indication of the success or failure of a CVS operation. Giving
	 * the same OperationContext to two successive asynchronous operations can
	 * have the effect of ensuring that each operation proceeds only if the
	 * previous operation was successful.
	 */
	public interface OperationContext {
		public void success(Shell parent);

		public void failure(Shell parent);

		public boolean operationMayProceed();

		public String getConsequenceMessage(int statusCode);
	}

	/**
	 * Simple implementation of OperationContext that just records the success
	 * or failure of a CVS operation as a boolean. If the same SimpleContext is
	 * used for multiple asynchronous operations, it will ensure that each
	 * operation proceeds only if the previous operation was successful.
	 */
	public static class SimpleContext implements OperationContext {
		public boolean success = true;

		public void success(Shell parent) {
			this.success = true;
		}

		public void failure(Shell parent) {
			this.success = false;
		}

		public boolean operationMayProceed() {
			return success;
		}

		public String getConsequenceMessage(int statusCode) {
			return null;
		}
	}

	public static class AutoCVSCommitListener extends CommandOutputListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.team.internal.ccvs.core.client.listeners.
		 * ICommandOutputListener#errorLine(java.lang.String,
		 * org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation,
		 * org.eclipse.team.internal.ccvs.core.ICVSFolder,
		 * org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus errorLine(String line, ICVSRepositoryLocation location,
				ICVSFolder commandRoot, IProgressMonitor monitor) {
			if (line.indexOf("Examining") != -1) {
				AutoCVSPlugin.getPlugin().getEventLog().logMessage(line);
				return OK;
			}
			// TODO necessary to ignore normal CVS commit messages here but I
			// don't know under what circumstances cvs commit will generate
			// messages to stderr
			return super.errorLine(line, location, commandRoot, monitor);
		}
	}

	/**
	 * @author jspacco
	 * 
	 *         Ignores normal CVS output from the server to stderr:
	 * 
	 *         scheduling file `foo' for addition use 'cvs commit' to add this
	 *         file permanently
	 */
	public static class AutoCVSAddListener extends CommandOutputListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.team.internal.ccvs.core.client.listeners.
		 * ICommandOutputListener#errorLine(java.lang.String,
		 * org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation,
		 * org.eclipse.team.internal.ccvs.core.ICVSFolder,
		 * org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus errorLine(String line, ICVSRepositoryLocation location,
				ICVSFolder commandRoot, IProgressMonitor monitor) {
			if (line.indexOf("scheduling file") != -1
					|| line.indexOf("to add this file permanently") != -1
					|| line.indexOf("re-adding file") != -1
					|| line.indexOf("added independently by second party") != -1) {
				AutoCVSPlugin.getPlugin().getEventLog().logMessage(line);
				return OK;
			}
			return super.errorLine(line, location, commandRoot, monitor);
		}
	}

	public static class AutoCVSRemoveListener extends CommandOutputListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.team.internal.ccvs.core.client.listeners.
		 * ICommandOutputListener#errorLine(java.lang.String,
		 * org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation,
		 * org.eclipse.team.internal.ccvs.core.ICVSFolder,
		 * org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus errorLine(String line, ICVSRepositoryLocation location,
				ICVSFolder commandRoot, IProgressMonitor monitor) {
			if (line.indexOf("scheduled for removal") != -1) {
				AutoCVSPlugin.getPlugin().getEventLog().logMessage(line);
				return OK;
			}
			return super.errorLine(line, location, commandRoot, monitor);
		}
	}

	/**
	 * @author jspacco
	 * 
	 *         IUpdateMessageListener implementation. Prints to event log when
	 *         various things happen. Note that there are no "commit" and "add"
	 *         message listeners, and this sucks.
	 */
	public static class AutoCVSUpdateMessageListener implements
			IUpdateMessageListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.team.internal.ccvs.core.client.listeners.
		 * IUpdateMessageListener
		 * #directoryDoesNotExist(org.eclipse.team.internal
		 * .ccvs.core.ICVSFolder, java.lang.String)
		 */
		public void directoryDoesNotExist(ICVSFolder commandRoot, String path) {
			AutoCVSPlugin.getPlugin().getEventLog()
					.logMessage("directory does not exist" + path);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.team.internal.ccvs.core.client.listeners.
		 * IUpdateMessageListener
		 * #directoryInformation(org.eclipse.team.internal.ccvs.core.ICVSFolder,
		 * java.lang.String, boolean)
		 */
		public void directoryInformation(ICVSFolder commandRoot, String path,
				boolean newDirectory) {
			AutoCVSPlugin.getPlugin().getEventLog()
					.logMessage("updating directory " + path);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.team.internal.ccvs.core.client.listeners.
		 * IUpdateMessageListener
		 * #fileDoesNotExist(org.eclipse.team.internal.ccvs.core.ICVSFolder,
		 * java.lang.String)
		 */
		public void fileDoesNotExist(ICVSFolder parent, String filename) {
			AutoCVSPlugin.getPlugin().getEventLog()
					.logMessage("file does not exist" + filename);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.team.internal.ccvs.core.client.listeners.
		 * IUpdateMessageListener#fileInformation(int,
		 * org.eclipse.team.internal.ccvs.core.ICVSFolder, java.lang.String)
		 */
		public void fileInformation(int type, ICVSFolder parent, String filename) {
			AutoCVSPlugin
					.getPlugin()
					.getEventLog()
					.logMessage(
							"updating file " + filename
									+ "; type of update is " + type);
		}
	}

	// Constants
	/** Execute CVS operation synchronously (in calling thread). */
	public static final int SYNC = 0;

	/**
	 * Execute CVS operation asynchronously (scheduling it in the GUI dispatch
	 * thread).
	 */
	public static final int ASYNC = 1;

	/** Execute CVS operation in a top level dialog. */
	public static final int IN_DIALOG = 0;

	/** Execute CVS operation in the status bar of the active workspace window. */
	public static final int IN_STATUS_BAR = 1;

	/** Execute CVS operation using a BusyIndicator */
	public static final int BUSY_INDICATOR = 2;

	private static void validateSyncMode(int syncMode) {
		if (syncMode != SYNC && syncMode != ASYNC)
			throw new IllegalStateException(
					"Invalid sync mode for cvs operation (should be either SYNC or ASYN): "
							+ syncMode);
	}

	public static void commit(final IResource[] resources, int syncMode,
			final OperationContext context) throws CoreException {
		commit(resources, syncMode, IN_STATUS_BAR, context);
	}

	/**
	 * Commit operation
	 * 
	 * @param theResources
	 *            The resources to commit
	 * @param syncMode
	 *            Synchronicity of operation
	 * @param context
	 *            Can veto operations
	 * @throws CoreException
	 */
	public static void commit(final IResource[] theResources, int syncMode,
			int how, final OperationContext context) throws CoreException {
		if (theResources == null || theResources.length == 0)
			return;

		final IProject project = validateResourcesAndSyncMode(theResources,
				syncMode);

		AutoCVSPlugin.getPlugin().getEventLog()
				.logMessage("Auto cvs commit for project " + project.getName());

		final MyCVSCommand cvsCommand = new MyCVSCommand(theResources,
				new LocalOption[] { Commit.makeArgumentOption(
						Command.MESSAGE_OPTION, "AutoCVS Commit") }) {
			protected IStatus execute() throws CVSException {
				return Command.COMMIT.execute(session,
						Command.NO_GLOBAL_OPTIONS, options, resources,
						new AutoCVSCommitListener(), monitor);
			}

			public String getTaskName() {
				return "Auto Commit Operation";
			}

			public String getTaskName(CVSTeamProvider provider) {
				return "Auto Commit Operation";
			}
		};

		final Operation runnable = new Operation() {
			public void execute(Shell parent, IProgressMonitor monitor)
					throws CoreException, InterruptedException {

				IProject project = null;
				for (int i = 0; i < theResources.length; i++) {
					IProject rProject = theResources[i].getProject();
					if (rProject == null)
						throw new IllegalStateException(
								"Adding a resource with no project!");
					if (project == null)
						project = rProject;
					else if (project != rProject)
						throw new CVSException(
								"Internal error: Adding resources from multiple projects");
				}
				cvsCommand.executeCommand(monitor);
			}

			public String getOperationName() {
				return cvsCommand.getTaskName();
			}

			public String getMessage() {
				return "Message: Automatic CVS Commit Message";
			}

			public IResource[] getResources() {
				return theResources;
			}

			public IProject getProject() {
				return project;
			}
		};

		exec(runnable, syncMode, how, context);
	}

	/**
	 * creates an Update command
	 * 
	 * @param resources
	 *            array of resources to be updated
	 * @param syncMode
	 *            synchronicity of the cvs operation
	 * @param context
	 *            can veto changes
	 * @throws CoreException
	 */
	public static void update(final IProject project, int syncMode,
			final OperationContext context) throws CoreException {
		final CVSTeamProvider provider = CVSOperations.getProvider(project);
		if (provider == null)
			throw new CVSException(
					"Internal error: No CVS provider for project "
							+ project.getName());

		ICVSResource[] cvsResources = provider.getCVSWorkspaceRoot()
				.getLocalRoot().members(ICVSFolder.MANAGED_MEMBERS);

		if (cvsResources == null || cvsResources.length == 0) {
			AutoCVSPlugin
					.getPlugin()
					.getEventLog()
					.logMessage(
							"No resources to manage for " + project.getName());
			return;
		}

		final IResource[] theResources = new IResource[cvsResources.length];
		for (int ii = 0; ii < theResources.length; ii++) {
			IResource resource = cvsResources[ii].getIResource();
			theResources[ii] = cvsResources[ii].getIResource();
		}

		validateSyncMode(syncMode);

		Debug.print("Performing automatic CVS update on project "
				+ project.getName() + " @" + System.identityHashCode(project));
		Debug.print("Project class is " + project.getClass().getName());

		AutoCVSPlugin.getPlugin().getEventLog()
				.logMessage("Auto cvs update for project " + project.getName());

		final MyCVSCommand cvsCommand = new MyCVSCommand(theResources,
				new LocalOption[] { Update.RETRIEVE_ABSENT_DIRECTORIES }) {
			public IStatus execute() throws CVSException {
				UpdateListener listener = new UpdateListener(
						new AutoCVSUpdateMessageListener());
				return Command.UPDATE.execute(session,
						Command.NO_GLOBAL_OPTIONS, options, resources,
						listener, monitor);
			}

			public String getTaskName() {
				return "Auto Update Operation";
			}

			public String getTaskName(CVSTeamProvider provider) {
				return "Auto Update Operation";
			}
		};

		final Operation runnable = new Operation() {
			public void execute(Shell parent, IProgressMonitor monitor)
					throws CoreException, InterruptedException {

				IProject project = null;
				for (int i = 0; i < theResources.length; i++) {
					IProject rProject = theResources[i].getProject();
					if (rProject == null)
						throw new IllegalStateException(
								"Updating a resource with no project!");
					if (project == null)
						project = rProject;
					else if (project != rProject)
						throw new CVSException(
								"Internal error: Updating resources from multiple projects");
				}
				cvsCommand.executeCommand(monitor);

			}

			public String getOperationName() {
				return cvsCommand.getTaskName();
			}

			public String getMessage() {
				return "Automatic CVS Update Message";
			}

			public IResource[] getResources() {
				return theResources;
			}

			public IProject getProject() {
				return project;
			}
		};

		exec(runnable, syncMode, IN_STATUS_BAR, context);
	}

	/**
	 * CVS Add
	 * 
	 * @param theResources
	 * @param syncMode
	 * @param context
	 * @throws CoreException
	 */
	public static void add(final IResource[] theResources, int syncMode,
			final OperationContext context) throws CoreException {
		if (theResources == null || theResources.length == 0)
			return;
		Debug.print("adding a cvs resource: " + theResources[0]);

		final IProject project = validateResourcesAndSyncMode(theResources,
				syncMode);

		AutoCVSPlugin.getPlugin().getEventLog()
				.logMessage("Auto cvs add for project " + project.getName());

		final MyCVSCommand cvsCommand = new MyCVSCommand(theResources,
				Command.NO_LOCAL_OPTIONS) {
			public IStatus execute() throws CVSException {
				return Command.ADD.execute(session, Command.NO_GLOBAL_OPTIONS,
						options, resources, new AutoCVSAddListener(), monitor);
			}

			public String getTaskName() {
				return "AutoCVS Add Operation";
			}

			public String getTaskName(CVSTeamProvider provider) {
				return "AutoCVS Add Operation";
			}
		};

		final Operation runnable = new Operation() {
			public void execute(Shell parent, IProgressMonitor monitor)
					throws CoreException, InterruptedException {

				IProject project = null;
				for (int i = 0; i < theResources.length; i++) {
					IProject rProject = theResources[i].getProject();
					if (rProject == null)
						throw new IllegalStateException(
								"Adding a resource with no project!");
					if (project == null)
						project = rProject;
					else if (project != rProject)
						throw new CVSException(
								"Internal error: Adding resources from multiple projects");
				}
				cvsCommand.executeCommand(monitor);
			}

			public String getOperationName() {
				return cvsCommand.getTaskName();
			}

			public String getMessage() {
				return "Automatic CVS Add Message";
			}

			public IResource[] getResources() {
				return theResources;
			}

			public IProject getProject() {
				return project;
			}
		};

		exec(runnable, syncMode, IN_STATUS_BAR, context);

		// Debug.print("Done with CVS command " + cvsCommand);
	}

	/**
	 * @param resources
	 * @param syncMode
	 * @return
	 * @throws CVSException
	 */
	private static IProject validateResourcesAndSyncMode(
			final IResource[] resources, int syncMode) throws CVSException {
		validateSyncMode(syncMode);

		// TODO I'm assuming all resources come from the same project
		// it's possible that this isn't true, so I should check this case
		IProject project = resources[0].getProject();

		/*
		 * Debug.print("Performing automatic CVS command on project " +
		 * project.getName() + " @" + System.identityHashCode(project));
		 * Debug.print("Project class is " + project.getClass().getName());
		 */

		final CVSTeamProvider provider = CVSOperations.getProvider(project);
		if (provider == null)
			throw new CVSException(
					"Internal error: No CVS provider for project "
							+ project.getName());
		return project;
	}

	public static void remove(final IResource[] theResources, int syncMode,
			final OperationContext context) throws CoreException {
		if (theResources == null || theResources.length == 0)
			return;

		final IProject project = validateResourcesAndSyncMode(theResources,
				syncMode);

		AutoCVSPlugin.getPlugin().getEventLog()
				.logMessage("Auto cvs remove for project " + project.getName());

		final MyCVSCommand cvsCommand = new MyCVSCommand(theResources,
				Command.NO_LOCAL_OPTIONS) {
			public IStatus execute() throws CVSException {
				return Command.REMOVE.execute(session,
						Command.NO_GLOBAL_OPTIONS, options, resources,
						new AutoCVSRemoveListener(), monitor);
			}

			public String getTaskName() {
				return "AutoCVS Remove Operation";
			}

			public String getTaskName(CVSTeamProvider provider) {
				return "AutoCVS Remove Operation";
			}
		};

		final Operation runnable = new Operation() {
			public void execute(Shell parent, IProgressMonitor monitor)
					throws CoreException, InterruptedException {

				IProject project = null;
				for (int i = 0; i < theResources.length; i++) {
					IProject rProject = theResources[i].getProject();
					if (rProject == null)
						throw new IllegalStateException(
								"Removing a resource with no project!");
					if (project == null)
						project = rProject;
					else if (project != rProject)
						throw new CVSException(
								"Internal error: Removing resources from multiple projects");
				}
				cvsCommand.executeCommand();
			}

			public String getOperationName() {
				return cvsCommand.getTaskName();
			}

			public String getMessage() {
				return "Automatic CVS Remove Operation";
			}

			public IResource[] getResources() {
				return theResources;
			}

			public IProject getProject() {
				return project;
			}
		};

		exec(runnable, syncMode, IN_STATUS_BAR, context);
	}

	/**
	 * @deprecated Converts an array of IResources to an array of ICVSResources.
	 * @param arr
	 *            the array of IResources
	 * @return an array of ICVSResources
	 */
	private static ICVSResource[] convertIResourcesToICVSResources(
			IResource[] arr) {
		ICVSResource[] result = new ICVSResource[arr.length];
		for (int ii = 0; ii < arr.length; ii++) {
			result[ii] = CVSWorkspaceRoot.getCVSResourceFor(arr[ii]);
		}
		return result;
	}

	private static class TagSetHolder {
		public CVSTag[] tagSet;
	}

	/**
	 * Get list of CVS tags for given resource. This operation must be performed
	 * synchronously.
	 * 
	 * @param resource
	 *            the resource
	 * @param how
	 *            either CVSOperations.IN_DIALOG or CVSOperations.IN_STATUS_BAR
	 * @return list of cvs tags, or null if couldn't fetch the tags
	 * @throws TeamException
	 */
	public static CVSTag[] fetchTags(final Shell parent,
			final IResource resource, int how) {
		final TagSetHolder tagSetHolder = new TagSetHolder();

		Operation runnable = new Operation() {
			public void execute(Shell parent, IProgressMonitor monitor)
					throws CoreException {
				if (!AutoCVSPlugin.isCVSManaged(resource))
					throw new CVSException("Resource " + resource.getName()
							+ " is not CVS managed");

				// Get the ICVSRemoteFile
				ICVSResource projectCVSResource = CVSWorkspaceRoot
						.getCVSResourceFor(resource);
				ICVSRemoteResource projectRemoteResource = CVSWorkspaceRoot
						.getRemoteResourceFor(projectCVSResource);
				if (!(projectRemoteResource instanceof ICVSRemoteFile))
					throw new CVSException("Resource " + resource.getName()
							+ " is not a CVS-managed file");
				ICVSRemoteFile remoteFile = (ICVSRemoteFile) projectRemoteResource;

				ArrayList<CVSTag> tagList = new ArrayList<CVSTag>();

				// Fetch log entries for the file.
				ILogEntry[] logEntryList = remoteFile.getLogEntries(monitor);
				for (int i = 0; i < logEntryList.length; i++) {
					ILogEntry logEntry = logEntryList[i];
					CVSTag[] entryTagList = logEntry.getTags();
					for (int j = 0; j < entryTagList.length; j++) {
						tagList.add(entryTagList[j]);
					}
				}
				Debug.print("Found " + tagList.size() + " tags for project");

				// Successfully got the tags. Put them in the TagSetHolder
				tagSetHolder.tagSet = tagList
						.toArray(new CVSTag[tagList.size()]);
			}

			public String getOperationName() {
				return "Fetch CVS Tags for Project";
			}

			public String getMessage() {
				return "Fetch tags describing assignments from repository";
			}

			public IResource[] getResources() {
				IResource[] tmp = new IResource[1];
				tmp[0] = resource;
				return tmp;
			}

			public IProject getProject() {
				return resource.getProject();
			}
		};

		syncExec(runnable, how, null);

		return tagSetHolder.tagSet;
	}

	/**
	 * Tag all managed resources in given project. The tag is a plain revision
	 * tag, not a branch tag.
	 * 
	 * @param project
	 *            the project to tag
	 * @param tagName
	 *            name of the tag
	 */
	public static void tagProject(final IProject project, final String tagName,
			int syncMode) {
		validateSyncMode(syncMode);
		Debug.print("tagProject() called");
		Operation runnable = new Operation() {
			public void execute(Shell parent, IProgressMonitor monitor)
					throws CoreException {
				ICVSResource cvsResource = CVSWorkspaceRoot
						.getCVSResourceFor(project);
				if (cvsResource == null)
					throw new CVSException("Couldn't get ICVSResource for "
							+ project.getName());
				ICVSRemoteResource remoteResource = CVSWorkspaceRoot
						.getRemoteResourceFor(cvsResource);
				if (remoteResource == null)
					throw new CVSException(
							"Couldn't get ICVSRemoteResource for "
									+ project.getName());

				if (!(remoteResource instanceof ICVSRemoteFolder))
					throw new CVSException("Couldn't get ICVSRemoteFolder for "
							+ project.getName());

				CVSTag tag = new CVSTag(tagName, CVSTag.VERSION);

				IStatus status = CVSTag.validateTagName(tagName);

				if (!status.isOK()) {
					Debug.print("Illegal tag " + tagName + " with status: "
							+ status);
					return;
				}

				monitor.setTaskName("Tagging project files for submission");
				AutoCVSPlugin
						.getPlugin()
						.getEventLog()
						.logMessage(
								"Tagging project " + project.getName()
										+ " with tag " + tag.getName());
				Debug.print("Adding tag " + tagName + " to project");
				Debug.print("Remote resource tagged is: "
						+ remoteResource.getName());
				remoteResource.tag(tag, Command.NO_LOCAL_OPTIONS, monitor);

				monitor.setTaskName("Tagging done");
			}

			public String getOperationName() {
				return "CVS Tag Project Files";
			}

			public String getMessage() {
				return "Tag files in project to mark them as submitted";
			}

			public IResource[] getResources() {
				IResource[] tmp = new IResource[1];
				tmp[0] = project;
				return tmp;
			}

			public IProject getProject() {
				return project;
			}
		};

		exec(runnable, syncMode, IN_STATUS_BAR, null);
	}

	private static String resourceArrayToString(IResource[] resourceList) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < resourceList.length; i++) {
			buf.append(resourceList[i].getName());
			if (i < resourceList.length - 1)
				buf.append(',');
		}
		return buf.toString();
	}

	/**
	 * Get CVSTeamProvider which is the repository provider for given resource.
	 * Returns null if the resource has no provider, or if the provider is not
	 * CVS.
	 * 
	 * @param resource
	 *            the resource
	 * @return the CVSRepositoryProvider for the project, or null if the
	 *         provider is not CVS
	 */
	public static CVSTeamProvider getProvider(IResource resource) {
		RepositoryProvider provider = RepositoryProvider.getProvider(resource
				.getProject());
		if (provider == null || !(provider instanceof CVSTeamProvider))
			return null;
		return (CVSTeamProvider) provider;
	}

	/**
	 * Get the single CVSTeamProvider managing all of a list of resources.
	 * Returns null if any of the resources are unmanaged, or if there are
	 * multiple providers.
	 * 
	 * @param resourceList
	 *            list of resources
	 * @return the single CVSTeamProvider, or null if there is no single
	 *         provider for all of the resources
	 */
	public static CVSTeamProvider getSingleProvider(IResource[] resourceList) {
		CVSTeamProvider provider = null;
		for (int i = 0; i < resourceList.length; ++i) {
			IResource resource = resourceList[i];

			CVSTeamProvider resProvider = getProvider(resource);
			if (resProvider == null) {
				AutoCVSPlugin
						.getPlugin()
						.getEventLog()
						.logError(
								"Resource " + resource.getName()
										+ " has no RepositoryProvider");
				return null;
			}

			if (provider == null)
				provider = resProvider;
			else if (provider != resProvider) {
				AutoCVSPlugin
						.getPlugin()
						.getEventLog()
						.logError("Multiple repository providers for resources");
				return null;
			}
		}
		return provider;
	}

	private static void exec(Operation operation, int syncMode, int how,
			OperationContext context) {
		if (syncMode == SYNC) {
			syncExec(operation, how, context);
		} else if (syncMode == ASYNC) {
			asyncExec(operation, how, context);
		} else
			throw new IllegalStateException("Illegal syncMode");
	}

	/**
	 * Run a CVS operation asynchronously in a progress dialog or workbench
	 * status bar.
	 * 
	 * @param runnable
	 *            the operation
	 * @param how
	 *            IN_STATUS_BAR to request running using the status bar,
	 *            IN_DIALOG to request running in a progress dialog
	 * @param context
	 *            the OperationContext
	 */
	private static void asyncExec(final Operation runnable, final int how,
			final OperationContext context) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				syncExec(runnable, how, context);
			}
		});
	}

	/**
	 * Run a CVS operation synchronously (in the context of the current thread),
	 * in a progress dialog or workbench status bar. Displays an error dialog if
	 * the CVS operation does not complete successfully.
	 * <p>
	 * <b> NOTE: </b> I'm not sure this method works for adding files because it
	 * keeps raising org.eclipse.swt.SWTException: Invalid thread access (error
	 * code = 22 (ERROR_THREAD_INVALID_ACCESS)), which means that the wrong
	 * thread is trying to do something.
	 * 
	 * @param runnable
	 *            the operation
	 * @param how
	 *            IN_STATUS_BAR to request running using the status bar,
	 *            IN_DIALOG to request running in a progress dialog
	 * @param context
	 *            the OperationContext
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	private static void syncExec(final Operation cvsOp, final int how,
			final OperationContext context) {
		// The Operation context, if present, may veto this entire operation.
		// This generally means that a previous asynchronously scheduled
		// operation
		// failed.
		final IWorkbenchWindow wwin = AutoCVSPlugin.getActiveWorkbenchWindow();
		final Shell parent = (wwin != null) ? wwin.getShell() : null;

		try {
			Debug.print("syncExec: " + cvsOp.getOperationName() + " "
					+ cvsOp.getMessage() + " "
					+ Thread.currentThread().getName());
			if (context != null && !context.operationMayProceed()) {
				Debug.print("Context is vetoing the following operation: "
						+ cvsOp.getOperationName());
				return;
			}

			// NOTE that the current plugin never uses the BUSY_INDICATOR option
			if (how == BUSY_INDICATOR) {
				Debug.print("BUSY_INDICATOR\n");
				Runnable task = new Runnable() {
					public void run() {
						try {
							cvsOp.execute(null, null);
						} catch (Exception e) {
							// TODO what to do when BusyIndicator handles an
							// exception?
							System.err.println("Exception! " + e);
						}
					}
				};

				org.eclipse.swt.custom.BusyIndicator.showWhile(null, task);
				return;
			}

			ISchedulingRule rule;
			IResource[] resources = cvsOp.getResources();
			if (false && resources.length == 1)
				rule = resources[0];
			else
				rule = cvsOp.getProject();
			rule = ResourcesPlugin.getWorkspace().getRoot();
			// Wrap the Operation in a WorkspaceModifyOperation

			WorkspaceModifyOperation runnable = new WorkspaceModifyOperation(
					rule) {
				protected void execute(IProgressMonitor monitor)
						throws CoreException, InvocationTargetException,
						InterruptedException {
					try {
						monitor.setTaskName(cvsOp.getOperationName());
						cvsOp.execute(parent, monitor);
					} finally {
						monitor.done();
					}
				}
			};

			try {
				if (how == IN_STATUS_BAR) {
					boolean bail = false;
					Debug.print("IN_STATUS_BAR");
					if (false)
						for (StackTraceElement element : new RuntimeException()
								.getStackTrace()) {
							if (element
									.getClassName()
									.equals("org.eclipse.jface.operation.ModalContext$ModalContextThread")
									&& element.getMethodName().equals("block")) {
								Debug.print("invocation of  "
										+ cvsOp.getOperationName()
										+ " since we are being invoked from a model context");
								System.out
										.println("invocation of  "
												+ cvsOp.getOperationName()
												+ " since we are being invoked from a model context");
								bail = true;
							}
						}
					if (!bail && wwin != null) {
						// wwin.run(false, true, runnable);
						wwin.run(true, true, runnable);
						if (context != null)
							context.success(parent);
						return;
					}
				}

				// A modal progress dialog is the fall back for the
				// case where we can't get a handle on the workbench window.
				Debug.print("modal progress dialog rather than IN_STATUS_BAR");
				System.out
						.println("modal progress dialog rather than IN_STATUS_BAR");

				ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
				try {

					dialog.run(false, true, runnable);
				} catch (SWTException e) {
					System.out.println("Error code: " + e.code);
					if (e.getCause() != null)
						System.out.println("Cause: " + e.getCause());
					else
						System.out.println("Cause is null!");
					throw e;
				}
				context.success(parent);
				return;
			} catch (Throwable e) {
				Debug.print("Exception thrown by runnable");
				// Failed operation!
				if (context != null)
					context.failure(parent);

				Debug.print("Exception, about to call parseStatus(): ", e);

				// Attempt to figure out the reason for the failure from the
				// exception.
				int statusCode = parseStatus(e);

				// Build a detailed message describing the failure and its
				// consequences.
				StringBuffer msg = new StringBuffer();
				msg.append("The following operation failed: "
						+ cvsOp.getOperationName());
				String consequenceMessage = null;
				if (context != null)
					consequenceMessage = context
							.getConsequenceMessage(statusCode);
				if (consequenceMessage != null) {
					msg.append("\n\n");
					msg.append(consequenceMessage);
				}

				if (AutoCVSPlugin.getPlugin().hasFailedOperation()) {
					// Add the generic message about how automatic CVS commands
					// are disabled.
					msg.append("\n\n");
					msg.append(AutoCVSPlugin.getMessage("error.offline"));
				}

				// Alert the user.
				Dialogs.errorDialog(parent,
						"Warning: " + cvsOp.getOperationName() + " failed",
						msg.toString(), e);
				if (context != null)
					context.failure(parent);
				else
					Debug.print("context is null!");
			}
		} catch (RuntimeException e) {
			Dialogs.errorDialog(parent, "Warning: runtime exception during "
					+ cvsOp.getOperationName() + " failed", e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * Ad-hoc parsing of status messages to try to glean information about an
	 * error.
	 * 
	 * @param e
	 *            The exception thrown by a CVS operation
	 * @return return a status code - hopefully, one of the failure reason codes
	 *         defined in this class
	 */
	private static int parseStatus(Throwable e) {
		int statusCode = 0;

		// Try to get status
		Throwable cause = e;
		if (e instanceof InvocationTargetException)
			cause = ((InvocationTargetException) e).getTargetException();

		IStatus status = null;
		if (cause instanceof CoreException) {
			CoreException ce = (CoreException) cause;
			status = ce.getStatus();
			Debug.print("status code = " + status.getCode());
			statusCode = status.getCode(); // generally useless, but who knows?

			List<String> statusMessageList = getStatusMessages(status);
			for (Iterator<String> iter = statusMessageList.iterator(); iter
					.hasNext();) {
				String element = iter.next();
				Debug.print("status error message: " + element);
				if (element.indexOf("Up-to-date check") >= 0) {
					statusCode = FAILED_UP_TO_DATE_CHECK;
					break;
				} else if (element.indexOf("had a conflict") >= 0) {
					statusCode = UNRESOLVED_MERGE_CONFLICT;
					break;
				} else if (element.indexOf("in the way") >= 0
						|| element.indexOf("added independently") >= 0
						|| element.indexOf("already exists") >= 0) {
					statusCode = FILE_IN_THE_WAY;
					break;
				}
			}
		}
		Debug.print("Final status code = " + statusCode
				+ ", final status reason = " + finalStatusCode(statusCode));

		Debug.print("all status messages as string: "
				+ getStatusMessagesAsString(status));

		return statusCode;
	}

	private static List<String> getStatusMessages(IStatus status) {
		ArrayList<String> list = new ArrayList<String>();
		list.add(status.getMessage());
		IStatus childList[] = status.getChildren();
		for (int i = 0; i < childList.length; i++) {
			list.add(childList[i].getMessage());
		}
		return list;
	}

	public static String getStatusMessagesAsString(IStatus status) {
		if (status == null)
			return "null";
		List<String> list = getStatusMessages(status);
		Iterator<String> it = list.iterator();
		StringBuffer res = new StringBuffer();
		while (it.hasNext()) {
			res.append(it.next());
			res.append("\n");
		}
		return res.toString();
	}

	public static String getType(IResource resource) {
		int type = resource.getType();
		switch (type) {
		case IResource.FILE:
			return "FILE";
		case IResource.FOLDER:
			return "FOLDER";
		case IResource.ROOT:
			return "ROOT";
		case IResource.PROJECT:
			return "PROJECT";
		default:
			return "UNKNOWN IResource type";
		}
	}

	public static String getType(ICVSResource resource) {
		if (resource.isFolder())
			return "FOLDER";
		return "FILE";
	}

	public static void printResources(IResource[] resources) {
		for (int ii = 0; ii < resources.length; ii++) {
			IResource res = resources[ii];
			System.out.println("Resource " + res.getFullPath() + " is of type "
					+ getType(res));
		}
	}

	public static void printResources(ICVSResource[] resources) {
		for (int ii = 0; ii < resources.length; ii++) {
			ICVSResource res = resources[ii];

			try {
				System.out.println("Resource "
						+ res.getRepositoryRelativePath() + " is of type "
						+ getType(res));
			} catch (CVSException e) {
				System.out.println("Exception in printResources: " + e);
			}
		}
	}
}

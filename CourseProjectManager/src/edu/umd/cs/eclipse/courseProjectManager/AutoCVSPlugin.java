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

import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
@SuppressWarnings("restriction")
public class AutoCVSPlugin extends AbstractUIPlugin implements IStartup {

	static final String ID = "edu.umd.cs.eclipse.courseProjectManager";

	static final String DISABLED = "disabled";
	static final String ENABLED = "enabled";
	/** Nature id for the AutoCVS nature. */
	public static final String AUTO_CVS_NATURE = ID + ".autoCVSNature";
	/** Nature id for the AutoSync nature. */
	public static final String AUTO_SYNC_NATURE = ID + ".autoSyncNature";

	public static final String AUTO_RUN_LOG_NATURE = ID + ".autoRunLogNature";
	/**
	 * Set to true if any automatic CVS operation fails. Upon the failure of an
	 * AutoCVS operation, we inform the user and disable automatic execution for
	 * the remainder of the Eclipse session.
	 */
	private boolean failedOperation;
	static final String SUBMITIGNORE = ".submitIgnore";
	static final String SUBMITINCLUDE = ".submitInclude";
	static final String SUBMITUSER = ".submitUser";
	static final String SUBMITPROJECT = ".submit";

	private EventLog eventLog;
	private ResourceBundle messageBundle;
	private IResourceChangeListener listener;
	/** The shared instance */
	private static AutoCVSPlugin plugin;

	/**
	 * Object used to accumulate modifications made to a project's files.
	 */
	private static class ModificationBundle {
		private ArrayList<IResource> addedFiles = new ArrayList<IResource>();
		private ArrayList<IResource> modifiedFiles = new ArrayList<IResource>();
		private ArrayList<IResource> removedFiles = new ArrayList<IResource>();

		public void addedFile(IResource resource) {
			addedFiles.add(resource);
			modifiedFiles.add(resource); // added resources also need to be
											// committed
		}

		public void removedFile(IResource resource) {
			removedFiles.add(resource);
			modifiedFiles.add(resource);
		}

		public void modifiedFile(IResource resource) {
			modifiedFiles.add(resource);
		}

		public boolean hasAddedFiles() {
			return !addedFiles.isEmpty();
		}

		public boolean hasModifiedFiles() {
			return !modifiedFiles.isEmpty();
		}

		public boolean hasRemovedFiles() {
			return !removedFiles.isEmpty();
		}

		public IResource[] getFilesToAdd() {
			return addedFiles.toArray(new IResource[addedFiles.size()]);
		}

		public IResource[] getFilesToCommit() {
			return modifiedFiles.toArray(new IResource[modifiedFiles.size()]);
		}

		public IResource[] getFilesToRemove() {
			return removedFiles.toArray(new IResource[removedFiles.size()]);
		}
	}

	/**
	 * A resource change listener to capture resource modification events.
	 * 
	 * @author David Hovemeyer
	 */
	private class AutoCVSResourceModificationListener implements
			IResourceChangeListener {
		final class AutoCVSDeltaVisitor implements IResourceDeltaVisitor {
			private final ModificationBundle bundle;

			IProject projectBeingModified = null;

			boolean multipleProjectsBeingModified = false;

			AutoCVSDeltaVisitor(ModificationBundle bundle) {
				super();
				this.bundle = bundle;
			}

			public boolean visit(IResourceDelta delta) {
				int kind = delta.getKind();
				int flags = delta.getFlags();
				IResource resource = delta.getResource();
				IProject project = resource.getProject();

				try {
					if (project == null)
						return true; // This is the workspace root. Continue
										// visiting children.

					if (!project.isOpen())
						return false; // Project is not open yet - don't
										// continue visiting children

					// Do not proceed if AutoSync is disabled.
					boolean autoSync = AutoCVSPlugin.getPlugin().getAutoSync(
							project);
					if (!autoSync)
						return false; // No need to continue visiting children

					if (project == resource
							&& (kind & IResourceDelta.ADDED) != 0) {
						// Project is being added.
						// We see this when a project is checked out for the
						// first time.
						// In this case, we definitely do not want to perform
						// any CVS operations on it.
						Debug.print("Project " + project.getName() + " added");
						return false; // Do not continue visiting children
					}

					// There are several things to do here.
					// - If the resource's project is not AutoCVS managed,
					// ignore it.
					// - If the resource is of an ignored resource type,
					// ignore it.
					// - If the resource has been added:
					// Schedule it to be added to CVS.
					// - If the resource's content is changed:
					// 1. If it isn't in CVS, schedule it to be added (a
					// safeguard: should never happen)
					// 2. Schedule it for a commit
					// - If the resource has been removed:
					// 1. Schedule it for a remove
					// 2. Schedule it for a commit

					if (!hasAutoCVSNature(project))
						return false; // Not an AutoCVS project: no need to
										// continue visiting children

					if (isCVSIgnored(resource))
						return false; // Resource ignored by CVS

					// Do not add or commit .class files under any circumstances
					if (!(resource instanceof IContainer)
							&& resource.getName().endsWith((".class"))) {
						Debug.print("Class file "
								+ resource.getName()
								+ " is not ignored by CVS (i.e. through .cvsignore) but we will not process it with CVS");
						return false;
					}
					if (projectBeingModified == null)
						projectBeingModified = project;
					else if (!projectBeingModified.equals(project))
						multipleProjectsBeingModified = true;
					if ((kind & IResourceDelta.ADDED) != 0
							&& !isCVSManaged(resource)) {
						Debug.print("added resource: " + resource.getName()
								+ " of type " + CVSOperations.getType(resource));
						bundle.addedFile(resource);
					} else if ((flags & IResourceDelta.CONTENT) != 0) {
						if (!isCVSManaged(resource)) {
							// Paranoid case: resource is unexpectedly unmanaged
							if (usesCVS(project)) {
								AutoCVSPlugin
										.getPlugin()
										.getEventLog()
										.logMessage(
												"Warning: detected change to non-CVS-managed resource "
														+ resource.getName()
														+ ": scheduling it for addition");
								bundle.addedFile(resource);
							}
							// Note: added resources are automatically scheduled
							// for commit
						} else {
							// Usual case: changed resource is managed.
							// Schedule it for commit.
							bundle.modifiedFile(resource);
						}
					} else if ((kind & IResourceDelta.REMOVED) != 0
							&& isCVSManaged(resource)
							&& !isCVSIgnored(resource)) {
						// Note: Removed resources are automatically scheduled
						// for commit
						// Note: Only schedule FILEs for removal (not FOLDERs)
						if (resource.getType() == IResource.FILE) {
							bundle.removedFile(resource);
						} else
							Debug.print("Directory resource "
									+ resource.getName()
									+ " will not be CVS removed");
					}
				} catch (Throwable e) {
					eventLog.logError("Exception checking resource delta", e);
					// TODO: use marker to notify user?
				}
				return true;
			}
		}

		public void resourceChanged(IResourceChangeEvent event) {

			// Nothing to do if an automatic CVS operation has failed earlier.
			if (hasFailedOperation())
				return;

			// The resource delta represents all changed resources
			IResourceDelta delta = event.getDelta();
			if (delta == null)
				return; // No resources modified - could be project being
						// deleted

			// Accumulate resources that have been added or had their contents
			// changed
			final ModificationBundle bundle = new ModificationBundle();
			AutoCVSDeltaVisitor visitor = new AutoCVSDeltaVisitor(bundle);

			try {
				delta.accept(visitor);

				if (!bundle.hasAddedFiles() && !bundle.hasModifiedFiles()
						&& !bundle.hasRemovedFiles()) {
					Debug.print("No resources added, no managed resources were changed.");
					return;
				}
				if (visitor.projectBeingModified == null) {
					Debug.print("No project.");
					return;
				}
				// if (!usesCVS(visitor.projectBeingModified)) return;

				// Added resources must also be committed. (?)
				// changedResources.addAll(addedResources);

				// Create an OperationContext object to record the
				// success or failure of the add and commit operations.
				// This will also have the effect of circumventing the
				// commit operation if the add fails.
				final CVSOperations.SimpleContext context = new CVSOperations.SimpleContext() {
					@Override
					public void failure(Shell parent) {
						setFailedOperation(true);
					}

					@Override
					public String getConsequenceMessage(int statusCode) {
						if (statusCode == CVSOperations.FAILED_UP_TO_DATE_CHECK)
							return getMessage("failed.commit.notuptodate");
						else if (statusCode == CVSOperations.UNRESOLVED_MERGE_CONFLICT)
							return getMessage("failed.commit.mergeconflict");
						else
							return null;
					}
				};

				WorkspaceJob job = new WorkspaceJob("Auto CVS commit") {

					public @Override
					IStatus runInWorkspace(IProgressMonitor monitor)
							throws CoreException {
						// Add added resources.

						CVSOperations.add(bundle.getFilesToAdd(),
								CVSOperations.ASYNC, context);

						// Remove resources
						CVSOperations.remove(bundle.getFilesToRemove(),
								CVSOperations.ASYNC, context);

						// Commit changed resources.
						CVSOperations.commit(bundle.getFilesToCommit(),
								CVSOperations.ASYNC, context);

						return Status.OK_STATUS;
					}

				};
				ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRoot();
				if (visitor.projectBeingModified != null
						&& !visitor.multipleProjectsBeingModified)
					rule = visitor.projectBeingModified;
				job.setRule(rule);
				job.setPriority(Job.DECORATE);
				job.schedule();

			} catch (Exception e) {
				eventLog.logError("Exception in automatic commit", e);
				// TODO: use marker to notify user?
				return;
			}
		}
	}

	static boolean usesCVS(IProject project) {
		// Guaranteed to have a valid .submit file.
		IResource submitProjectFile = project
				.findMember(AutoCVSPlugin.SUBMITPROJECT);
		if (submitProjectFile == null)
			return false;
		CVSTeamProvider provider = CVSOperations.getProvider(submitProjectFile);
		return provider != null;
	}

	/**
	 * @author jspacco
	 * 
	 *         Catches run events and can log them to a standard file that
	 *         should log all events.
	 */
	private static class LaunchLogger implements ILaunchListener {
		public void launchAdded(ILaunch launch) {
			String projectName;
			IProject project = null;
			ILaunchConfiguration launchConfiguration = launch
					.getLaunchConfiguration();
			if (launchConfiguration == null)
				return;
			try {
				projectName = launchConfiguration
						.getAttribute("org.eclipse.jdt.launching.PROJECT_ATTR",
								(String) null);
				if (projectName == null) {
					AutoCVSPlugin
							.getPlugin()
							.getEventLog()
							.logMessage(
									"Unable to determine project that was just executed: "
											+ launch.getLaunchMode());
					Debug.print("Unable to determine project that was just executed: "
							+ launch.getLaunchMode());
					return;

				}
				project = ResourcesPlugin.getWorkspace().getRoot()
						.getProject(projectName);
				if (project == null) {
					Debug.print("Unable to find project " + projectName);
					return;
				}
				if (!usesCVS(project)) {
					Debug.print("project doesn use cvs, can't log run event "
							+ projectName);
					return;

				}

			} catch (CoreException e) {
				Debug.print("Unable to retrieve name of project", e);
				return;
			}

			String launchType = "";
			try {
				launchType = "-" + launchConfiguration.getType().getName();
			} catch (CoreException e) {
				Debug.print("Can't determine launch type " + e.getMessage());
			}

			EclipseLaunchEventLog.logEventToFile(launch.getLaunchMode()
					+ launchType, project);
		}

		public void launchRemoved(ILaunch launch) {
			// AutoCVSPlugin.getPlugin().getEventLog().logMessage("launch removed: "
			// +launch.getLaunchMode());
			// Debug.print(launch.getLaunchMode());
		}

		public void launchChanged(ILaunch launch) {
			// AutoCVSPlugin.getPlugin().getEventLog().logMessage("launch changed: "
			// +launch.getLaunchMode());
			// Debug.print(launch.getLaunchMode());
		}
	}

	public EventLog getEventLog() {
		return eventLog;
	}

	/**
	 * Get a message from the message bundle.
	 * 
	 * @param key
	 *            the key of the message to retrieve
	 * @return the message
	 */
	public static String getMessage(String key) {
		return getPlugin().messageBundle.getString(key);
	}

	public String getId() {
		return getBundle().getSymbolicName();
		// return getDescriptor().getUniqueIdentifier();
	}

	public String getVersion() {
		return (String) getBundle().getHeaders().get(
				org.osgi.framework.Constants.BUNDLE_VERSION);
		// new PluginVersionIdentifier(version);
	}

	/**
	 * Has any automatic CVS operation failed?
	 * 
	 * @return true if an update has failed, false if not
	 */
	public boolean hasFailedOperation() {
		return failedOperation;
	}

	/**
	 * The constructor.
	 */
	public AutoCVSPlugin() {
		plugin = this;
	}

	/**
	 * Determine whether given resource is managed by CVS.
	 * 
	 * @param resource
	 *            the resource
	 * @return true if resource is managed by CVS, false otherwise
	 * @throws CVSException
	 */
	static boolean isCVSManaged(IResource resource) throws CVSException {
		boolean result;
		ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
		if (cvsResource == null) {
			result = false;
		} else {
			result = cvsResource.isManaged();
		}
		return result;
	}

	/**
	 * Determine whether given resource is/should be ignored by CVS.
	 * 
	 * @param resource
	 *            the resource
	 * @return true if resource should be ignored, false if not
	 * @throws CVSException
	 */
	static boolean isCVSIgnored(IResource resource) throws CVSException {
		// Is this file a special file that should be ignored by CVS?
		// Currently the only such file is the courseProjectManager log file
		// (.cpmLOG)
		if (resource.getName().equals(EclipseLaunchEventLog.LOG_NAME)) {
			return true;
		}

		ICVSResource mResource = CVSWorkspaceRoot.getCVSResourceFor(resource);

		if (mResource != null && mResource.isIgnored())
			Debug.print("ignored resource: " + resource.getName());

		return mResource != null && mResource.isIgnored();
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		Debug.print("Starting up at " + new Date());

		this.eventLog = new EventLog();
		this.messageBundle = ResourceBundle
				.getBundle("edu.umd.cs.eclipse.courseProjectManager.Messages");
		// this.messageBundle =
		// ResourceBundle.getBundle("edu.umd.cs.courseProjectManager.Messages");

		// Install our resource modification listener
		this.listener = new AutoCVSResourceModificationListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);

		// Install our launch listener for logging Eclipse launch events.
		DebugPlugin debugPlugin = DebugPlugin.getDefault();
		debugPlugin.getLaunchManager().addLaunchListener(new LaunchLogger());

		// Update projects in workspace
		try {
			autoUpdateWorkspaceProjects();
		} catch (CoreException e) {
			eventLog.logError("Exception updating workspace projects", e);
		}
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static AutoCVSPlugin getPlugin() {
		return plugin;
	}

	/**
	 * Convenience method for getting the handle of the active workbench window.
	 * 
	 * @return the active workbench window, or null if we can't get it
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) {
			IWorkbenchWindow wwin = workbench.getActiveWorkbenchWindow();
			if (wwin == null)
				Debug.print("Could not get handle of active workbench window");
			return wwin;
		}
		return null;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin
				.imageDescriptorFromPlugin("AutoCVSPlugin", path);
	}

	/** Get value of AutoSync property for given project. */
	public boolean getAutoSync(IProject project) {
		return hasAutoSyncNature(project);
	}

	/**
	 * Add the AutoCVS nature to given project.
	 * 
	 * @param project
	 */
	public void addAutoCVSNature(IProject project) throws CoreException {
		addProjectNature(project, AUTO_CVS_NATURE);
	}

	public void addAutoSyncNature(IProject project) throws CoreException {
		addProjectNature(project, AUTO_SYNC_NATURE);
	}

	/**
	 * Remove the AutoCVS nature from given project.
	 * 
	 * @param project
	 */
	void removeAutoCVSNature(IProject project) throws CoreException {
		removeProjectNature(project, AUTO_CVS_NATURE);
	}

	void removeAutoSyncNature(IProject project) throws CoreException {
		removeProjectNature(project, AUTO_SYNC_NATURE);
	}

	private void removeProjectNature(IProject project, String natureId)
			throws CoreException {
		if (!hasProjectNature(project, natureId))
			return;

		IProjectDescription projectDescription = project.getDescription();
		String[] ids = projectDescription.getNatureIds();
		String[] updateIds = new String[ids.length - 1];
		int count = 0;
		for (int i = 0; i < ids.length; ++i) {
			if (!ids[i].equals(natureId))
				updateIds[count++] = ids[i];
		}

		projectDescription.setNatureIds(updateIds);
		project.setDescription(projectDescription, null);
	}

	/** Set the AutoSync property for given project. */
	public void setAutoSync(IProject project, boolean autoSync)
			throws CoreException {
		if (autoSync)
			addAutoSyncNature(project);
		else
			removeAutoSyncNature(project);
	}

	public void setAutoRunLog(IProject project, boolean autoRunLog)
			throws CoreException {
		if (autoRunLog) {
			addProjectNature(project, AUTO_RUN_LOG_NATURE);
			EclipseLaunchEventLog.logEventToFile(ENABLED, project);
		} else {
			removeProjectNature(project, AUTO_RUN_LOG_NATURE);
			EclipseLaunchEventLog.logEventToFile(DISABLED, project);
		}
	}

	private void addProjectNature(IProject project, String natureId)
			throws CoreException {
		if (hasProjectNature(project, natureId))
			return;

		IProjectDescription projectDescription = project.getDescription();
		String[] ids = projectDescription.getNatureIds();
		String[] updateIds = new String[ids.length + 1];
		System.arraycopy(ids, 0, updateIds, 0, ids.length);
		updateIds[ids.length] = natureId;

		projectDescription.setNatureIds(updateIds);
		project.setDescription(projectDescription, null);
		// project.setDescription(projectDescription, IResource.FORCE, null);
	}

	public static boolean hasAutoSyncNature(IProject project) {
		return hasProjectNature(project, AUTO_SYNC_NATURE);
	}

	private static boolean hasProjectNature(IProject project, String natureId) {
		try {
			return project.hasNature(natureId);
		} catch (CoreException e) {
			plugin.eventLog.logError("Exception getting project nature", e);
			return false;
		}
	}

	/**
	 * Attempt an automatic CVS update of given project
	 * 
	 * @param project
	 *            the project
	 */
	void attemptCVSUpdate(final IProject project, int syncMode)
			throws CoreException/*
								 * , InvocationTargetException,
								 * InterruptedException
								 */
	{
		// If AutoSync is disabled for the project, we just leave it alone.
		if (!getAutoSync(project))
			return;

		if (!usesCVS(project))
			return;

		// Don't attempt the update if a CVS operation has failed
		if (hasFailedOperation())
			return;

		// Create a callback action for successful update.
		// Failure to successfully update any AutoCVS project will
		// put us in disconnected mode.
		CVSOperations.OperationContext context = new CVSOperations.SimpleContext() {
			@Override
			public void failure(Shell parent) {
				setFailedOperation(true);
			}

			@Override
			public String getConsequenceMessage(int statusCode) {
				if (statusCode == CVSOperations.FILE_IN_THE_WAY)
					return getMessage("failed.update.fileintheway");
				else
					return getMessage("failed.update.generic");
			}
		};

		// Asynchronously execute the CVS operation
		CVSOperations.update(project, syncMode, context);
	}

	/**
	 * Set whether any automatic CVS operations have failed.
	 * 
	 * @param failedOperation
	 */
	public void setFailedOperation(boolean failedOperation) {
		this.failedOperation = failedOperation;
	}

	/**
	 * Automatically update all AutoCVS projects in the workspace.
	 */
	private void autoUpdateWorkspaceProjects() throws CoreException {
		// Attempt a CVS update on projects with the AutoCVS nature
		IProject[] projectList = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		Debug.print("Workspace contains " + projectList.length + " projects");

		// Check for projects with AutoCVS nature and try to update them,
		// but stop if any CVS operation fails.
		for (int i = 0; !hasFailedOperation() && i < projectList.length; ++i) {
			IProject project = projectList[i];
			if (project.isOpen() && hasAutoCVSNature(project))
				attemptCVSUpdate(project, CVSOperations.ASYNC);
		}
	}

	/**
	 * Return whether or not the given project has the AutoCVS nature.
	 * 
	 * @param project
	 * @return true if the project has the AutoCVS nature, false otherwise
	 */
	public static boolean hasAutoCVSNature(IProject project) {
		return hasProjectNature(project, AUTO_CVS_NATURE);
	}

	public static boolean hasAutoRunLogNature(IProject project) {
		return hasProjectNature(project, AUTO_RUN_LOG_NATURE);
	}

	public void earlyStartup() {
		// This method intentionally left unimplemented
	}
}

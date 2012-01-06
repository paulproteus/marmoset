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
 * Created on Aug 10, 2004
 */
package edu.umd.cs.eclipse.courseProjectManager;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;

/**
 * @author jspacco
 * 
 *         Simple wrapper class for CVS commands TODO options probably shouldn't
 *         be a protected variable, it should get passed as a final object where
 *         an anonymous subclass of MyCVSCommand is being created.
 */
@SuppressWarnings("restriction")
public abstract class MyCVSCommand {

	protected Session session;
	protected CVSTeamProvider provider;
	protected ICVSResource[] resources;
	protected LocalOption[] options;
	protected IProgressMonitor monitor;

	protected abstract IStatus execute() throws CVSException,
			InterruptedException;

	public IStatus executeCommand() throws CVSException, InterruptedException {
		return executeCommand(null);
	}

	public IStatus executeCommand(IProgressMonitor monitor)
			throws CVSException, InterruptedException {
		// TODO assert resources != null && all resources are in same project
		// TODO monitor might be null

		// CVSOperations.printResources(resources);

		ICVSRepositoryLocation repos = provider.getRemoteLocation();
		ICVSFolder root = provider.getCVSWorkspaceRoot().getLocalRoot();
		session = new Session(repos, root);
		this.monitor = monitor;

		session.open(monitor);

		try {
			IStatus status = execute();

			Debug.print("return status is: " + status.getCode()
					+ " with severity: " + status.getSeverity());
			if (status.getSeverity() == IStatus.ERROR) {
				throw new CVSException(status);
			}

			return status;
		} finally {
			session.close();
		}
	}

	public abstract String getTaskName();

	public abstract String getTaskName(CVSTeamProvider provider);

	/**
	 * 
	 */
	public MyCVSCommand(IResource[] r, LocalOption[] o) {
		// TODO assert r[] not empty and has useful stuff in it
		provider = CVSOperations.getProvider(r[0]);
		options = o;

		// CVSOperations.printResources(r);

		resources = new ICVSResource[r.length];
		for (int ii = 0; ii < r.length; ii++) {
			resources[ii] = CVSWorkspaceRoot.getCVSResourceFor(r[ii]);
		}
	}
}

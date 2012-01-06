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
 * Created on Jan 13, 2004
 */
package edu.umd.cs.eclipse.courseProjectManager;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * A property page dialog for configuring the AutoCVS nature for projects. This
 * code adapted from the example in Chapter 19 of <cite>Contributing to
 * Eclipse</cite> by Gamma and Beck.
 * 
 * @author David Hovemeyer
 */
public class AutoCVSPropertyPage extends PropertyPage {

	// Fields
	private Button autoCVSButton;
	private Button autoSyncButton;
	private Button autoRunLogButton;

	private boolean origAutoCVS;
	private boolean origAutoSync;
	private boolean origAutoRunLog;

	public AutoCVSPropertyPage() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		boolean hasAutoCVSNature = false;
		boolean hasAutoSyncNature = false;
		boolean hasAutoRunLogNature = false;
		try {
			hasAutoCVSNature = getProject().hasNature(
					AutoCVSPlugin.AUTO_CVS_NATURE);
			hasAutoSyncNature = getProject().hasNature(
					AutoCVSPlugin.AUTO_SYNC_NATURE);
			hasAutoRunLogNature = getProject().hasNature(
					AutoCVSPlugin.AUTO_RUN_LOG_NATURE);

			Debug.print("createContents: hasAutoCVSNature ==> "
					+ hasAutoCVSNature);
		} catch (CoreException e) {
			AutoCVSPlugin.getPlugin().getEventLog()
					.logError("Exception getting project nature", e);
		}
		Control control = addControl(parent, hasAutoCVSNature,
				hasAutoSyncNature, hasAutoRunLogNature);

		origAutoCVS = hasAutoCVSNature;
		origAutoSync = hasAutoSyncNature;
		origAutoRunLog = hasAutoRunLogNature;

		return control;
	}

	@Override
	public boolean performOk() {
		Debug.print("performOk() called");
		try {
			AutoCVSPlugin plugin = AutoCVSPlugin.getPlugin();
			boolean autoCVSEnabled = autoCVSButton.getSelection();
			boolean autoSyncEnabled = autoSyncButton.getSelection();
			boolean autoRunLogEnabled = autoRunLogButton.getSelection();

			Debug.print("\tAutoCVS ==> " + autoCVSEnabled);
			Debug.print("\tAutoSync ==> " + autoSyncEnabled);
			Debug.print("\tAutoRunLog ==> " + autoRunLogEnabled);

			// IProjectNatureDescriptor[] natureArr =
			// getProject().getWorkspace().getNatureDescriptors();
			// for (int ii=0; ii < natureArr.length; ii++) {
			// Debug.print("Nature: " +natureArr[ii].getLabel()+ ", "
			// +natureArr[ii]);
			// }

			// Only reconfigure the project if the settings have
			// actually changed.
			if (autoCVSEnabled != origAutoCVS
					|| autoSyncEnabled != origAutoSync
					|| autoRunLogEnabled != origAutoRunLog) {
				if (autoCVSEnabled) {
					if (autoSyncEnabled) {
						// AutoSync has been turned on -
						// run a cvs update to make sure the project
						// starts out in sync with the repository.
						AutoCVSPlugin.getPlugin().attemptCVSUpdate(
								getProject(), CVSOperations.SYNC);
					}

					plugin.setAutoRunLog(getProject(), autoRunLogEnabled);
					plugin.addAutoCVSNature(getProject());
					plugin.setAutoSync(getProject(), autoSyncEnabled);
				} else {
					plugin.setAutoRunLog(getProject(), false);
					plugin.setAutoSync(getProject(), false);
					plugin.removeAutoCVSNature(getProject());
				}
			}
		} catch (CoreException e) {
			// IStatus s = e.getStatus();
			// if (!s.isOK()) {
			// Debug.print("Code: " +s.getCode());
			// Debug.print("Severity: " +s.getSeverity());
			// Debug.print("Message: " +s.getMessage());
			// IStatus[] statusArr = s.getChildren();
			// if (statusArr != null) {
			// Debug.print("MultiStatus messages: ");
			// for (int ii=0; ii<statusArr.length; ii++) {
			// Debug.print("\t" +statusArr[ii].getMessage());
			// }
			//
			// }
			// Throwable cause = s.getException();
			// if (cause != null)
			// Debug.print("Cause: ", cause);
			// }
			Debug.print("Core exception in performOK()", e);
			AutoCVSPlugin.getPlugin().getEventLog()
					.logError("Exception getting project nature", e);
		}
		return true;
	}

	/**
	 * Get the project we're configuring properties of.
	 * 
	 * @return the project
	 */
	private IProject getProject() {
		return (IProject) getElement();
	}

	/**
	 * Create the control used in the property page.
	 * 
	 * @param parent
	 *            the parent control
	 * @return the property page control
	 */
	private Control addControl(Composite parent, boolean hasAutoCVSNature,
			boolean hasAutoSyncNature, boolean hasAutoRunLogNature) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		composite.setLayout(gridLayout);

		// This sets the GridData for the composite itself
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		Font font = parent.getFont();
		Label label = new Label(composite, SWT.NONE);
		Label label2 = new Label(composite, SWT.NONE);
		label.setText("CourseProjectManager allows submission of projects and (optionally)");
		label2.setText("keeps the project in sync with the repository automatically.");
		Label blank = new Label(composite, SWT.NONE);
		blank.setText("");

		if (AutoCVSPlugin.getPlugin().hasFailedOperation()) {
			// TODO: this code is ugly. Maybe we should use the JFace
			// components to lay out this dialog.

			Label label3 = new Label(composite, SWT.NONE);
			Label label4 = new Label(composite, SWT.NONE);
			Label label5 = new Label(composite, SWT.NONE);
			Label space2 = new Label(composite, SWT.NONE);

			label3.setText("ATTENTION: Automatic synchronization with the CVS is temporarily disabled, because");
			label4.setText(" a CVS operation failed.  You can/should manually synchronize your work with the repository");
			label5.setText(" using options on the Team menu. Restarting Eclipse will reenable automatic synchronization");

			space2.setText("");
		}

		autoCVSButton = new Button(composite, SWT.CHECK);
		autoCVSButton.setText("Enable Course Project Management");
		autoCVSButton.setFont(font);
		autoCVSButton.setSelection(hasAutoCVSNature);

		// The selection listener ensures:
		// - If AutoCVS is checked, we also enable and check AutoSync,
		// because AutoSync defaults to on.
		// - If AutoCVS is unchecked, AutoSync is disabled and unchecked.
		autoCVSButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (autoCVSButton.getSelection()) {
					autoSyncButton.setSelection(true);
					autoSyncButton.setEnabled(true);
					autoRunLogButton.setSelection(true);
					autoRunLogButton.setEnabled(true);
				} else {
					autoSyncButton.setSelection(false);
					autoSyncButton.setEnabled(false);
					autoRunLogButton.setSelection(false);
					autoRunLogButton.setEnabled(false);
				}
			}
		});

		autoSyncButton = new Button(composite, SWT.CHECK);
		autoSyncButton
				.setText("Automatically keep project in sync with the repository via CVS");
		autoSyncButton.setFont(font);

		// This button should only be active when the project has autoCVSNature.
		// Otherwise it should be grayed out.
		if (hasAutoCVSNature) {
			autoSyncButton.setSelection(hasAutoSyncNature);
		} else {
			autoSyncButton.setSelection(false);
			autoSyncButton.setEnabled(false);
		}

		// Indent the AutoSync button to show that it is
		// dependent on the status of the AutoCVS button.
		GridData syncButtonGridData = new GridData();
		syncButtonGridData.horizontalIndent = 15;
		// Tell the syncButton's grid data that it should grow larger
		// horizontally, if necessary
		syncButtonGridData.grabExcessHorizontalSpace = true;
		autoSyncButton.setLayoutData(syncButtonGridData);

		// Button for setting/unsetting logging of Eclipse Launch Events
		autoRunLogButton = new Button(composite, SWT.CHECK);
		autoRunLogButton
				.setText("Log which versions are executed/tested (for Marmoset research project)");
		autoRunLogButton.setFont(font);

		if (hasAutoCVSNature) {
			autoRunLogButton.setSelection(hasAutoRunLogNature);
		} else {
			autoRunLogButton.setSelection(false);
			autoRunLogButton.setEnabled(false);
		}

		autoRunLogButton.setLayoutData(syncButtonGridData);
		return composite;
	}

}

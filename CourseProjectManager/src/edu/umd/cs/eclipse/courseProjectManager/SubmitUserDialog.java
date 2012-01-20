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
 * @author jspacco
 */
package edu.umd.cs.eclipse.courseProjectManager;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class SubmitUserDialog extends Dialog {
	private String username;
	private String password;

	private Text usernameText;
	private Text passwordText;
	private Text errorMessageText;

	private Button okButton;

	private String message = "Enter submit user data from submit server web page";
	private String title = "User data from Submit server";

	public SubmitUserDialog(Shell parent) {
		super(parent);
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		// create composite
		Composite composite = (Composite) super.createDialogArea(parent);
		// create message
		if (message != null) {
			Label label = new Label(composite, SWT.WRAP);
			label.setText(message);
			GridData data = new GridData(GridData.GRAB_HORIZONTAL
					| GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_CENTER);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			label.setLayoutData(data);
			label.setFont(parent.getFont());
		}

		// username prompt
		Label usernameLabel = new Label(composite, SWT.WRAP);
		usernameLabel.setText("Info:");
		GridData data = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_BEGINNING);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		usernameLabel.setLayoutData(data);
		usernameLabel.setFont(parent.getFont());

		// username textbox
		usernameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		usernameText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));

	
		// Validate the user's input
		usernameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		errorMessageText = new Text(composite, SWT.READ_ONLY);
		errorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		errorMessageText.setBackground(errorMessageText.getDisplay()
				.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		applyDialogFont(composite);
		return composite;
	}

	/**
	 * Validates the input.
	 * <p>
	 * The default implementation of this framework method delegates the request
	 * to the supplied input validator object; if it finds the input invalid,
	 * the error message is displayed in the dialog's message line. This hook
	 * method is called whenever the text changes in the input field.
	 * </p>
	 */
	String classAccount;
	String oneTimePassword;
	protected void validateInput() {
	    String info = usernameText.getText();
        if (info.length() > 2) {
            int checksum = Integer.parseInt(info.substring(info.length() - 1), 16);
            info = info.substring(0, info.length() - 1);
            int hash = info.hashCode() & 0x0f;
            if (checksum == hash) {
                String fields[] = info.split(";");
                if (fields.length == 2) {
                    classAccount = fields[0];
                    oneTimePassword = fields[1];
                    setErrorMessage(null);
                    return;

                }
            }
        }
        String errorMessage = "The information should be your account name and a string of hexidecimal digits, separated by a semicolon";
        
		setErrorMessage(errorMessage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets
	 * .Shell)
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null)
			shell.setText(title);
	}

	/**
	 * Sets or clears the error message. If not <code>null</code>, the OK button
	 * is disabled.
	 * 
	 * @param errorMessage
	 *            the error message, or <code>null</code> to clear
	 * @since 3.0
	 */
	protected void setErrorMessage(String errorMessage) {
		errorMessageText.setText(errorMessage == null ? "" : errorMessage); //$NON-NLS-1$
		okButton.setEnabled(errorMessage == null);
		errorMessageText.getParent().update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		if (password != null) {
			passwordText.setText(password);
			passwordText.selectAll();
		}

		usernameText.setFocus();
		if (username != null) {
			usernameText.setText(username);
			usernameText.selectAll();
		}
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			username = usernameText.getText();
			password = passwordText.getText();
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * @return Returns the password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return Returns the username.
	 */
	public String getUsername() {
		return username;
	}
}

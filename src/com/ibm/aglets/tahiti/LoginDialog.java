package com.ibm.aglets.tahiti;

/*
 * @(#)LoginDialog.java
 * 
 * IBM Confidential-Restricted
 * 
 * OCO Source Materials
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.security.cert.Certificate;
import com.ibm.aglet.system.AgletRuntime;

public class LoginDialog extends TahitiWindow implements ActionListener {

	String _username = null;
	Certificate _certificate = null;

	TextField _account = new TextField(12);
	TextField _password = new TextField(12);

	private Button _LoginButton = null;

	private boolean auth = false;
	Object lock = new Object();

	public LoginDialog() {
		super("Login");

		_username = UserManager.getDefaultUsername();
		_account.setText(_username);
		_password.setEchoChar('*');
		_password.setText("");

		GridBagPanel p = new GridBagPanel();
		GridBagConstraints cns = new GridBagConstraints();

		cns.fill = GridBagPanel.HORIZONTAL;
		p.setConstraints(cns);

		p.addLabeled("Name:", _account);
		p.addLabeled("Password:", _password);
		add("North", new Label("Aglets Login", Label.CENTER));
		add("Center", p);
		addLoginButton("Login");
		addCloseButton("Cancel");
		_password.addActionListener(this);
	}
	public void actionPerformed(ActionEvent ev) {
		checkPassword();
	}
	protected void addLoginButton(String name) {
		class LoginListener extends ActionAndKeyListener {
			LoginDialog loginDialog = null;

			LoginListener(LoginDialog dialog) {
				loginDialog = dialog;
			}
			protected void doAction() {
				loginDialog.checkPassword();
			} 
		}
		if (name == null) {
			name = "Login";
		} 
		ActionAndKeyListener listener = new LoginListener(this);

		_LoginButton = addButton(name, listener, listener);
	}
	public void checkPassword() {
		_username = _account.getText();
		String password = _password.getText();
		AgletRuntime runtime = AgletRuntime.getAgletRuntime();

		if (runtime == null) {
			return;
		} 
		_certificate = runtime.authenticateOwner(_username, password);
		if (_certificate != null) {
			auth = true;
			dispose();
			synchronized (lock) {
				lock.notifyAll();
			} 
		} else {
			TahitiDialog dialog = TahitiDialog.alert(this, "Login Failed");

			dialog.popupAtCenterOfParent();
			_password.setText("");
		} 
	}
	public void closeButtonPressed() {
		System.exit(1);
	}
	public Certificate getCertificate() {
		return _certificate;
	}
	public String getUsername() {
		return _username;
	}
	public boolean isAuthenticated() {
		return auth;
	}
	private void requestFocusOnInputText() {
		if (_username == null || _username.equals("")) {
			_account.requestFocus();
		} else {
			_password.requestFocus();
		} 
	}
	public void waitForAuthentication() {
		synchronized (lock) {
			requestFocusOnInputText();		// It looks no effect.
			while (auth == false) {
				try {
					lock.wait();
				} catch (InterruptedException ex) {
					break;
				} 
			} 
		} 
	}
	public boolean windowClosing(WindowEvent ev) {
		System.exit(1);
		return false;
	}
}

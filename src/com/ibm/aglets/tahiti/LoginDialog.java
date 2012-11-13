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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.security.cert.Certificate;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.ImagePanel;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

import com.ibm.aglet.system.AgletRuntime;

public class LoginDialog extends TahitiWindow {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3551677656038813086L;

	// only for testing
	public static void main(final String argv[]) {
		new LoginDialog();
		new MainWindow(null);

	}

	/**
	 * The username of the user that is going to login.
	 */
	protected String username = null;

	/**
	 * The certifcate of the user.
	 */
	protected Certificate certificate = null;
	/**
	 * The texfields for the username and the password.
	 */
	protected JTextField usernameField = null;

	protected JPasswordField passwordField = null;
	private boolean auth = false;

	Object lock = new Object();

	/**
	 * Builds up the login dialog with the main components, that are a text
	 * field for the username and one for the password.
	 * 
	 */
	public LoginDialog() {
		super();
		setTitle("Tahiti Log-in");
		shouldExitOnClosing = true; // if this window is closed without the
		// ok button
		// exit from the application

		// get the default username
		username = UserManager.getDefaultUsername();

		// create and initialize the text fields
		usernameField = JComponentBuilder.createJTextField(12, username, baseKey
				+ ".usernameField");
		passwordField = JComponentBuilder.createJPasswordField(12);
		usernameField.setText(username);

		// create a panel for containing the username and password field
		final JPanel northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(2, 2));
		JLabel label = JComponentBuilder.createJLabel(baseKey
				+ ".usernameLabel");
		northPanel.add(label);
		usernameField = JComponentBuilder.createJTextField(15, username, baseKey
				+ ".usernameField");
		northPanel.add(usernameField);
		label = JComponentBuilder.createJLabel(baseKey + ".passwordLabel");
		northPanel.add(label);
		passwordField = JComponentBuilder.createJPasswordField(15);
		northPanel.add(passwordField);

		// create the image panel with the logo
		final ImagePanel imgPanel = JComponentBuilder.createLogoPanel();

		this.add(northPanel, BorderLayout.NORTH);
		if (imgPanel != null) {
			// create a pseudo-panel to center the image
			final JPanel cPanel = new JPanel();
			cPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			cPanel.add(imgPanel);
			this.add(cPanel, BorderLayout.CENTER);
		}

		pack();
		setVisible(true);

	}

	/**
	 * Manages events from the button
	 */
	@Override
	public void actionPerformed(final ActionEvent ev) {
		if (ev == null)
			return;

		final String command = ev.getActionCommand();

		if (GUICommandStrings.OK_COMMAND.equals(command)) {
			// ok button pressed, check for the authentication
			if (!checkAuthentication())
				// show a gui with problem for authentication here
				JComponentBuilder.showErrorDialog(this, baseKey
						+ ".errorDialog");
		} else
			super.actionPerformed(ev);
	}

	/**
	 * Checks if the username and password are correct and the user can
	 * authenticate itself into the system.
	 * 
	 * @return true if the user has been authenticated.
	 */
	public boolean checkAuthentication() {
		// get the username and the password
		username = usernameField.getText();
		final String password = new String(passwordField.getPassword());

		// now get the aglet runtime and try to authenticate
		final AgletRuntime runtime = AgletRuntime.getAgletRuntime();
		// check if the aglet runtime works well
		if (runtime == null) {
			logger.error("Cannot get the aglet runtime object!");
			return false;
		}

		// get the certificates
		certificate = runtime.authenticateOwner(username, password);
		if (certificate != null) {
			// the user has been authenticated
			logger.info("Authenticated user " + username);
			auth = true;

			// store the data about this login
			LoginData.setCertificate(certificate);
			LoginData.setUsername(username);

			dispose();
			synchronized (lock) {
				lock.notify();
			}
			return true;
		} else {
			logger.error("Cannot authenticate the user " + username);
			auth = false;
			passwordField.setText("");
			return false;
		}

	}

	/**
	 * Gets back the certificate.
	 * 
	 * @return the certificate
	 */
	public synchronized final Certificate getCertificate() {
		return certificate;
	}

	/**
	 * Gets back the username.
	 * 
	 * @return the username
	 */
	public synchronized final String getUsername() {
		return username;
	}

	/**
	 * If the username field is empty than move the focus on it, otherwise move
	 * the focus on the password field.
	 */
	private void requestFocusOnInputText() {
		if ((username == null) || username.equals(""))
			usernameField.requestFocus();
		else
			passwordField.requestFocus();

	}

	/**
	 * Sets the username value.
	 * 
	 * @param username
	 *            the username to set
	 */
	public synchronized final void setUsername(final String username) {
		this.username = username;
	}

	/**
	 * Waits for the user to make the authentication.
	 * 
	 */
	public void waitForAuthentication() {
		synchronized (lock) {
			requestFocusOnInputText(); // It looks no effect.
			while (auth == false) {
				try {
					lock.wait();
				} catch (final InterruptedException ex) {
					break;
				}
			}
		}
	}

}

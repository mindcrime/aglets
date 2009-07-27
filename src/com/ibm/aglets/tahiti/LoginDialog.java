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
import java.io.FileInputStream;
import java.security.cert.Certificate;
import java.util.Properties;

import com.ibm.aglet.system.AgletRuntime;
import com.ibm.aglets.tahiti.utils.IconRepository;

import javax.swing.*;
import com.ibm.aglets.tahiti.utils.*;

/**
 * Converted to swing by Luca Ferrari.
 *
 */
public class LoginDialog extends TahitiWindow implements ActionListener {

	String _username = null;
	Certificate _certificate = null;

	JTextField _account = new JTextField(12);
	JPasswordField _password = new JPasswordField(12);

	private JButton _LoginButton = null;

	private boolean auth = false;
	Object lock = new Object();

	public LoginDialog() {
		super(bundle.getString("dialog.login.title"));
		
		// initialize the icon repository
		IconRepository.loadIconFromPropertyFile(System.getProperty("aglets.icons"));
		// set the icon of this window
		this.setIconImage(IconRepository.getImage("login"));

				
		_username = UserManager.getDefaultUsername();
		_account.setText(_username);
		_password.setEchoChar('*');
		_password.setText("");

		GridBagPanel p = new GridBagPanel();
		GridBagConstraints cns = new GridBagConstraints();

		cns.fill = GridBagPanel.HORIZONTAL;
		p.setConstraints(cns);

		p.addLabeled("Username:", _account);
		p.addLabeled("Password:", _password);
		ImagePanel image =new ImagePanel(System.getProperty("aglets.home")+"/icons/logo_aglets.jpg");
		this.getContentPane().add("Center", image);
		this.getContentPane().add("North", p);
		
		
		// add buttons
		this.addJButton(bundle.getString("dialog.login.button.ok"),TahitiCommandStrings.OK_COMMAND,IconRepository.getIcon("ok"),this,bundle.getString("dialog.login.button.tooltip.ok"));
		this.addJButton(bundle.getString("dialog.login.button.cancel"),TahitiCommandStrings.CANCEL_COMMAND,IconRepository.getIcon("cancel"),this,bundle.getString("dialog.login.button.tooltip.cancel"));

		// add the listener for the password field
		this._password.setActionCommand(TahitiCommandStrings.OK_COMMAND);
		this._password.addActionListener(this);
		
		this.setSize(image.getWidth(),image.getHeight()+100);
		
				
	}
	
	/**
	 * Manage events from buttons.
	 * @param event the event to manage
	 */
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		
		if(command.equals(TahitiCommandStrings.OK_COMMAND)){
		    this.checkPassword();
		}
		else
		if(command.equals(TahitiCommandStrings.CANCEL_COMMAND)){
		    System.exit(1);
		}
	}
		
	
	
	/**
	 * Check the inserted password.
	 *
	 */
	public void checkPassword() {
		_username = _account.getText();
		String password = new String(_password.getPassword());
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
			JOptionPane.showMessageDialog(this,bundle.getString("dialog.login.error.loginfailed"),bundle.getString("dialog.login.error.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("error"));
			_password.setText("");
		} 
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
	
	

}

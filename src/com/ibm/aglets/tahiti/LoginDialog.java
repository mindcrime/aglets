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

import javax.swing.*;

import org.aglets.util.gui.GUICommandStrings;
import org.aglets.util.gui.ImagePanel;
import org.aglets.util.gui.JComponentBuilder;

import com.ibm.aglet.system.AgletRuntime;
import javax.swing.border.*;

public class LoginDialog extends TahitiWindow {

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
	protected JTextField     usernameField = null;
	protected JPasswordField passwordField = null;
	

	
	private boolean auth = false;
	Object lock = new Object();

	
	
	/**
	 * Builds up the login dialog with the main components, that are a text field
	 * for the username and one for the password.
	 *
	 */
	public LoginDialog() {
		super();
		this.setTitle("Tahiti Log-in");
		this.shouldExitOnClosing = true;	// if this window is closed without the ok button
							// exit from the application
		
		
		// get the default username
		username = UserManager.getDefaultUsername();
		
		// create and initialize the text fields
		this.usernameField = JComponentBuilder.createJTextField(12,this.username, this.baseKey + ".usernameField");
		this.passwordField = JComponentBuilder.createJPasswordField(12);
		this.usernameField.setText(this.username);

		// create a panel for containing the username and password field
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(2,2));
		JLabel label = JComponentBuilder.createJLabel(this.baseKey + ".usernameLabel");
		northPanel.add(label);
		this.usernameField = JComponentBuilder.createJTextField(15, this.username, this.baseKey + ".usernameField");
		northPanel.add(this.usernameField);
		label = JComponentBuilder.createJLabel(this.baseKey + ".passwordLabel");
		northPanel.add(label);
		this.passwordField = JComponentBuilder.createJPasswordField(15);
		northPanel.add(this.passwordField);
		
		// create the image panel with the logo
		ImagePanel imgPanel = JComponentBuilder.createLogoPanel();
		
		
		this.add(northPanel, BorderLayout.NORTH);
		if( imgPanel != null  ){
		    // create a pseudo-panel to center the image
		    JPanel cPanel = new JPanel();
		    cPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		    cPanel.add(imgPanel);
		    this.add(cPanel, BorderLayout.CENTER);
		}
		
	
		this.pack();
		this.setVisible(true);

	}
	
	

	
	/**
	 * Manages events from the button
	 */
	public void actionPerformed(ActionEvent ev) {
	    if( ev == null )
		return;
	    
		String command = ev.getActionCommand();
		
		
		if( GUICommandStrings.OK_COMMAND.equals(command)){
		    // ok button pressed, check for the authentication
		    if( ! this.checkAuthentication() )
			// show a gui with problem for authentication here
			JComponentBuilder.showErrorDialog(this, this.baseKey + ".errorDialog");
		}
		else
		    super.actionPerformed(ev);
	}
	
	/**
	 * Checks if the username and password are correct and the user can authenticate
	 * itself into the system.
	 * @return true if the user has been authenticated.
	 */
	public  boolean checkAuthentication(){
	    // get the username and the password
	    this.username = this.usernameField.getText();
	    String password = new String(this.passwordField.getPassword());
	    
	    // now get the aglet runtime and try to authenticate
	    AgletRuntime runtime = AgletRuntime.getAgletRuntime();
	    // check if the aglet runtime works well
	    if( runtime == null ){
		logger.error("Cannot get the aglet runtime object!");
		return false;
	    }
	    
	    // get the certificates
	    this.certificate = runtime.authenticateOwner(this.username, password);
	    if( this.certificate != null ){
		// the user has been authenticated
		logger.info("Authenticated user " + this.username);
		this.auth = true;
		this.dispose();
		synchronized( this.lock ){
		    this.lock.notify();
		}
		return true;
	    }
	    else{
		logger.error("Cannot authenticate the user " + this.username);
		this.auth = false;
		this.passwordField.setText("");
		return false;
	    }
		
	}
	
	/**
	 * If the username field is empty than move the focus on it, otherwise
	 * move the focus on the password field.
	 */
	private void requestFocusOnInputText() {
	    if (this.username == null || this.username.equals("")) 
		this.usernameField.requestFocus();
	    else
		this.passwordField.requestFocus();
		
	}
	
	
	/**
	 * Waits for the user to make the authentication.
	 *
	 */
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
	

	
	 /**
	 * Gets back the username.
	 * @return the username
	 */
	public synchronized final String getUsername() {
	    return username;
	}




	/**
	 * Sets the username value.
	 * @param username the username to set
	 */
	public synchronized final void setUsername(String username) {
	    this.username = username;
	}




	/**
	 * Gets back the certificate.
	 * @return the certificate
	 */
	public synchronized final Certificate getCertificate() {
	    return certificate;
	}




	// only for testing
	public static void main(String argv[]){
	    LoginDialog dialog = new LoginDialog();
	    //JFrame f = new JFrame();
	    //f.setSize(300,300);
	    //f.setJMenuBar(new TahitiMenuBar(null));
	    //f.add(new TahitiToolBar(null));
	    //f.setVisible(true);
	    //dialog.setVisible(true);
	    //TahitiDialog dialog2 = new TahitiDialog(dialog);
	    //dialog2.setVisible(true);
	    //CloneAgletDialog dialog2 = new CloneAgletDialog(new MainWindow(null), null);
	    //dialog2.setVisible(true);
	    //dialog2.setModal(true);
	    MainWindow window = new MainWindow(null);
	    
	}
	
}

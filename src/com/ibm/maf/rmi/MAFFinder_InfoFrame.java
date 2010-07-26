package com.ibm.maf.rmi;

/*
 * @(#)MAFFinder_InfoFrame.java
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
import java.awt.Button;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class MAFFinder_InfoFrame extends Frame implements ActionListener,
	WindowListener {
    private Button _exit_button = new Button("Exit");

    public MAFFinder_InfoFrame(String name, int port) {
	String host = "localhost";

	try {
	    java.net.InetAddress ia = java.net.InetAddress.getLocalHost();

	    host = ia.getHostName();
	} catch (java.net.UnknownHostException ex) {
	    host = "localhost";
	}

	String url = "rmi://" + host + ":" + port + "/" + name;

	this.setTitle("MAFFinder: " + url);
	this.addWindowListener(this);
	this._exit_button.addActionListener(this);

	GridBagLayout gl = new GridBagLayout();
	GridBagConstraints cst = new GridBagConstraints();

	this.setLayout(gl);

	Label lbl = new Label(url);

	cst.fill = GridBagConstraints.REMAINDER;
	cst.anchor = GridBagConstraints.CENTER;
	cst.insets = new Insets(4, 4, 4, 4);
	gl.setConstraints(lbl, cst);
	this.add(lbl);

	cst.fill = GridBagConstraints.NONE;
	cst.gridy = 1;
	gl.setConstraints(this._exit_button, cst);
	this.add(this._exit_button);
    }

    public void actionPerformed(ActionEvent e) {
	if ("Exit".equals(e.getActionCommand())) {
	    System.exit(0);
	}
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
	System.exit(0);
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }
}

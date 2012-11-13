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
	/**
	 * 
	 */
	private static final long serialVersionUID = -6782945355902858396L;
	private final Button _exit_button = new Button("Exit");

	public MAFFinder_InfoFrame(final String name, final int port) {
		String host = "localhost";

		try {
			final java.net.InetAddress ia = java.net.InetAddress.getLocalHost();

			host = ia.getHostName();
		} catch (final java.net.UnknownHostException ex) {
			host = "localhost";
		}

		final String url = "rmi://" + host + ":" + port + "/" + name;

		setTitle("MAFFinder: " + url);
		addWindowListener(this);
		_exit_button.addActionListener(this);

		final GridBagLayout gl = new GridBagLayout();
		final GridBagConstraints cst = new GridBagConstraints();

		setLayout(gl);

		final Label lbl = new Label(url);

		cst.fill = GridBagConstraints.REMAINDER;
		cst.anchor = GridBagConstraints.CENTER;
		cst.insets = new Insets(4, 4, 4, 4);
		gl.setConstraints(lbl, cst);
		this.add(lbl);

		cst.fill = GridBagConstraints.NONE;
		cst.gridy = 1;
		gl.setConstraints(_exit_button, cst);
		this.add(_exit_button);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if ("Exit".equals(e.getActionCommand())) {
			System.exit(0);
		}
	}

	@Override
	public void windowActivated(final WindowEvent e) {
	}

	@Override
	public void windowClosed(final WindowEvent e) {
	}

	@Override
	public void windowClosing(final WindowEvent e) {
		System.exit(0);
	}

	@Override
	public void windowDeactivated(final WindowEvent e) {
	}

	@Override
	public void windowDeiconified(final WindowEvent e) {
	}

	@Override
	public void windowIconified(final WindowEvent e) {
	}

	@Override
	public void windowOpened(final WindowEvent e) {
	}
}

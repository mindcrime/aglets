package com.ibm.aglets.tahiti;

/*
 * @(#)ShutdownDialog.java
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

import java.awt.Label;
import java.awt.event.WindowEvent;

import javax.swing.JButton;

/**
 * ShutdownDialog
 * 
 * @version 1.05 96/10/01
 * @author Mitsuru Oshima
 */

final class ShutdownDialog extends TahitiDialog {

    /*
     * Singleton instance reference.
     */
    private static ShutdownDialog _instance = null;

    /**
     * Shutdown
     */
    private JButton _OKButton = null;
    /**
     * Reboot
     */
    private JButton _RebootButton = null;

    /*
     * Constructs
     */
    private ShutdownDialog(MainWindow parent) {
	this(parent, "OK?");
    }

    /*
	 * 
	 */
    /* package protected */
    ShutdownDialog(MainWindow parent, String msg) {
	super(parent);

	this.add("North", new Label("Shutdown Server", Label.CENTER));
	this.add("Center", new Label(msg, Label.CENTER));

	this.addOKButton("OK");
	this.addRebootButton("Reboot");
	this.addCloseButton("Cancel");
    }

    protected void addOKButton(String name) {
	if (name == null) {
	    name = "OK";
	}
	class ShutdownListener extends ActionAndKeyListener {
	    ShutdownDialog shutdownDialog = null;

	    ShutdownListener(ShutdownDialog dialog) {
		this.shutdownDialog = dialog;
	    }

	    @Override
	    protected void doAction() {
		this.shutdownDialog.dispose();
		this.shutdownDialog.getMainWindow().shutdown();
	    }
	}
	ActionAndKeyListener listener = new ShutdownListener(this);

	this._OKButton = this.addButton(name, listener, listener);
    }

    protected void addRebootButton(String name) {
	if (name == null) {
	    name = "Reboot";
	}
	class RebootListener extends ActionAndKeyListener {
	    ShutdownDialog shutdownDialog = null;

	    RebootListener(ShutdownDialog dialog) {
		this.shutdownDialog = dialog;
	    }

	    @Override
	    protected void doAction() {
		this.shutdownDialog.dispose();
		this.shutdownDialog.getMainWindow().reboot();
	    }
	}
	ActionAndKeyListener listener = new RebootListener(this);

	this._RebootButton = this.addButton(name, listener, listener);
    }

    /*
     * Singletion method to get the instnace
     */
    static ShutdownDialog getInstance(MainWindow parent) {
	if (_instance == null) {
	    _instance = new ShutdownDialog(parent);
	}
	return _instance;
    }

    public boolean windowClosing(WindowEvent ev) {
	return false;
    }
}

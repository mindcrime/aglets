package com.ibm.aglets.tahiti;

/*
 * @(#)MemoryUsageDialog.java
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

/**
 * MemoryUsageDialog is a dialog window that shows the memory usage
 * 
 * @version 1.01 96/03/28
 * @author Mitsuru Oshima
 */

final class MemoryUsageDialog extends TahitiDialog implements Runnable {

    private java.awt.Canvas _myCanvas = new MemCanvas();
    private Thread _handler = null;

    /*
     * Singleton instance reference.
     */
    private static MemoryUsageDialog _instance = null;

    /*
     * Constructs a new Aglet creation dialog.
     */
    private MemoryUsageDialog(MainWindow parent) {
	super(parent, "Memory Usage", false);
	this.add("North", new Label(""));
	this.add("Center", this._myCanvas);

	this.addCloseButton(null);

	this._handler = new Thread(this);
	this._handler.start();
    }

    protected void closeButtonPressed() {
	this._handler.suspend();
    }

    /*
     * Singletion method to get the instnace
     */
    static MemoryUsageDialog getInstance(MainWindow parent) {
	if (_instance == null) {
	    _instance = new MemoryUsageDialog(parent);
	} else {
	    _instance.repaint();
	}
	_instance.start();
	return _instance;
    }

    /*
     * Layouts all components. protected void makePanel(GridBagLayout grid) {
     * GridBagConstraints cns = new GridBagConstraints();
     * 
     * cns.fill = GridBagConstraints.HORIZONTAL; cns.weightx = 1.0; //
     * cns.weighty = 0.1; cns.ipadx = cns.ipady = 5; cns.insets = new
     * Insets(5,5,5,5); cns.gridwidth = GridBagConstraints.REMAINDER;
     * 
     * addCmp(_myCanvas, grid, cns); }
     */

    /*
     * Save options
     */
    @Override
    public void run() {
	while (true) {
	    this._myCanvas.repaint();
	    try {
		Thread.currentThread();
		Thread.sleep(1000);
	    } catch (Exception ex) {
		break;
	    }
	}
	this._handler = null;
    }

    public void start() {
	this._handler.resume();
    }

    protected boolean windowClosing(WindowEvent ev) {
	this._handler.suspend();
	return false;
    }
}

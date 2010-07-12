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

import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.AgletException;
import com.ibm.aglets.*;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;

/**
 * MemoryUsageDialog is a dialog window that shows the memory usage
 * 
 * @version     1.01    96/03/28
 * @author	Mitsuru Oshima
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
		add("North", new Label(""));
		add("Center", _myCanvas);

		addCloseButton(null);

		_handler = new Thread(this);
		_handler.start();
	}
	protected void closeButtonPressed() {
		_handler.suspend();
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
	 * Layouts all components.
	 * protected void makePanel(GridBagLayout grid) {
	 * GridBagConstraints cns = new GridBagConstraints();
	 * 
	 * cns.fill = GridBagConstraints.HORIZONTAL;
	 * cns.weightx = 1.0;
	 * //      cns.weighty = 0.1;
	 * cns.ipadx = cns.ipady = 5;
	 * cns.insets = new Insets(5,5,5,5);
	 * cns.gridwidth = GridBagConstraints.REMAINDER;
	 * 
	 * addCmp(_myCanvas, grid, cns);
	 * }
	 */

	/*
	 * Save options
	 */
	public void run() {
		while (true) {
			_myCanvas.repaint();
			try {
				Thread.currentThread().sleep(1000);
			} catch (Exception ex) {
				break;
			} 
		} 
		_handler = null;
	}
	public void start() {
		_handler.resume();
	}
	protected boolean windowClosing(WindowEvent ev) {
		_handler.suspend();
		return false;
	}
}

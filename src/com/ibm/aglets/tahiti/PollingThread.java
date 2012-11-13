package com.ibm.aglets.tahiti;

/*
 * @(#)PollingThread.java
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

import com.ibm.aglets.AgletRuntime;

/**
 * A <tt>PollingThread</tt> thread polls an aglet box for incoming aglets
 * 
 * @version 1.03 97/02/23
 * @author Yariv Aridor
 */

class PollingThread extends Thread {

	// -- frequency of polling
	private int _sec = 0; // never
	private MainWindow _window = null;

	PollingThread(final int id, final MainWindow window) {
		setPriority(Thread.MIN_PRIORITY);
		setFrequency(id);
		_window = window;
	}

	private void poll() {
		AgletRuntime.verboseOut("polling!!");
		_window.getAglets();
	}

	@Override
	synchronized public void run() {
		while (true) {
			try {
				if (_sec <= 0) {
					this.wait();
				} else {
					poll();
					this.wait(_sec * 1000L); // wait...
				}
			} catch (final InterruptedException ie) {
			} // Catch any interrupts.
		}
	}

	synchronized public void setFrequency(int id) {
		if ((id < 0) || (id > 6)) {
			id = 0;

			// new IllegalArgumentException("illegal setting :" +
			// id).printStackTrace();
		}
		switch (id) {
			case 0:
				_sec = 0;
				break;
			case 1:
				_sec = 15;
				break;
			case 2:
				_sec = 30;
				break;
			case 3:
				_sec = 60;
				break;
			case 4:
				_sec = 60 * 5;
				break;
			case 5:
				_sec = 60 * 15;
				break;
			case 6:
				_sec = 60 * 60;
				break;
		}
		notify();
	}
}

package com.ibm.aglets;

/*
 * @(#)AgletThread.java
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

import com.ibm.aglet.Aglet;
import com.ibm.aglet.MessageManager;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.InvalidAgletException;

// import com.ibm.awb.misc.Debug;

public		// TEMPORARY (because of Fiji)
final class AgletThread extends Thread {
	private boolean valid = true;
	private boolean start = false;
	private boolean loop_started = false;

	private MessageManagerImpl manager = null;
	private MessageImpl message = null;

	static int count = 1;

	public AgletThread(ThreadGroup group, MessageManager m) {
		super(group, "No." + (count++) + ']');
		manager = (MessageManagerImpl)m;
		setPriority(group.getMaxPriority());
	}
	static MessageImpl getCurrentMessage() {
		Thread t = Thread.currentThread();

		if (t instanceof AgletThread) {
			return ((AgletThread)t).message;
		} 
		return null;
	}
	void handleMessage(MessageImpl msg) {
		if (isAlive()) {

			// this is called after this is pushed into the thread stack.

			// Debug.check();

			// synchronized block is needed only when the thread
			// is already running.
			synchronized (this) {

				// following two can be outside of sync. block.
				message = msg;
				start = true;

				notifyAll();
			} 

			// Debug.check();
		} else {

			// Debug.check();
			message = msg;
			start = true;
			start();

			// Debug.check();
		} 
	}
	synchronized public void invalidate() {

		// Debug.check();
		if (valid) {
			valid = false;
			start = true;
			notifyAll();
		} 

		// Debug.check();
	}
	public void run() {
		if (loop_started) {

			// to assure that aglet cannot call run on this thread.
			return;
		} 

		loop_started = true;
		start = false;
		LocalAgletRef ref = manager.getAgletRef();

		// Debug.start();

		try {
			while (valid) {
				try {
					message.handle(ref);
					message = null;
				} catch (RuntimeException ex) {
					valid = false;
					throw ex;
				} catch (Error ex) {
					valid = false;
					throw ex;
				} catch (InvalidAgletException ex) {
					ex.printStackTrace();
					valid = false;
					start = true;
				} 
				finally {

					// Debug.check();
					if (valid) {
						manager.pushThreadAndExitMonitorIfOwner(this);
					} else {
						manager.exitMonitorIfOwner();
					} 

					// Debug.check();
				} 

				synchronized (this) {

					// Debug.check();
					while (start == false && valid) {
						try {
							wait();
						} catch (InterruptedException ex) {
							ex.printStackTrace();
						} 
					} 
					start = false;
				} 

				// Debug.check();
			} 
		} 
		finally {
			manager.removeThread(this);
			message = null;

			// Debug.end();
		} 
	}
	public String toString() {
		MessageImpl m = message;

		if (m == null) {
			return "AgletThread[" + getName() + ", priority = " 
				   + getPriority() + ", valid = " + valid + ", start = " 
				   + start;
		} else {
			return "AgletThread[" + getName() + "," + m.toString() 
				   + ", priority = " + getPriority() + ", valid = " + valid 
				   + ", start = " + start;
		} 
	}
}

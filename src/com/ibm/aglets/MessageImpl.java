package com.ibm.aglets;

/*
 * @(#)MessageImpl.java
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
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.RequestRefusedException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;

// import com.ibm.awb.misc.Debug;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Permission;

import org.aglets.log.AgletsLogger;

import com.ibm.aglets.security.MessagePermission;
import com.ibm.aglets.thread.AgletThread;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageManager;
import com.ibm.aglet.security.MessageProtection;

/**
 * The <tt>MessageImpl</tt> class is an implementation of Message class.
 * 
 * @version     1.10    96/07/01
 * @author	Mitsuru Oshima
 * @author	ONO Kouichi
 */
public class MessageImpl extends Message implements Cloneable {

	transient FutureReplyImpl future = null;

	private int msg_type;
	private boolean defered = false;

	transient protected boolean delegatable = false;

	transient AgletThread thread = null;

	boolean waiting = false;
	
	private static AgletsLogger logger = AgletsLogger.getLogger("com.ibm.aglets.MessageImpl");

	/*
	 * For system and event message. These are all synchronus.
	 */
	protected MessageImpl() {
		super(null, null);
		msg_type = Message.SYNCHRONOUS;
		timestamp = System.currentTimeMillis();
	}
	/*
	 * 
	 */
	public MessageImpl(Message msg, FutureReplyImpl future, int msg_type, 
					   long timestamp) {
		super(msg.getKind(), msg.getArg());
		this.future = future;
		this.msg_type = msg_type;
		this.timestamp = timestamp;
	}
	protected MessageImpl(Object arg) {
		super(null, arg);
		msg_type = Message.SYNCHRONOUS;
		timestamp = System.currentTimeMillis();
	}
	final void activate(MessageManagerImpl manager) {
	    try{
		if (thread == null) {
			thread = manager.popThread();
		}
			thread.handleMessage(this);

//		} else {
//			synchronized (this) {
//				waiting = false;
//				notifyAll();
//			} 
//		}
	    }catch(AgletException e){
		logger.error("Exception caught while trying to activate a message",e);
	    }
	}
	
	final synchronized void cancel(String explain) {
		if (future != null) {
			future.cancel(explain);
		} 
	}
	public Object clone() {
		MessageImpl c = new MessageImpl(this, future, msg_type, timestamp);

		c.priority = priority;
		return c;
	}
	final synchronized void destroy() {
		if (thread == Thread.currentThread()) {
			System.err.println("waring: tring to destroy itself");
		} 

		if (waiting) {
			waiting = false;

			// all thread must be suspended before notify
			// to make sure...
			// thread.suspend();
			// Debug.check();
			notifyAll();
			final Thread th = thread;

			AccessController.doPrivileged(new PrivilegedAction() {
				public Object run() {

					// all thread will be stopped and them resumed
					th.stop();
					th.resume();
					return null;
				} 
			});
		} 
		thread = null;
	}
	/* synchronized */
	final void disable() {
		future = null;
		delegatable = false;
	}
	final synchronized void doWait() {

		// Debug.check();
		while (waiting) {
			try {
				wait();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			} 
		} 

		// Debug.check();
	}
	final synchronized void doWait(long timeout) {
		if (timeout == 0) {
			doWait();
		} else {

			// Debug.check();
			long until = System.currentTimeMillis() + timeout;
			long reft;

			while (waiting 
				   && (reft = (until - System.currentTimeMillis())) > 0) {
				try {
					wait(reft);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				} 
			} 

			// Debug.check();
		} 
	}
	final public void enableDeferedReply(boolean b) {
		defered = b;
	}
	/**
	 * 
	 */
	final void enableDelegation() {
		delegatable = true;
	}
	final public int getMessageType() {
		return msg_type;
	}
	Permission getPermission(String authority) {

		// or MessagePermission(authority)
		return new MessagePermission(authority, /*"message." +*/ getKind());
	}
	Permission getProtection(String authority) {

		// or MessageProtection(authority)
		return new MessageProtection(authority, /*"message." +*/ getKind());
	}
	public void handle(LocalAgletRef ref) throws InvalidAgletException {
		FutureReplyImpl f = future;
		Aglet aglet = ref.aglet;
		Throwable result_ex = null;
		boolean handled = false;

		try {

			// Debug.check();
			handled = aglet.handleMessage(this);

		} catch (RuntimeException ex) {

			// was trying to process someting..
			f.sendExceptionIfNeeded(ex);
			ex.printStackTrace();
		} catch (ThreadDeath ex) {
			f.sendExceptionIfNeeded(ex);
			throw ex;

		} catch (Throwable ex) {
			f.sendExceptionIfNeeded(ex);
			ex.printStackTrace();

		} 
		finally {

			// Debug.check();
			if (delegatable == false) {
				if (handled) {
					if (defered == false) {
						f.sendReplyIfNeeded(null);
					} 
				} else {
					f.cancel(toString());
				} 
			} 
		} 
	}
	/* synchronized */
	boolean isDelegatable() {
		return delegatable && future != null &&!future.available;
	}
	final boolean isWaiting() {
		return waiting;
	}
	/**
	 * 
	 */
	final public void sendException(Exception exp) {
		future.setExceptionAndNotify(exp);
	}
	/**
	 * 
	 */
	final public void sendReply() {
		future.setReplyAndNotify(null);
	}
	/**
	 * Sets the reply of the message.
	 */
	final public void sendReply(Object arg) {
		future.setReplyAndNotify(arg);
	}
	final void setWaiting() {
		waiting = true;
	}
	public String toString() {
		StringBuffer buff = new StringBuffer();

		buff.append("[Message : kind = " + kind + ": arg = " 
					+ String.valueOf(arg) + ": priority = " + priority);
		if (waiting) {
			buff.append(" :waiting ");
		} 
		buff.append(']');

		return buff.toString();
	}
	/**
	 * Gets back the thread.
	 * @return the thread
	 */
	protected synchronized AgletThread getThread() {
	    return thread;
	}
	/**
	 * Sets the thread value.
	 * @param thread the thread to set
	 */
	protected synchronized void setThread(AgletThread thread) {
	    this.thread = thread;
	}
	
	protected synchronized void setReplyAvailable(){
	    this.future.available = true;
	}
	

	/**
	 * Overrides the method that normalizes the priority. This method simply returns
	 * the priority provided as argument, thus you can set a system priority.
	 * @param priority the priority you want to set for this message
	 * @return the unchanged priority argument
	 */
	protected int normalizePriority(int priority){
	    return priority;
	}
	

}

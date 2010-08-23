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

import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;

import net.sourceforge.aglets.log.AgletsLogger;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.security.MessageProtection;
import com.ibm.aglets.security.MessagePermission;
import com.ibm.aglets.thread.AgletThread;

/**
 * The <tt>MessageImpl</tt> class is an implementation of Message class.
 * 
 * @version 1.10 96/07/01
 * @author Mitsuru Oshima
 * @author ONO Kouichi
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
	this.msg_type = Message.SYNCHRONOUS;
	this.timestamp = System.currentTimeMillis();
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
	this.msg_type = Message.SYNCHRONOUS;
	this.timestamp = System.currentTimeMillis();
    }

    final void activate(MessageManagerImpl manager) {
	try {
	    if (this.thread == null) {
		this.thread = manager.popThread();
	    }
	    this.thread.handleMessage(this);

	    // } else {
	    // synchronized (this) {
	    // waiting = false;
	    // notifyAll();
	    // }
	    // }
	} catch (AgletException e) {
	    logger.error("Exception caught while trying to activate a message", e);
	}
    }

    final synchronized void cancel(String explain) {
	if (this.future != null) {
	    this.future.cancel(explain);
	}
    }

    @Override
    public Object clone() {
	MessageImpl c = new MessageImpl(this, this.future, this.msg_type, this.timestamp);

	c.priority = this.priority;
	return c;
    }

    final synchronized void destroy() {
	if (this.thread == Thread.currentThread()) {
	    System.err.println("waring: tring to destroy itself");
	}

	if (this.waiting) {
	    this.waiting = false;

	    // all thread must be suspended before notify
	    // to make sure...
	    // thread.suspend();
	    // Debug.check();
	    this.notifyAll();
	    final Thread th = this.thread;

	    AccessController.doPrivileged(new PrivilegedAction() {
		@Override
		public Object run() {

		    // all thread will be stopped and them resumed
		    th.stop();
		    th.resume();
		    return null;
		}
	    });
	}
	this.thread = null;
    }

    /* synchronized */
    final void disable() {
	this.future = null;
	this.delegatable = false;
    }

    final synchronized void doWait() {

	// Debug.check();
	while (this.waiting) {
	    try {
		this.wait();
	    } catch (InterruptedException ex) {
		ex.printStackTrace();
	    }
	}

	// Debug.check();
    }

    final synchronized void doWait(long timeout) {
	if (timeout == 0) {
	    this.doWait();
	} else {

	    // Debug.check();
	    long until = System.currentTimeMillis() + timeout;
	    long reft;

	    while (this.waiting
		    && ((reft = (until - System.currentTimeMillis())) > 0)) {
		try {
		    this.wait(reft);
		} catch (InterruptedException ex) {
		    ex.printStackTrace();
		}
	    }

	    // Debug.check();
	}
    }

    @Override
    final public void enableDeferedReply(boolean b) {
	this.defered = b;
    }

    /**
     * 
     */
    final void enableDelegation() {
	this.delegatable = true;
    }

    @Override
    final public int getMessageType() {
	return this.msg_type;
    }

    Permission getPermission(String authority) {

	// or MessagePermission(authority)
	return new MessagePermission(authority, /* "message." + */this.getKind());
    }

    Permission getProtection(String authority) {

	// or MessageProtection(authority)
	return new MessageProtection(authority, /* "message." + */this.getKind());
    }

    public void handle(LocalAgletRef ref) throws InvalidAgletException {
	FutureReplyImpl f = this.future;
	Aglet aglet = ref.aglet;
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

	} finally {

	    // Debug.check();
	    if (this.delegatable == false) {
		if (handled) {
		    if (this.defered == false) {
			f.sendReplyIfNeeded(null);
		    }
		} else {
		    f.cancel(this.toString());
		}
	    }
	}
    }

    /* synchronized */
    boolean isDelegatable() {
	return this.delegatable && (this.future != null)
	&& !this.future.available;
    }

    final boolean isWaiting() {
	return this.waiting;
    }

    /**
     * 
     */
    @Override
    final public void sendException(Exception exp) {
	this.future.setExceptionAndNotify(exp);
    }

    /**
     * 
     */
    @Override
    final public void sendReply() {
	this.future.setReplyAndNotify(null);
    }

    /**
     * Sets the reply of the message.
     */
    @Override
    final public void sendReply(Object arg) {
	this.future.setReplyAndNotify(arg);
    }

    final void setWaiting() {
	this.waiting = true;
    }

    @Override
    public String toString() {
	StringBuffer buff = new StringBuffer();

	buff.append("[Message : kind = " + this.kind + ": arg = "
		+ String.valueOf(this.arg) + ": priority = " + this.priority);
	if (this.waiting) {
	    buff.append(" :waiting ");
	}
	buff.append(']');

	return buff.toString();
    }

    /**
     * Gets back the thread.
     * 
     * @return the thread
     */
    protected synchronized AgletThread getThread() {
	return this.thread;
    }

    /**
     * Sets the thread value.
     * 
     * @param thread
     *            the thread to set
     */
    protected synchronized void setThread(AgletThread thread) {
	this.thread = thread;
    }

    protected synchronized void setReplyAvailable() {
	this.future.available = true;
    }

    /**
     * Overrides the method that normalizes the priority. This method simply
     * returns the priority provided as argument, thus you can set a system
     * priority.
     * 
     * @param priority
     *            the priority you want to set for this message
     * @return the unchanged priority argument
     */
    @Override
    protected int normalizePriority(int priority) {
	return priority;
    }

}

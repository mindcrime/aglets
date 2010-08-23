package com.ibm.aglets;

/*
 * @(#)FutureReplyImpl.java
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

import java.util.Enumeration;
import java.util.Vector;

import com.ibm.aglet.AgletException;
import com.ibm.aglet.NotHandledException;
import com.ibm.aglet.message.FutureReply;
import com.ibm.aglet.message.MessageException;
import com.ibm.aglet.message.ReplySet;

/**
 * The <tt>FutureReplyImpl</tt> class is an implementation of
 * com.ibm.aglet.FutureReply abstract class.
 * 
 * @version 1.30 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 */
class FutureReplyImpl extends FutureReply {
    volatile boolean available = false;
    Object result;
    AgletException exception = null;
    Object set = null;

    @Override
    final synchronized protected void addedTo(ReplySet replySet) {
	if (this.available) {
	    replySet.done(this);
	    return;
	}
	if (this.set == null) {
	    this.set = replySet;
	} else if (this.set instanceof ReplySet) {
	    Vector v = new Vector();

	    v.addElement(this.set);
	    v.addElement(replySet);
	    this.set = v;
	} else {
	    ((Vector) this.set).addElement(replySet);
	}
    }

    synchronized void cancel(String msg) {
	if (!this.available) {
	    this.available = true;
	    this.exception = new NotHandledException(msg);
	    this.notifyAll();
	    this.notifySet();
	}
    }

    @Override
    final synchronized public Object getReply()
    throws MessageException,
    NotHandledException {
	this.waitForReply();
	if (this.exception != null) {
	    if (this.exception instanceof NotHandledException) {
		throw (NotHandledException) this.exception;
	    } else if (this.exception instanceof MessageException) {
		throw (MessageException) this.exception;
	    }
	}
	return this.result;
    }

    /*
     * synchronized void complete(boolean result, Throwable ex, String msg) { //
     * REMIND. critical session. if (! available) { if (result) { if (ex ==
     * null) { setReplyAndNotify(null); } else { setExceptionAndNotify(ex); } }
     * else { // REMIND: // Improvement is needed to define // precise
     * semantics. if (ex != null) { msg += " with exception " + ex.getMessage();
     * } cancel(msg); } } }
     */

    @Override
    final public boolean isAvailable() {
	return this.available;
    }

    /*
     * This must be called only from synchronized method.
     */
    final private void notifySet() {
	if (this.set == null) {
	    return;
	} else if (this.set instanceof ReplySet) {
	    ((ReplySet) this.set).done(this);
	} else if (this.set instanceof Vector) {
	    Enumeration e = ((Vector) this.set).elements();

	    while (e.hasMoreElements()) {
		ReplySet rep = (ReplySet) e.nextElement();

		rep.done(this);
	    }
	}
	this.set = null;
    }

    final synchronized void sendExceptionIfNeeded(Throwable ex) {
	if (!this.available) {
	    this.setExceptionAndNotify(ex);
	}
    }

    final synchronized void sendReplyIfNeeded(Object obj) {
	if (!this.available) {
	    this.setReplyAndNotify(obj);
	}
    }

    synchronized void setExceptionAndNotify(Throwable ex) {
	if (this.available) {
	    throw new IllegalAccessError("Reply has been already set");
	}
	this.available = true;
	this.exception = new MessageException(ex);
	this.notifyAll();

	this.notifySet();
    }

    synchronized void setReplyAndNotify(Object result) {
	if (this.available) {
	    throw new IllegalAccessError("Reply has been already set");
	}
	this.result = result;
	this.available = true;
	this.notifyAll();

	this.notifySet();
    }

    @Override
    final synchronized public void waitForReply() {
	while (this.available == false) {
	    try {
		this.wait();
	    } catch (InterruptedException ex) {
		ex.printStackTrace();
	    }
	}
    }

    @Override
    final synchronized public void waitForReply(long timeout) {
	if (timeout == 0) {
	    this.waitForReply();
	} else {
	    long until = System.currentTimeMillis() + timeout;
	    long reft;

	    while ((this.available == false)
		    && ((reft = (until - System.currentTimeMillis())) > 0)) {
		try {
		    this.wait(reft);
		} catch (InterruptedException ex) {
		    ex.printStackTrace();
		}
	    }
	}
    }
}

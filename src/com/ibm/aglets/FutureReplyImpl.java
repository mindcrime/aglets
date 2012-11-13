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
	final synchronized protected void addedTo(final ReplySet replySet) {
		if (available) {
			replySet.done(this);
			return;
		}
		if (set == null) {
			set = replySet;
		} else if (set instanceof ReplySet) {
			final Vector v = new Vector();

			v.addElement(set);
			v.addElement(replySet);
			set = v;
		} else {
			((Vector) set).addElement(replySet);
		}
	}

	synchronized void cancel(final String msg) {
		if (!available) {
			available = true;
			exception = new NotHandledException(msg);
			notifyAll();
			notifySet();
		}
	}

	@Override
	final synchronized public Object getReply()
	throws MessageException,
	NotHandledException {
		this.waitForReply();
		if (exception != null) {
			if (exception instanceof NotHandledException) {
				throw (NotHandledException) exception;
			} else if (exception instanceof MessageException) {
				throw (MessageException) exception;
			}
		}
		return result;
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
		return available;
	}

	/*
	 * This must be called only from synchronized method.
	 */
	final private void notifySet() {
		if (set == null) {
			return;
		} else if (set instanceof ReplySet) {
			((ReplySet) set).done(this);
		} else if (set instanceof Vector) {
			final Enumeration e = ((Vector) set).elements();

			while (e.hasMoreElements()) {
				final ReplySet rep = (ReplySet) e.nextElement();

				rep.done(this);
			}
		}
		set = null;
	}

	final synchronized void sendExceptionIfNeeded(final Throwable ex) {
		if (!available) {
			setExceptionAndNotify(ex);
		}
	}

	final synchronized void sendReplyIfNeeded(final Object obj) {
		if (!available) {
			setReplyAndNotify(obj);
		}
	}

	synchronized void setExceptionAndNotify(final Throwable ex) {
		if (available) {
			throw new IllegalAccessError("Reply has been already set");
		}
		available = true;
		exception = new MessageException(ex);
		notifyAll();

		notifySet();
	}

	synchronized void setReplyAndNotify(final Object result) {
		if (available) {
			throw new IllegalAccessError("Reply has been already set");
		}
		this.result = result;
		available = true;
		notifyAll();

		notifySet();
	}

	@Override
	final synchronized public void waitForReply() {
		while (available == false) {
			try {
				this.wait();
			} catch (final InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	final synchronized public void waitForReply(final long timeout) {
		if (timeout == 0) {
			this.waitForReply();
		} else {
			final long until = System.currentTimeMillis() + timeout;
			long reft;

			while ((available == false)
					&& ((reft = (until - System.currentTimeMillis())) > 0)) {
				try {
					this.wait(reft);
				} catch (final InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}

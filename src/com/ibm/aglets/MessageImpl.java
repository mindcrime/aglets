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

	/**
	 * 
	 */
	private static final long serialVersionUID = 6493363552096602045L;

	transient FutureReplyImpl future = null;

	private final int msg_type;
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
	public MessageImpl(final Message msg, final FutureReplyImpl future, final int msg_type,
	                   final long timestamp) {
		super(msg.getKind(), msg.getArg());
		this.future = future;
		this.msg_type = msg_type;
		this.timestamp = timestamp;
	}

	protected MessageImpl(final Object arg) {
		super(null, arg);
		msg_type = Message.SYNCHRONOUS;
		timestamp = System.currentTimeMillis();
	}

	final void activate(final MessageManagerImpl manager) {
		try {
			if (thread == null) {
				thread = manager.popThread();
			}
			thread.handleMessage(this);

			// } else {
			// synchronized (this) {
			// waiting = false;
			// notifyAll();
			// }
			// }
		} catch (final AgletException e) {
			logger.error("Exception caught while trying to activate a message", e);
		}
	}

	final synchronized void cancel(final String explain) {
		if (future != null) {
			future.cancel(explain);
		}
	}

	@Override
	public Object clone() {
		final MessageImpl c = new MessageImpl(this, future, msg_type, timestamp);

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
				@Override
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
				this.wait();
			} catch (final InterruptedException ex) {
				ex.printStackTrace();
			}
		}

		// Debug.check();
	}

	final synchronized void doWait(final long timeout) {
		if (timeout == 0) {
			this.doWait();
		} else {

			// Debug.check();
			final long until = System.currentTimeMillis() + timeout;
			long reft;

			while (waiting
					&& ((reft = (until - System.currentTimeMillis())) > 0)) {
				try {
					this.wait(reft);
				} catch (final InterruptedException ex) {
					ex.printStackTrace();
				}
			}

			// Debug.check();
		}
	}

	@Override
	final public void enableDeferedReply(final boolean b) {
		defered = b;
	}

	/**
	 * 
	 */
	final void enableDelegation() {
		delegatable = true;
	}

	@Override
	final public int getMessageType() {
		return msg_type;
	}

	Permission getPermission(final String authority) {

		// or MessagePermission(authority)
		return new MessagePermission(authority, /* "message." + */getKind());
	}

	Permission getProtection(final String authority) {

		// or MessageProtection(authority)
		return new MessageProtection(authority, /* "message." + */getKind());
	}

	/**
	 * Gets back the thread.
	 * 
	 * @return the thread
	 */
	protected synchronized AgletThread getThread() {
		return thread;
	}

	public void handle(final LocalAgletRef ref) throws InvalidAgletException {
		final FutureReplyImpl f = future;
		final Aglet aglet = ref.aglet;
		boolean handled = false;

		try {

			// Debug.check();
			handled = aglet.handleMessage(this);

		} catch (final RuntimeException ex) {

			// was trying to process someting..
			f.sendExceptionIfNeeded(ex);
			ex.printStackTrace();
		} catch (final ThreadDeath ex) {
			f.sendExceptionIfNeeded(ex);
			throw ex;

		} catch (final Throwable ex) {
			f.sendExceptionIfNeeded(ex);
			ex.printStackTrace();

		} finally {

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
		return delegatable && (future != null)
		&& !future.available;
	}

	final boolean isWaiting() {
		return waiting;
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
	protected int normalizePriority(final int priority) {
		return priority;
	}

	/**
	 * 
	 */
	@Override
	final public void sendException(final Exception exp) {
		future.setExceptionAndNotify(exp);
	}

	/**
	 * 
	 */
	@Override
	final public void sendReply() {
		future.setReplyAndNotify(null);
	}

	/**
	 * Sets the reply of the message.
	 */
	@Override
	final public void sendReply(final Object arg) {
		future.setReplyAndNotify(arg);
	}

	protected synchronized void setReplyAvailable() {
		future.available = true;
	}

	/**
	 * Sets the thread value.
	 * 
	 * @param thread
	 *            the thread to set
	 */
	protected synchronized void setThread(final AgletThread thread) {
		this.thread = thread;
	}

	final void setWaiting() {
		waiting = true;
	}

	@Override
	public String toString() {
		final StringBuffer buff = new StringBuffer();

		buff.append("[Message : kind = " + kind + ": arg = "
				+ String.valueOf(arg) + ": priority = " + priority);
		if (waiting) {
			buff.append(" :waiting ");
		}
		buff.append(']');

		return buff.toString();
	}

}

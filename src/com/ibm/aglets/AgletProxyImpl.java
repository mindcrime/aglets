package com.ibm.aglets;

/*
 * @(#)AgletProxyImpl.java
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

import java.io.IOException;
import java.net.URL;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletID;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.NotHandledException;
import com.ibm.aglet.Ticket;
import com.ibm.aglet.message.FutureReply;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageException;
import com.ibm.awb.weakref.VirtualRef;

/**
 * @version 1.00 96/11/25
 * @author Mitsuru Oshima
 * @author ONO Kouichi
 */
public final class AgletProxyImpl extends VirtualRef implements AgletProxy {

	static final long serialVersionUID = 4237434474691700860L;

	/*
	 * Creates an remote aglet proxy with an specific aglet.
	 */
	AgletProxyImpl(final AgletRef ref) {
		super(ref);
	}

	@Override
	public void activate() throws IOException, AgletException {
		getAgletRef().activate();
	}

	/**
	 * Clones the aglet proxy. Note that the cloned aglet will get activated. If
	 * you like to get cloned aglet which is not activated, throw ThreadDeath
	 * exception in the onClone method.
	 * 
	 * @return the new aglet proxy what holds cloned aglet.
	 * @exception CloneNotSupportedException
	 *                if the cloning fails.
	 * @exception InvalidAgletException
	 *                if the aglet is invalid.
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		final Message msg = new SystemMessage(Message.CLONE, null, SystemMessage.CLONE_REQUEST);

		try {
			return getAgletRef().sendMessage(msg);
		} catch (final InvalidAgletException ex) {
			throw new CloneNotSupportedException(ex.getMessage());
		} catch (final MessageException ex) {
			throw new CloneNotSupportedException(ex.getException().getMessage());
		} catch (final NotHandledException ex) {
			throw new CloneNotSupportedException(ex.getMessage());
		}
	}

	/**
	 * Deactivate aglet till the specified date. The deactivated aglet are
	 * stored in the aglet spool.
	 * 
	 * @param duration
	 *            the term to sleep
	 * @exception AgletEception
	 *                if can not deactivate the aglet.
	 */
	@Override
	public void deactivate(final long duration)
	throws IOException,
	InvalidAgletException {
		final Message msg = new SystemMessage(Message.DEACTIVATE, new Long(duration), SystemMessage.DEACTIVATE_REQUEST);

		try {
			getAgletRef().sendMessage(msg);
		} catch (final MessageException ex) {
			final Throwable t = ex.getException();

			if (t instanceof IOException) {
				throw (IOException) t;
			} else if (t instanceof InvalidAgletException) {
				throw (InvalidAgletException) t;
			} else if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			}
			throw new InvalidAgletException(ex.getMessage());
		} catch (final NotHandledException ex) {
			throw new InvalidAgletException(ex.getMessage());
		}
	}

	/**
	 * Delegates a message
	 */
	@Override
	public void delegateMessage(final Message msg) throws InvalidAgletException {
		getAgletRef().delegateMessage(msg);
	}

	// trip with Ticket
	@Override
	public AgletProxy dispatch(final Ticket ticket)
	throws IOException,
	AgletException {
		final Message msg = new SystemMessage(Message.DISPATCH, ticket, SystemMessage.DISPATCH_REQUEST);

		try {
			return (AgletProxy) getAgletRef().sendMessage(msg);
		} catch (final MessageException ex) {
			final Throwable t = ex.getException();

			if (t instanceof IOException) {
				throw (IOException) t;
			} else if (t instanceof AgletException) {
				throw (AgletException) t;
			} else if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			}
			throw new InvalidAgletException(ex.getMessage());
		} catch (final NotHandledException ex) {
			throw new InvalidAgletException(ex.getMessage());
		}
	}

	/*
	 * dispatches
	 */
	@Override
	public AgletProxy dispatch(final URL url) throws IOException, AgletException {
		return this.dispatch(new Ticket(url));
	}

	/**
	 * Disposes the aglet.
	 * 
	 * @exception InvalidAgletException
	 *                if the aglet is invalid.
	 */
	@Override
	public void dispose() throws InvalidAgletException {
		final Message msg = new SystemMessage(Message.DISPOSE, null, SystemMessage.DISPOSE_REQUEST);

		try {
			getAgletRef().sendMessage(msg);
		} catch (final MessageException ex) {
			final Throwable t = ex.getException();

			if (t instanceof InvalidAgletException) {
				throw (InvalidAgletException) t;
			} else if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			}
			ex.printStackTrace();
			throw new InvalidAgletException(ex.getMessage());
		} catch (final NotHandledException ex) {
			throw new InvalidAgletException(ex.getMessage());
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof AgletProxyImpl) {
			try {
				final AgletID target = ((AgletProxyImpl) obj).getAgletID();

				return getAgletID().equals(target);
			} catch (final Exception ex) {
				return false;
			}
		}
		return false;
	}

	// # /**
	// # * Gets the allowance: availability of the aglet's resources.
	// # * @return the Allowance object
	// # */
	// # public Allowance getAllowance() throws InvalidAgletException {
	// # AgletRef ref = getAgletRef();
	// # ref.checkValidation(); // not necessarily be critical region
	// # return ref.getAllowance();
	// # }

	/**
	 * Gets the aglet's class name.
	 * 
	 * @return the address.
	 */
	@Override
	public String getAddress() throws InvalidAgletException {
		return getAgletRef().getAddress();
	}

	/**
	 * Gets the aglet. If the aglet is access protected it will require the
	 * right key to get access.
	 * 
	 * @return the aglet
	 * @exception SecurityException
	 *                if the current execution is not allowed.
	 */
	@Override
	public Aglet getAglet() throws InvalidAgletException {
		return getAgletRef().getAglet();
	}

	/**
	 * Gets the aglet's class name.
	 * 
	 * @return the class name.
	 */
	@Override
	public String getAgletClassName() throws InvalidAgletException {
		return getAgletInfo().getAgletClassName();
	}

	/**
	 * Gets the aglet's id
	 * 
	 * @return the aglet's id
	 * @exception InvalidAgletException
	 *                if the aglet is not valid.
	 */
	@Override
	public AgletID getAgletID() throws InvalidAgletException {
		return getAgletInfo().getAgletID();
	}

	/**
	 * Gets the information of the aglet
	 * 
	 * @return the AgletInfo of the aglet
	 */
	@Override
	public AgletInfo getAgletInfo() throws InvalidAgletException {
		final AgletRef ref = getAgletRef();

		return ref.getAgletInfo();
	}

	AgletRef getAgletRef() {
		return (AgletRef) getRef();
	}

	/**
	 * Gets the URL of the aglet's class. Null is returned if the class is in
	 * the set of common classes.
	 * 
	 * @return the class URL.
	 */
	public URL getCodeBase() throws InvalidAgletException {
		return getAgletInfo().getCodeBase();
	}

	/*
	 * 
	 */
	@Override
	public int hashCode() {
		try {
			return getAgletID().hashCode();
		} catch (final Exception ex) {
			return super.hashCode();
		}
	}

	/*
	 * Checks if it's active.
	 */
	@Override
	public boolean isActive() {
		return getAgletRef().isActive();
	}

	/**
	 * Checks if it's remote
	 */
	@Override
	public boolean isRemote() {
		return getAgletRef().isRemote();
	}

	@Override
	public boolean isState(final int state) {
		return getAgletRef().isState(state);
	}

	/**
	 * Checks if it's valid.
	 */
	@Override
	public boolean isValid() {
		return getAgletRef().isValid();
	}

	public void resume() throws AgletException {
		getAgletRef().resume();
	}

	/**
	 * Sends a message in asynchronous way.
	 * 
	 * @param msg
	 *            the message to send
	 */
	@Override
	synchronized public FutureReply sendAsyncMessage(final Message msg)
	throws InvalidAgletException {
		return getAgletRef().sendFutureMessage(msg);
	}

	/**
	 * Sends a future message in asynchronous way.
	 * 
	 * @param msg
	 *            the message to send
	 */
	@Override
	synchronized public FutureReply sendFutureMessage(final Message msg)
	throws InvalidAgletException {
		return getAgletRef().sendFutureMessage(msg);
	}

	/**
	 * Sends a message in synchronous way.
	 * 
	 * @param msg
	 *            the message to send
	 */
	@Override
	public Object sendMessage(final Message msg)
	throws MessageException,
	InvalidAgletException,
	NotHandledException {
		return getAgletRef().sendMessage(msg);
	}

	/**
	 * Sends a oneway message
	 * 
	 * @param msg
	 *            the message to send
	 */
	@Override
	synchronized public void sendOnewayMessage(final Message msg)
	throws InvalidAgletException {
		getAgletRef().sendOnewayMessage(msg);
	}

	/**
	 * Suspend the aglet. That is, objects of the suspended aglet will remain in
	 * the memory.
	 * 
	 * @param duration
	 *            the term to sleep
	 * @exception AgletEception
	 *                if can not suspend the aglet.
	 * @exception IllegalArgumentException
	 *                if the minutes parameter is negative.
	 */
	@Override
	public void suspend(final long duration) throws InvalidAgletException {
		final Message msg = new SystemMessage(Message.DEACTIVATE, new Long(duration), SystemMessage.SUSPEND_REQUEST);

		try {
			getAgletRef().sendMessage(msg);
		} catch (final MessageException ex) {
			final Throwable t = ex.getException();

			if (t instanceof InvalidAgletException) {
				throw (InvalidAgletException) t;
			} else if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			}
			throw new InvalidAgletException(ex.getMessage());
		} catch (final NotHandledException ex) {
			throw new InvalidAgletException(ex.getMessage());
		}
	}

	public String toHTMLString() {
		final AgletRef ref = getAgletRef();

		if (ref instanceof LocalAgletRef)
			return ((LocalAgletRef) ref).toHTMLString();
		else
			return ref.toString();
	}

	@Override
	public String toString() {
		return "AgletProxyImpl : " + getAgletRef();
	}
}

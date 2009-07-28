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

import java.util.Date;
import java.util.Enumeration;

import java.net.URL;
import java.net.MalformedURLException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.ibm.aglet.*;
import com.ibm.aglet.message.FutureReply;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageException;

// # import com.ibm.aglets.security.Allowance;

import com.ibm.awb.weakref.VirtualRef;

/**
 * @version     1.00    96/11/25
 * @author	Mitsuru Oshima
 * @author	ONO Kouichi
 */
public final class AgletProxyImpl extends VirtualRef implements AgletProxy {

	static final long serialVersionUID = 4237434474691700860L;

	/*
	 * Creates an remote aglet proxy with an specific aglet.
	 */
	AgletProxyImpl(AgletRef ref) {
		super(ref);
	}
	public void activate() throws IOException, AgletException {
		getAgletRef().activate();
	}
	/**
	 * Clones the aglet proxy. Note that the cloned aglet will get activated.
	 * If you like to get cloned aglet which is not activated, throw
	 * ThreadDeath exception in the onClone method.
	 * 
	 * @return  the new aglet proxy what holds cloned aglet.
	 * @exception CloneNotSupportedException if the cloning fails.
	 * @exception InvalidAgletException if the aglet is invalid.
	 */
	public Object clone() throws CloneNotSupportedException {
		Message msg = new SystemMessage(Message.CLONE, null, 
										SystemMessage.CLONE_REQUEST);

		try {
			return getAgletRef().sendMessage(msg);
		} catch (InvalidAgletException ex) {
			throw new CloneNotSupportedException(ex.getMessage());
		} catch (MessageException ex) {
			throw new CloneNotSupportedException(ex.getException()
				.getMessage());
		} catch (NotHandledException ex) {
			throw new CloneNotSupportedException(ex.getMessage());
		} 
	}
	/**
	 * Deactivate aglet till the specified date. The deactivated aglet are
	 * stored in the aglet spool.
	 * @param duraration the term to sleep
	 * @exception AgletEception if can not deactivate the aglet.
	 */
	public void deactivate(long duration) 
			throws IOException, InvalidAgletException {
		Message msg = new SystemMessage(Message.DEACTIVATE, 
										new Long(duration), 
										SystemMessage.DEACTIVATE_REQUEST);

		try {
			getAgletRef().sendMessage(msg);
		} catch (MessageException ex) {
			Throwable t = ex.getException();

			if (t instanceof IOException) {
				throw (IOException)t;
			} else if (t instanceof InvalidAgletException) {
				throw (InvalidAgletException)t;
			} else if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			} 
			throw new InvalidAgletException(ex.getMessage());
		} catch (NotHandledException ex) {
			throw new InvalidAgletException(ex.getMessage());
		} 
	}
	/**
	 * Delegates a message
	 */
	public void delegateMessage(Message msg) throws InvalidAgletException {
		getAgletRef().delegateMessage(msg);
	}
	// trip with Ticket
	public AgletProxy dispatch(Ticket ticket) 
			throws IOException, AgletException {
		Message msg = new SystemMessage(Message.DISPATCH, ticket, 
										SystemMessage.DISPATCH_REQUEST);

		try {
			return (AgletProxy)getAgletRef().sendMessage(msg);
		} catch (MessageException ex) {
			Throwable t = ex.getException();

			if (t instanceof IOException) {
				throw (IOException)t;
			} else if (t instanceof AgletException) {
				throw (AgletException)t;
			} else if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			} 
			throw new InvalidAgletException(ex.getMessage());
		} catch (NotHandledException ex) {
			throw new InvalidAgletException(ex.getMessage());
		} 
	}
	/*
	 * dispatches
	 */
	public AgletProxy dispatch(URL url) throws IOException, AgletException {
		return dispatch(new Ticket(url));
	}
	/**
	 * Disposes the aglet.
	 * @exception InvalidAgletException if the aglet is invalid.
	 */
	public void dispose() throws InvalidAgletException {
		Message msg = new SystemMessage(Message.DISPOSE, null, 
										SystemMessage.DISPOSE_REQUEST);

		try {
			getAgletRef().sendMessage(msg);
		} catch (MessageException ex) {
			Throwable t = ex.getException();

			if (t instanceof InvalidAgletException) {
				throw (InvalidAgletException)t;
			} else if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			} 
			ex.printStackTrace();
			throw new InvalidAgletException(ex.getMessage());
		} catch (NotHandledException ex) {
			throw new InvalidAgletException(ex.getMessage());
		} 
	}
	public boolean equals(Object obj) {
		if (obj instanceof AgletProxyImpl) {
			try {
				AgletID target = ((AgletProxyImpl)obj).getAgletID();

				return getAgletID().equals(target);
			} catch (Exception ex) {
				return false;
			} 
		} 
		return false;
	}
	// #     /**
	// #      * Gets the allowance: availability of the aglet's resources.
	// #      * @return the Allowance object
	// #      */
	// #     public Allowance getAllowance() throws InvalidAgletException {
	// # 	AgletRef ref = getAgletRef();
	// # 	ref.checkValidation(); // not necessarily be critical region
	// # 	return ref.getAllowance();
	// #     }

	/**
	 * Gets the aglet's class name.
	 * 
	 * @return the address.
	 */
	public String getAddress() throws InvalidAgletException {
		return getAgletRef().getAddress();
	}
	/**
	 * Gets the aglet. If the aglet is access protected it will require
	 * the right key to get access.
	 * @return the aglet
	 * @exception SecurityException if the current execution is not allowed.
	 */
	public Aglet getAglet() throws InvalidAgletException {
		return getAgletRef().getAglet();
	}
	/**
	 * Gets the aglet's class name.
	 * @return the class name.
	 */
	public String getAgletClassName() throws InvalidAgletException {
		return getAgletInfo().getAgletClassName();
	}
	/**
	 * Gets the aglet's id
	 * @return the aglet's id
	 * @exception InvalidAgletException if the aglet is not valid.
	 */
	public AgletID getAgletID() throws InvalidAgletException {
		return getAgletInfo().getAgletID();
	}
	/**
	 * Gets the information of the aglet
	 * @return the AgletInfo of the aglet
	 */
	public AgletInfo getAgletInfo() throws InvalidAgletException {
		AgletRef ref = getAgletRef();

		return ref.getAgletInfo();
	}
	AgletRef getAgletRef() {
		return (AgletRef)getRef();
	}
	/**
	 * Gets the URL of the aglet's class.
	 * Null is returned if the class is in the set of common classes.
	 * @return the class URL.
	 */
	public URL getCodeBase() throws InvalidAgletException {
		return getAgletInfo().getCodeBase();
	}
	/*
	 * 
	 */
	public int hashCode() {
		try {
			return getAgletID().hashCode();
		} catch (Exception ex) {
			return super.hashCode();
		} 
	}
	/*
	 * Checks if it's active.
	 */
	public boolean isActive() {
		return getAgletRef().isActive();
	}
	/**
	 * Checks if it's remote
	 */
	public boolean isRemote() {
		return getAgletRef().isRemote();
	}
	public boolean isState(int state) {
		return getAgletRef().isState(state);
	}
	/**
	 * Checks if it's valid.
	 */
	public boolean isValid() {
		return getAgletRef().isValid();
	}
	public void resume() throws AgletException {
		getAgletRef().resume();
	}
	/**
	 * Sends a message in asynchronous way.
	 * @param msg the message to send
	 */
	synchronized public FutureReply sendAsyncMessage(Message msg) 
			throws InvalidAgletException {
		return getAgletRef().sendFutureMessage(msg);
	}
	/**
	 * Sends a future message in asynchronous way.
	 * @param msg the message to send
	 */
	synchronized public FutureReply sendFutureMessage(Message msg) 
			throws InvalidAgletException {
		return getAgletRef().sendFutureMessage(msg);
	}
	/**
	 * Sends a message in synchronous way.
	 * @param msg the message to send
	 */
	public Object sendMessage(Message msg) 
			throws MessageException, InvalidAgletException, 
				   NotHandledException {
		return getAgletRef().sendMessage(msg);
	}
	/**
	 * Sends a oneway message
	 * @param msg the message to send
	 */
	synchronized public void sendOnewayMessage(Message msg) 
			throws InvalidAgletException {
		getAgletRef().sendOnewayMessage(msg);
	}
	/**
	 * Suspend the aglet.  That is, objects of the suspended aglet will remain in
	 * the memory.
	 * @param duraration the term to sleep
	 * @exception AgletEception if can not suspend the aglet.
	 * @exception IllegalArgumentException if the minutes parameter is negative.
	 */
	public void suspend(long duration) throws InvalidAgletException {
		Message msg = new SystemMessage(Message.DEACTIVATE, 
										new Long(duration), 
										SystemMessage.SUSPEND_REQUEST);

		try {
			getAgletRef().sendMessage(msg);
		} catch (MessageException ex) {
			Throwable t = ex.getException();

			if (t instanceof InvalidAgletException) {
				throw (InvalidAgletException)t;
			} else if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			} 
			throw new InvalidAgletException(ex.getMessage());
		} catch (NotHandledException ex) {
			throw new InvalidAgletException(ex.getMessage());
		} 
	}
	
	public String toString() {
		return "AgletProxyImpl : " + getAgletRef();
	}
	
	public String toHTMLString(){
	    AgletRef ref = this.getAgletRef();
	    
	    if( ref instanceof LocalAgletRef )
		return ((LocalAgletRef)ref).toHTMLString();
	    else
		return ref.toString();
	}
}

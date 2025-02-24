package com.ibm.agletx.patterns;

/*
 * @(#)Messenger.java
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
import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletID;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.message.Arguments;
import com.ibm.aglet.message.Message;
import com.ibm.agletx.util.MessengerItinerary;

/**
 * Create a Messenger by calling the static method <tt>create</tt>. The
 * messenger will get dispatched automatically. The messenger carries a message
 * between two remote aglets. Upon reaching the host of the receiver aglet and
 * sending the message, the messenger complete its job and so, it is disposed.
 * If a Messenger cannot be dispatched, it is disposed.
 * 
 * @version 1.01 96/08/18
 * @author Danny B. Lange
 * @author Yariv Aridor
 */

public final class Messenger extends Aglet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5372840358253267377L;
	/**
	 * Creates a messenger.
	 * 
	 * @param context
	 *            the aglet context in which the messenger should be created.
	 * @param dest
	 *            the host of the receiver aglet.
	 * @param id
	 *            the identifier of the receiver aglet.
	 * @param message
	 *            the message object.
	 * @return an aglet proxy for the messenger.
	 * @exception AgletException
	 *                if initialization fails.
	 */
	static public AgletProxy create(
	                                final AgletContext context,
	                                final URL dest,
	                                final AgletID id,
	                                final Message message)
	throws IOException,
	AgletException {
		return create(context, null, dest, id, message);
	}

	/**
	 * Creates a messenger.
	 * 
	 * @param context
	 *            the aglet context in which the messenger should be created.
	 * @param codebase
	 *            the codebase of this class
	 * @param dest
	 *            the host of the receiver aglet.
	 * @param id
	 *            the identifier of the receiver aglet.
	 * @param message
	 *            the message object.
	 * @return an aglet proxy for the messenger.
	 * @exception AgletException
	 *                if initialization fails.
	 */
	static public AgletProxy create(
	                                final AgletContext context,
	                                final URL codebase,
	                                final URL dest,
	                                final AgletID id,
	                                final Message message)
	throws IOException,
	AgletException {
		final Arguments args = new Arguments();

		args.setArg("destination", dest);
		args.setArg("aglet.id", id);
		args.setArg("message", message);
		try {
			return context.createAglet(codebase, "com.ibm.agletx.patterns.Messenger", args);
		} catch (final InstantiationException ex) {
			throw new AgletException(ex.getClass().getName() + ':'
					+ ex.getMessage());
		} catch (final ClassNotFoundException ex) {
			throw new AgletException(ex.getClass().getName() + ':'
					+ ex.getMessage());
		}
	}

	MessengerItinerary itin = null;

	/**
	 * Initializes the messenger. The argument object containes the destination
	 * URL, the message and the identifier of the receiver aglet.
	 * 
	 * @exception AgletException
	 *                if initialization fails.
	 */
	@Override
	public synchronized void onCreation(final Object object) {
		final Arguments obj = (Arguments) object;
		final URL url = (URL) (obj.getArg("destination"));
		final Message message = (Message) (obj.getArg("message"));
		final AgletID id = (AgletID) (obj.getArg("aglet.id"));

		itin = new MessengerItinerary(this, message);
		itin.addAglet(url.toString(), id);
		itin.startTrip();
	}

	/**
	 * Universal entry point for the messenger's execution thread.
	 */
	@Override
	public void run() {
		if (itin.atLastDestination()) {
			dispose();
		}
	}
}

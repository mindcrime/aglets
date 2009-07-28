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

import com.ibm.aglet.*;
import com.ibm.aglet.message.Arguments;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.util.*;
import com.ibm.agletx.util.MessengerItinerary;
import java.net.URL;
import java.io.IOException;

/**
 * Create a Messenger by calling the static method <tt>create</tt>.
 * The messenger will get dispatched automatically.
 * The messenger carries a message between two remote aglets.
 * Upon reaching the host of the receiver aglet and sending the
 * message, the messenger complete its job and so, it is disposed.
 * If a Messenger cannot be dispatched, it is disposed.
 * 
 * @version     1.01  96/08/18
 * @author      Danny B. Lange
 * @author      Yariv Aridor
 */

public final class Messenger extends Aglet {


	MessengerItinerary itin = null;

	/**
	 * Creates a messenger.
	 * @param context the aglet context in which the messenger should be created.
	 * @param dest the host of the receiver aglet.
	 * @param id the identifier of the receiver aglet.
	 * @param message the message object.
	 * @return an aglet proxy for the messenger.
	 * @exception AgletException if initialization fails.
	 */
	static public AgletProxy create(AgletContext context, URL dest, 
									AgletID id, 
									Message message) throws IOException, 
									AgletException {
		return create(context, null, dest, id, message);
	}
	/**
	 * Creates a messenger.
	 * @param context the aglet context in which the messenger should be created.
	 * @param codebase the codebase of this class
	 * @param dest the host of the receiver aglet.
	 * @param id the identifier of the receiver aglet.
	 * @param message the message object.
	 * @return an aglet proxy for the messenger.
	 * @exception AgletException if initialization fails.
	 */
	static public AgletProxy create(AgletContext context, URL codebase, 
									URL dest, AgletID id, 
									Message message) throws IOException, 
									AgletException {
		Arguments args = new Arguments();

		args.setArg("destination", dest);
		args.setArg("aglet.id", id);
		args.setArg("message", message);
		try {
			return context.createAglet(codebase, 
									   "com.ibm.agletx.patterns.Messenger", 
									   args);
		} catch (InstantiationException ex) {
			throw new AgletException(ex.getClass().getName() + ':' 
									 + ex.getMessage());
		} catch (ClassNotFoundException ex) {
			throw new AgletException(ex.getClass().getName() + ':' 
									 + ex.getMessage());
		} 
	}
	/**
	 * Initializes the messenger. The argument object containes
	 * the destination URL, the message and the identifier of the
	 * receiver aglet.
	 * @exception AgletException if initialization fails.
	 */
	public synchronized void onCreation(Object object) {
		Arguments obj = (Arguments)object;
		URL url = (URL)(obj.getArg("destination"));
		Message message = (Message)(obj.getArg("message"));
		AgletID id = (AgletID)(obj.getArg("aglet.id"));

		itin = new MessengerItinerary(this, message);
		itin.addAglet(url.toString(), id);
		itin.startTrip();
	}
	/**
	 * Universal entry point for the messenger's execution thread.
	 */
	public void run() {
		if (itin.atLastDestination()) {
			dispose();
		} 
	}
}

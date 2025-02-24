package com.ibm.agletx.patterns;

/*
 * @(#)Slave.java
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
import java.util.Vector;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletID;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.message.Arguments;
import com.ibm.aglet.message.Message;

/**
 * Create a slave by calling the static method <tt>create</tt>. The slave will
 * get dispatched automatically. <br>
 * Given an itinerary, the slave is travelled from one destination to another
 * while repeating a local computation in every destination. Then, a final
 * result is delivered to the slave's Master (the creator of the slave). During
 * its tour the slave aglet skips destinations which are not available.
 * 
 * <br>
 * When a slave: <br>
 * 1) completes its tour. <br>
 * 2) encounters an error during a local computation. <br>
 * 3) cannot be further dispatched to yet unvisited destinations. <br>
 * it immediately returns to its origin host and submits the intermediate
 * result.
 * 
 * @version 1.0 96/08/18
 * @author Danny B. Lange
 * @author Yariv Aridor
 */

public abstract class Slave extends Aglet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3292145267680928210L;

	/**
	 * Create a slave.
	 * 
	 * @param url
	 *            the url of the aglet class.
	 * @param name
	 *            the name of the aglet class.
	 * @param context
	 *            the aglet context in which the slave should be created.
	 * @param master
	 *            the master aglet.
	 * @param itinerary
	 *            A vector of addresses of destinations
	 * @param argument
	 *            the
	 * 
	 *            <pre>
	 * argument
	 * </pre>
	 * 
	 *            object.
	 * @return an aglet proxy for the slave.
	 * @exception AgletException
	 *                if initialization fails.
	 */
	static public AgletProxy create(
	                                final URL url,
	                                final String name,
	                                final AgletContext context,
	                                final Aglet master,
	                                final Vector itinerary,
	                                final Object argument)
	throws IOException,
	AgletException {
		final Arguments args = new Arguments();

		args.setArg("master", master.getAgletID());
		args.setArg("itinerary", itinerary);
		args.setArg("argument", argument);
		try {
			return context.createAglet(url, name, args);
		} catch (final InstantiationException ex) {
			throw new AgletException(ex.getClass().getName() + ':'
					+ ex.getMessage());
		} catch (final ClassNotFoundException ex) {
			throw new AgletException(ex.getClass().getName() + ':'
					+ ex.getMessage());
		}
	}

	// the master identifier
	private AgletID master = null;
	private SlaveAgletItinerary itin = null;

	/**
	 * The protected variable that accumulates the results of the local task
	 * performed in every destination.
	 */
	protected Object RESULT = null;

	/**
	 * The protected variable that carries an argument for the local task
	 * performed in every destination.
	 */
	protected Object ARGUMENT = null;

	/**
	 * This method should be overridden to specify the local task of the slave.
	 * 
	 * @exception AgletException
	 *                if fails to complete.
	 */
	abstract protected void doJob() throws Exception;

	public AgletID getMaster() {
		return master;
	}

	private AgletProxy getMasterProxy(final AgletID master, final Aglet aglet)
	throws AgletException {
		return getAgletContext().getAgletProxy(master);
	}

	/**
	 * Return the address of origin of the Slave (i.e. the host from which it
	 * started its tour).
	 * 
	 * @return the address of the origin.
	 */
	public String getOrigin() {
		return itin.getOrigin();
	}

	// -- Handler for messages
	//
	@Override
	public boolean handleMessage(final Message msg) {
		try {
			if (msg.sameKind("initializeJob")) {
				initializeJob();
			} else if (msg.sameKind("doJob")) {
				doJob();
			} else if (msg.sameKind("onError")) {
				onError((SlaveError) (msg.getArg()));
			} else if (msg.sameKind("onReturn")) {
				onReturn();
			} else {
				return false;
			}
			msg.sendReply(true); // dummy
			return true;
		} catch (final Exception ex) {
			ex.printStackTrace();
			msg.sendException(ex);
		} catch (final ThreadDeath ex) {

			//
		} catch (final Throwable ex) {
			ex.printStackTrace();
			msg.sendException(new Exception(ex.getMessage()));
		}
		return false;
	}

	// Abstract methods
	/**
	 * This method should be overridden to specify initialization part for the
	 * job of the slave.
	 * 
	 * @exception AgletException
	 *                if fails to complete.
	 */
	abstract protected void initializeJob() throws Exception;

	/**
	 * Initialize the slave. It is called only the first time the slave is
	 * created. The initialization argument includes three elements: (1) the
	 * master aglet and (2) the Slave's itinerary, and (3) an argument for the
	 * local task.
	 * 
	 * @param object
	 *            the initialization argument
	 * @exception AgletException
	 *                if the initialization fails.
	 */
	@Override
	public synchronized void onCreation(final Object object) {
		final Arguments obj = (Arguments) object;

		master = (AgletID) (obj.getArg("master"));
		final Vector v = (Vector) (obj.getArg("itinerary"));

		ARGUMENT = obj.getArg("argument");
		RESULT = null;

		itin = new SlaveAgletItinerary(this, v);
		itin.startTrip();
	}

	// -- Reports an error to the master.
	//
	private void onError(final SlaveError error) {
		final String text = error.host + "::<" + error.text + ">";
		final Message msg = new Message("error", text);

		try {
			getMasterProxy(master, this).sendAsyncMessage(msg);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	// -- Reports the results to the master
	//
	private void onReturn() {
		final Message msg = new Message("result", RESULT);

		try {
			getMasterProxy(master, this).sendAsyncMessage(msg);
		} catch (final AgletException ex) {
			ex.printStackTrace();
		}
	}
}

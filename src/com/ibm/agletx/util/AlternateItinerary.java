package com.ibm.agletx.util;

/*
 * @(#)AlternateItinerary.java
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.RequestRefusedException;
import com.ibm.aglet.ServerNotFoundException;
import com.ibm.aglet.event.MobilityAdapter;
import com.ibm.aglet.event.MobilityEvent;
import com.ibm.aglet.message.Message;

/**
 * An itinerary class to dispatch an aglet to any one of multiple destination. <br>
 * The following is a typical usage of this case.
 * 
 * <pre>
 * AlternateItinerary itinerary = null;
 * 
 * public boolean handleMessage(Message msg) {
 *     if (msg.sameKind(&quot;test&quot;)) {
 * 	System.out.println(&quot;arrived!!!&quot;);
 * 	return true;
 *     }
 *     return false;
 * }
 * 
 * public void onCreation(Object ini) {
 *     itinerary = new AlternateItinerary(this);
 *     itinerary.addAlternate(&quot;atp://tsdsai.trl.ibm.com:4434&quot;);
 *     itinerary.addAlternate(&quot;atp://yariv.trl.ibm.com:4434&quot;);
 *     try {
 * 	itinerary.go(&quot;test&quot;);
 *     } catch (SecurityException ex) { // a RuntimeException
 * 	// failed to the aglet specific problems.
 *     } catch (IOException ex) {
 * 	// failed due to communication problems.
 *     }
 * }
 * </pre>
 * 
 * In the above code, the <tt>go()</tt> tries to dispatch the owner aglet to one
 * of the alternative destinations specified by the <tt>addAlternate()</tt>. If
 * succeeds, it sends <tt>Message("test")</tt> to the owner aglet upon its
 * arrival to that destination. If multiple destinations are available to host
 * the owner aglet, the selection is implementation-dependant. If fails, it
 * throws an <tt>IOException</tt> exception (in case of communication problems)
 * or <tt>SecurityException</tt> (otherwise).
 * 
 * @version 1.20 $Date: 2009/07/28 07:04:53 $
 * @author Yariv Aridor
 */

public class AlternateItinerary extends MobilityAdapter implements
java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6259222370792813718L;
	private final Aglet aglet;
	private final Vector hosts = new Vector();
	private Message next;

	public AlternateItinerary(final Aglet aglet) {
		this.aglet = aglet;
		aglet.addMobilityListener(this);
	}

	public void addAlternate(final String address) {
		hosts.addElement(address);
	}

	/**
	 * Go to one available destination
	 * 
	 * @exception IOException
	 *                if dispatch failed due to communication problems
	 * @exception AgletException
	 *                if dispatch failed due to aglet specific problems.
	 */
	public void go() throws java.io.IOException {
		this.go((Message) null);
	}

	/**
	 * Go to one available destination where the message is processed
	 * 
	 * @param msg
	 *            the message being sent to the aglet at the destination
	 * @exception IOException
	 *                if dispatch failed due to communication problems
	 * @exception AgletException
	 *                if dispatch failed due to aglet specific problems.
	 */
	public void go(final Message msg) throws java.io.IOException {
		next = msg;
		for (final Enumeration e = hosts.elements(); e.hasMoreElements();) {
			try {
				aglet.dispatch(new URL((String) e.nextElement()));
			} catch (final MalformedURLException ex) {
				continue;
			} catch (final ServerNotFoundException ex) {
				continue;
			} catch (final IOException ex) {
				continue;
			} catch (final RequestRefusedException ex) {
				continue;
			} catch (final SecurityException ex) { // can never be dispatched.
				throw ex;
			}
		}
		throw new ServerNotFoundException("dispatched failed for all alternative destinations");
	}

	/**
	 * Go to one available destination where the message is processed
	 * 
	 * @param msg
	 *            the message being sent to the aglet at the destination
	 * @exception IOException
	 *                if dispatch failed due to communication problems
	 * @exception AgletException
	 *                if dispatch failed due to aglet specific problems.
	 */
	public void go(final String msg) throws java.io.IOException {
		this.go(new Message(msg, null));
	}

	/**
	 * This is not normally used by aglets programmers.
	 * 
	 * @param ev
	 *            a mobility event
	 */
	@Override
	public void onArrival(final MobilityEvent ev) {
		try {
			if (next != null) {
				ev.getAgletProxy().sendAsyncMessage(next);
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	public void removeAlternate(final String address) {
		hosts.removeElement(address);
	}
}

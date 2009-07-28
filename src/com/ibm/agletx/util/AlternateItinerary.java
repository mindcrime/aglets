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

import com.ibm.aglet.*;
import com.ibm.aglet.event.*;
import com.ibm.aglet.message.Message;

import java.util.Vector;
import java.util.Enumeration;
import java.net.URL;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * An itinerary class to dispatch an aglet to any one of multiple destination.
 * <br>
 * The following is a typical usage of this case.
 * 
 * <pre>
 * AlternateItinerary  itinerary= null;
 * public boolean handleMessage(Message msg) {
 * if (msg.sameKind("test")) {
 * System.out.println("arrived!!!");
 * return true;
 * }
 * return false;
 * }
 * 
 * public void onCreation(Object ini) {
 * itinerary = new AlternateItinerary(this);
 * itinerary.addAlternate("atp://tsdsai.trl.ibm.com:4434");
 * itinerary.addAlternate("atp://yariv.trl.ibm.com:4434");
 * try {
 * itinerary.go("test");
 * } catch (SecurityException ex) {  // a RuntimeException
 * // failed to the aglet specific problems.
 * } catch (IOException ex) {
 * // failed due to communication problems.
 * }
 * }
 * </pre>
 * In the above code, the <tt>go()</tt> tries to dispatch the owner aglet
 * to one of the alternative destinations specified by the <tt>addAlternate()</tt>.
 * If succeeds, it sends <tt>Message("test")</tt> to the owner aglet upon its
 * arrival to that destination.
 * If multiple destinations are available to host the owner aglet, the selection is implementation-dependant.
 * If fails, it throws an <tt>IOException</tt> exception (in case of communication
 * problems) or <tt>SecurityException</tt> (otherwise).
 * 
 * @version     1.20    $Date: 2009/07/28 07:04:53 $
 * @author      Yariv Aridor
 */

public class AlternateItinerary extends MobilityAdapter 
	implements java.io.Serializable {


	private Aglet aglet;
	private Vector hosts = new Vector();
	private Message next;

	public AlternateItinerary(Aglet aglet) {
		this.aglet = aglet;
		aglet.addMobilityListener(this);
	}
	public void addAlternate(String address) {
		hosts.addElement(address);
	}
	/**
	 * Go to one available destination
	 * @exception IOException if dispatch failed due to communication problems
	 * @exception AgletException if dispatch failed due to aglet specific problems.
	 */
	public void go() throws java.io.IOException {
		go((Message)null);
	}
	/**
	 * Go to one available destination where the message is processed
	 * @param msg   the message being sent to the aglet at the destination
	 * @exception IOException if dispatch failed due to communication problems
	 * @exception AgletException if dispatch failed due to aglet specific problems.
	 */
	public void go(Message msg) throws java.io.IOException {
		next = msg;
		for (Enumeration e = hosts.elements(); e.hasMoreElements(); ) {
			try {
				aglet.dispatch(new URL((String)e.nextElement()));
			} catch (MalformedURLException ex) {
				continue;
			} catch (ServerNotFoundException ex) {
				continue;
			} catch (IOException ex) {
				continue;
			} catch (RequestRefusedException ex) {
				continue;
			} catch (SecurityException ex) {	// can never be dispatched.
				throw ex;
			} 
		} 
		throw new ServerNotFoundException("dispatched failed for all alternative destinations");
	}
	/**
	 * Go to one available destination where the message is processed
	 * @param msg  the message being sent to the aglet at the destination
	 * @exception IOException if dispatch failed due to communication problems
	 * @exception AgletException if dispatch failed due to aglet specific problems.
	 */
	public void go(String msg) throws java.io.IOException {
		go(new Message(msg, null));
	}
	/**
	 * This is not normally used by aglets programmers.
	 * @param ev a mobility event
	 */
	public void onArrival(MobilityEvent ev) {
		try {
			if (next != null) {
				ev.getAgletProxy().sendAsyncMessage(next);
			} 
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	public void removeAlternate(String address) {
		hosts.removeElement(address);
	}
}

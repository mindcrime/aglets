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
    private Aglet aglet;
    private Vector hosts = new Vector();
    private Message next;

    public AlternateItinerary(Aglet aglet) {
	this.aglet = aglet;
	aglet.addMobilityListener(this);
    }

    public void addAlternate(String address) {
	this.hosts.addElement(address);
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
    public void go(Message msg) throws java.io.IOException {
	this.next = msg;
	for (Enumeration e = this.hosts.elements(); e.hasMoreElements();) {
	    try {
		this.aglet.dispatch(new URL((String) e.nextElement()));
	    } catch (MalformedURLException ex) {
		continue;
	    } catch (ServerNotFoundException ex) {
		continue;
	    } catch (IOException ex) {
		continue;
	    } catch (RequestRefusedException ex) {
		continue;
	    } catch (SecurityException ex) { // can never be dispatched.
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
    public void go(String msg) throws java.io.IOException {
	this.go(new Message(msg, null));
    }

    /**
     * This is not normally used by aglets programmers.
     * 
     * @param ev
     *            a mobility event
     */
    @Override
    public void onArrival(MobilityEvent ev) {
	try {
	    if (this.next != null) {
		ev.getAgletProxy().sendAsyncMessage(this.next);
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public void removeAlternate(String address) {
	this.hosts.removeElement(address);
    }
}

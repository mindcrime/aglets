package com.ibm.agletx.util;

/*
 * @(#)SimpleItinerary.java
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

import java.util.Hashtable;
import java.util.Enumeration;
import java.net.URL;
import java.io.IOException;

/**
 * SimpleItinerary class is an itinerary object which can specify a
 * destination and a message which will be sent to the owner aglet
 * when it arrived at the destination.
 * 
 * <pre>
 * SimpleItinerary itinerary;
 * public void onCreation(Object init) {
 * itinerary = new SimpleItinerary(this);
 * itinerary.go("atp://first", "job1");
 * }
 * 
 * public boolean handleMessage(Message msg) {
 * if (msg.sameKind("job1")) {
 * // job at the first place
 * itinerary.go("atp://second", "job2");
 * } else if (msg.sameKind("job2")) {
 * // job at the second place
 * itinerary.go("atp://third", "job3");
 * } else if (msg.sameKind("job3")) {
 * // job at the third place
 * dispose();
 * } else return false;
 * return true;
 * }
 * </pre>
 * 
 * In above case, what the call <tt> itinerary.go("atp://second", "job2") </tt>
 * does is
 * <dt>
 * <dd> dispatches the owner aglet (specified in the argument of constructor)
 * to <tt> "atp://second" </tt>.
 * <dd> sends <tt> new Message("job2") </tt> message to the owner aglet
 * when arrived at the destination
 * </dt>
 * In this way, an aglet can specify both a destination
 * to go and a message to be handled at the destination.
 * 
 * @version     1.00    $Date: 2001/07/28 06:33:39 $
 * @author      Mitsuru Oshima
 */

// public class SimpleItinerary extends MobilityAdapter implements java.io.Serializable {
public class SimpleItinerary extends MobilityAdapter 
	implements java.io.Externalizable {

	static final long serialVersionUID = -5696240265720297145L;

	private Aglet aglet;
	private Message next;
	private Hashtable plan = new Hashtable();

	/**
	 * For Manual Serialization
	 */
	public SimpleItinerary() {}
	/**
	 * Constructs a SimpleItinerary with the specified owner aglet.
	 * @param aglet the owner of this itinerary.
	 */
	public SimpleItinerary(Aglet aglet) {
		this.aglet = aglet;
		aglet.addMobilityListener(this);
	}
	/**
	 * Goes to the destination given by name and the message processed
	 * at the destination.
	 * @param address the address of the destination
	 * @param msg     the message being sent to the aglet at the destination
	 * @exception IOException if dispatch completely failed
	 * @exception AgletException if dispatch completely failed
	 */
	public void go(String address, 
				   Message msg) throws java.io.IOException, AgletException {
		next = msg;
		aglet.dispatch(new URL(address));
	}
	/**
	 * Goes to the destination given by name and the message processed
	 * at the destination.
	 * @param address the address of the destination
	 * @param msg     the message being sent to the aglet at the destination
	 * @exception IOException if dispatch completely failed
	 * @exception AgletException if dispatch completely failed
	 */
	public void go(String address, 
				   String msg) throws java.io.IOException, AgletException {
		go(address, new Message(msg));
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
	public void readExternal(java.io.ObjectInput in) 
			throws java.io.IOException, ClassNotFoundException {
		aglet = (Aglet)in.readObject();
		next = (Message)in.readObject();
		plan = (Hashtable)in.readObject();
	}
	public void writeExternal(java.io.ObjectOutput oo) 
			throws java.io.IOException {
		oo.writeObject(aglet);
		oo.writeObject(next);
		oo.writeObject(plan);
	}
}

package com.ibm.agletx.util;

/*
 * @(#)MessengerItinerary.java
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

/**
 * An Itinerary class to broadcast a message to remote aglets. The message
 * is carried by a messenger aglet which visits the hosts
 * of the receiver aglets to send them the message, locally.
 * <br>
 * The following code segment shows a typical usage of this class.
 * <pre>
 * public MessengerAglet extends Aglet {
 * MessengerItinerary itinerary;
 * Message message = new Message("test",null); // the message to broadcast
 * String receivers[] = ....  // URIs of receiver aglets.
 * 
 * class MessengerItineraryx extends MessengerItinerary {
 * AgletProxy sender = null;
 * public MessengerItineraryx (Aglet aglet, Message msg, AgletProxy sender) {
 * super(aglet,msg);
 * this.sender=sender;
 * }
 * 
 * public void handleException (Throwable th) {
 * try {
 * sender.sendMessage(new Message("ack",th));
 * } catch (Exception ex) {
 * ex.printStackTrace();
 * }
 * th.printStackTrace();
 * }
 * }
 * public void onCreation(Object ini) {
 * AgletProxy sender = (AgletProxy)ini;
 * itinerary=new MessengerItineraryx(this,message,sender);
 * for (<<tt>every receiver in receivers[]</tt>>) {
 * itinerary.addAglet(<<tt>location of a receiver</tt>>,<<tt>ID of a receiver</tt>>);
 * }
 * itinerary.startTrip();
 * }
 * }
 * </pre>
 * 
 * The above code defines the messenger aglet. Unlike one-way messaging, the
 * messenger aglet notifies its owner aglet (i.e. the creator aglet) in case
 * any of the receiver aglets can not be located.
 * In the code, the message to broadcast and the receiver aglets are saved
 * in the <tt>message</tt> and <tt>receivers</tt> instance variables,
 * respectivally. The <tt>MessangerItineraryx</tt> subclass overrides the
 * <tt>handleException()</tt> to notifies the creator aglet (via the message
 * <tt>Message("ack",ex)</tt>) of any exceptions encountered by the messenger
 * aglet. The invocation of <tt>itinerary.startTrip()</tt> causes the
 * messenger aglet to start its trip among the hosts of the receiver aglets.
 * 
 * @version     1.20    $Date: 2009/07/28 07:04:53 $
 * @author      Yariv Aridor
 */

public class MessengerItinerary extends SeqItinerary {

	private Message msg = null;

	/**
	 * Construct a MessengerItinerary object with a specified owner aglet
	 * The message should be explicitly set by the <tt>setMessage()</tt>
	 * @param aglet the owner aglet
	 */
	public MessengerItinerary(Aglet aglet) {
		this(aglet, (Message)null);
	}
	/**
	 * Construct a MessengerItinerary object with the specified owner aglet
	 * and a message to broadcast.
	 * @param aglet the owner aglet
	 * @param msg the message to be broadcasted
	 */
	public MessengerItinerary(Aglet aglet, Message msg) {
		super(aglet);
		this.msg = msg;
	}
	/**
	 * Construct a MessengerItinerary object with the specified owner aglet
	 * and a message to broadcast.
	 * @param aglet the owner aglet
	 * @param msg the message to br broadcasted
	 */
	public MessengerItinerary(Aglet aglet, String msg) {
		this(aglet, new Message(msg));
	}
	/**
	 * Add a new item [address, aglet id] to the itinerary.
	 * @param address the address of the aglet.
	 * @param id the id of the aglet.
	 */
	public synchronized void addAglet(String address, AgletID id) {
		addTask(address, new MessengerTask(id));
	}
	/**
	 * Return the URI of the aglet at the specified index. The URI is
	 * represented as <address> + '#' + <aglet id>.
	 * @param index the specified index.
	 */
	public synchronized URL getAgletAt(int index) throws IOException {
		return new URL(getAddressAt(index) + "/#" 
					   + ((MessengerTask)getTaskAt(index)).getAgletID());
	}
	/**
	 * Return the enumeration of all the aglets to receive the broadcast
	 * message
	 */
	public Enumeration getAglets() {
		Vector v = new Vector();

		for (int i = 0; i < size(); i++) {
			AgletID id = ((MessengerTask)getTaskAt(i)).getAgletID();

			v.addElement(id);
		} 
		return v.elements();
	}
	/**
	 * Return the message to be broadcast.
	 * @param index the index to remove.
	 */
	Message getMessage() {
		return msg;
	}
	/**
	 * Remove an aglet from the plan at a specific index.
	 * @param index the index to remove.
	 */
	public synchronized void removeAglet(int index) {
		removeTaskAt(index);
	}
	/**
	 * Set the message to be broadcast
	 * @param msg the message
	 */
	public void setMessage(Message msg) {
		this.msg = msg;
	}
	/**
	 * Set the message to be broadcast
	 * @param msg the message
	 */
	public void setMessage(String msg) {
		this.msg = new Message(msg, null);
	}
}

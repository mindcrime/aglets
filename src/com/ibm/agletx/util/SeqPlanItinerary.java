package com.ibm.agletx.util;

/*
 * @(#)SeqPlanItinerary.java
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

import java.util.Vector;
import java.util.Enumeration;
import java.net.URL;

/**
 * <tt> SeqPlanItinerary </tt> defines the trip plan which has a sequence of
 * place-message pair. For example, the plan defined like:
 * <pre>
 * itinerary.addPlan("atp://first", "job1");
 * itinerary.addPlan("atp://second", "job2");
 * </pre>
 * first dispatches an aglet to the <tt> "atp://first" </tt> and
 * sends <tt> new Message("job1") </tt> message to the aglet. After
 * that message handling is completed, the itinerary dispatches the aglet
 * to the next address, <tt> "atp://second" </tt>, and send the
 * corresponding message <tt> new Message("job2", init) </tt>
 * to the aglet again.
 * The order of plan is defined in the order <tt>addPlan</tt> method is called,
 * or you can insert a new plan item at the specified index by calling <tt>
 * insertPlanAt </tt>. To automatically dispatches an aglet to the next
 * address when the job is completed, the aglet have to have the following
 * block
 * 
 * @version     1.10    $Date: 2001/07/28 06:33:40 $
 * @author      Mitsuru Oshima
 * @author      Yariv Aridor
 */
public class SeqPlanItinerary extends SeqItinerary {

	static final long serialVersionUID = 7979344708988677116L;

	/**
	 * Constructs a SeqPlanItinerary object with the specified owner aglet.
	 * @param aglet the owner aglet
	 */
	public SeqPlanItinerary(Aglet aglet) {
		super(aglet);
	}
	/**
	 * Adds the new itinerary item of the form [address, message].
	 * @param address  the address to go
	 * @param msg      the message to be sent to the owner aglet
	 */
	public void addPlan(String address, Message msg) {
		addTask(address, new SeqPlanTask(msg));
	}
	/**
	 * Adds the new itinerary item of the form [address, message].
	 * This is added at the end of plan.
	 * @param address  the address to go
	 * @param msg      the message to be sent to the owner aglet
	 */
	public void addPlan(String address, String msg) {
		addPlan(address, new Message(msg));
	}
	public Message getMessageAt(int index) {
		return ((SeqPlanTask)getTaskAt(index)).getMessage();
	}
	public void onArrival(MobilityEvent ev) {
		try {
			Task task = getCurrentTask();

			incIndex();		// should be improved
			task.execute(this);
		} catch (Exception ex) {
			handleException(ex);
		} 
	}
	public void removePlanAt(int index) {
		removeTaskAt(index);
	}
}

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

import com.ibm.aglet.Aglet;
import com.ibm.aglet.event.MobilityEvent;
import com.ibm.aglet.message.Message;

/**
 * <tt> SeqPlanItinerary </tt> defines the trip plan which has a sequence of
 * place-message pair. For example, the plan defined like:
 * 
 * <pre>
 * itinerary.addPlan(&quot;atp://first&quot;, &quot;job1&quot;);
 * itinerary.addPlan(&quot;atp://second&quot;, &quot;job2&quot;);
 * </pre>
 * 
 * first dispatches an aglet to the <tt> "atp://first" </tt> and sends
 * <tt> new Message("job1") </tt> message to the aglet. After that message
 * handling is completed, the itinerary dispatches the aglet to the next
 * address, <tt> "atp://second" </tt>, and send the corresponding message
 * <tt> new Message("job2", init) </tt> to the aglet again. The order of plan is
 * defined in the order <tt>addPlan</tt> method is called, or you can insert a
 * new plan item at the specified index by calling <tt>
 * insertPlanAt </tt>. To automatically dispatches an aglet to the next address
 * when the job is completed, the aglet have to have the following block
 * 
 * @version 1.10 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 * @author Yariv Aridor
 */
public class SeqPlanItinerary extends SeqItinerary {

	static final long serialVersionUID = 7979344708988677116L;

	/**
	 * Constructs a SeqPlanItinerary object with the specified owner aglet.
	 * 
	 * @param aglet
	 *            the owner aglet
	 */
	public SeqPlanItinerary(final Aglet aglet) {
		super(aglet);
	}

	/**
	 * Adds the new itinerary item of the form [address, message].
	 * 
	 * @param address
	 *            the address to go
	 * @param msg
	 *            the message to be sent to the owner aglet
	 */
	public void addPlan(final String address, final Message msg) {
		addTask(address, new SeqPlanTask(msg));
	}

	/**
	 * Adds the new itinerary item of the form [address, message]. This is added
	 * at the end of plan.
	 * 
	 * @param address
	 *            the address to go
	 * @param msg
	 *            the message to be sent to the owner aglet
	 */
	public void addPlan(final String address, final String msg) {
		this.addPlan(address, new Message(msg));
	}

	public Message getMessageAt(final int index) {
		return ((SeqPlanTask) getTaskAt(index)).getMessage();
	}

	@Override
	public void onArrival(final MobilityEvent ev) {
		try {
			final Task task = getCurrentTask();

			incIndex(); // should be improved
			task.execute(this);
		} catch (final Exception ex) {
			handleException(ex);
		}
	}

	public void removePlanAt(final int index) {
		removeTaskAt(index);
	}
}

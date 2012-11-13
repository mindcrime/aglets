package com.ibm.agletx.util;

/*
 * @(#)SlaveItinerary.java
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

import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import com.ibm.aglet.Aglet;

/**
 * An Itinerary class to repeatedly perform a specific task in multiple
 * destinations. <br>
 * Here is a typical usage of this class.
 * 
 * <pre>
 * private boolean lastTask = false;
 * private SlaveItinerary itinerary = null;
 * 
 * class TaskA extends Task {
 *     public void execute(SeqItinerary itin) throws Exception {
 * 	// do some work
 *     }
 * }
 * 
 * class TaskB extends Task {
 *     public void execute(SeqItinerary itin) throws Exception {
 * 	// do some work
 *     }
 * }
 * 
 * public void onCreation(Object ini) {
 *     itinerary = new SlaveItinerary(this, &quot;atp://yariv.trl.ibm.com&quot;, new TaskA());
 *     itinerary.addPlace(&quot;atp://tai.trl.ibm.com&quot;);
 *     itinerary.startTrip();
 * }
 * 
 * public void run() {
 *     if (itinerary.atLastDestination() == true) {
 * 	if (lastTask == true) {
 * 	    // completed all tasks.
 * 	} else {
 * 	    lastTask = true;
 * 	    itinerary.setTask(new TaskB());
 * 	    itinerary.startTrip();
 * 	}
 *     }
 * }
 * </pre>
 * 
 * The above code defines an aglet which performs 2-phase computation: in the
 * first phase, the aglet travels among multiple destinations to perform a task
 * defined by TaskA. Then, the second phase is started, in which the aglet
 * visits again all these destinations to perform a task of TaskB.
 * <p>
 * In the above code, the invocation of <tt>itinerary.startTrip()</tt> causes
 * the owner aglet to be dispatched sequentially among the destinations. In
 * every destination, the <tt>execute()</tt> of the corresponding <tt>Task</tt>
 * object (assigned to the <tt>SlaveItinerary</tt> object via <tt>setTask()</tt>
 * ) is automatically invoked.
 * 
 * @version 1.20 $Date: 2009/07/28 07:04:53 $
 * @author Yariv Aridor
 */

public class SlaveItinerary extends SeqItinerary {

	/**
	 * 
	 */
	private static final long serialVersionUID = -528297595968340623L;
	private Task task = null;

	/**
	 * Constructor.
	 * 
	 * @param aglet
	 *            the owner aglet
	 * @param task
	 *            the task to preform
	 * @param address
	 *            an address where the task should be preformed
	 */
	public SlaveItinerary(final Aglet aglet, final String address, final Task task) {
		super(aglet);
		this.task = task;
		addPlan(address);
	}

	/**
	 * Constructor.
	 * 
	 * @param aglet
	 *            the owner aglet
	 * @param task
	 *            the task to preform
	 * @param addresses
	 *            a vector of address where the task should be performed.
	 */
	public SlaveItinerary(final Aglet aglet, final Vector addresses, final Task task) {
		super(aglet);
		this.task = task;
		for (final Enumeration e = addresses.elements(); e.hasMoreElements();) {
			addPlan(((URL) e.nextElement()).toString());
		}
	}

	/**
	 * Add a new address to the itinerary of the owner aglet
	 */
	public void addPlan(final String address) {
		addTask(address, task);
	}

	/**
	 * Return the current task to be preformed by the owner aglet
	 */
	public Task getTask() {
		return task;
	}

	/**
	 * Set the task to be preformed by the owner aglet
	 */
	public void setTask(final Task task) {
		this.task = task;
		final Enumeration e = addresses();

		clear();
		for (; e.hasMoreElements();) {
			final String address = (String) e.nextElement();

			addPlan(address);
		}
	}
}

package com.ibm.agletx.util;

/*
 * @(#)MeetingsItinerary.java
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
import com.ibm.agletx.patterns.Meeting;

/**
 * An Itinerary class to control an itinerary to participate in multiple
 * meetings. <br>
 * The following is an typical usage of this class.
 * 
 * <pre>
 * MeetingsItinerary itinerary = null;
 * 
 * class MeetingTaskA extends MeetingTask {
 *     public MeetingTaskA(Meeting m) {
 * 	super(m);
 *     }
 * 
 *     public void execute(SeqItinerary itin, Enumeration e) throws Exception {
 * 	// do some work during the meeting.
 *     }
 * }
 * 
 * class MeetingTaskB extends MeetingTask {
 *     public MeetingTaskB(Meeting m) {
 * 	super(m);
 * 
 *     }
 * 
 *     public void execute(SeqItinerary itin, Enumeration e) throws Exception {
 * 	// do some work during the meeting
 *     }
 * }
 * 
 * public boolean handleMessage(Message msg) {
 *     if (msg.sameKind(itinerary.getCurrentMeeting().getID())) {
 * 	// get notified of any new aglets arrived to the current meeting.
 * 	return true;
 *     }
 *     return false;
 * }
 * 
 * public void onCreation(Object ini) {
 *     itinerary = new MeetingsItinerary(this);
 *     itinerary.addMeetingTask(new MeetingTaskA(new Meeting(&quot;atp://yariv.trl.ibm.com:4434&quot;)));
 *     itinerary.addMeetingTask(new MeetingTaskB(new Meeting(&quot;atp://tai.trl.ibm.com:4434&quot;)));
 *     itinerary.startTrip();
 * }
 * </pre>
 * 
 * In the above code, The <tt>MeetingTaskA</tt> and <tt>MeetingTaskB</tt>
 * defines the specific tasks to be performed in every meeting. The
 * <tt>itinerary.startTrip()</tt> causes the owner aglet to be dispatched
 * sequentially among the meeting places. Upon arrival to a meeting place, the
 * corresponding task (defined by the <tt>MeetingTask</tt> objects) is
 * automatically executed.
 * 
 * @version 1.20 $Date: 2009/07/28 07:04:53 $
 * @author Yariv Aridor
 * @see com.ibm.agletx.patterns.Meeting
 */

public class MeetingsItinerary extends SeqItinerary {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6503791120022552099L;

	/**
	 * Constructs a MeetingsItinerary object with the specified owner aglet.
	 * 
	 * @param aglet
	 *            the owner aglet
	 */
	public MeetingsItinerary(final Aglet aglet) {
		super(aglet);
	}

	/**
	 * Add the new plan item (meetingTask object)
	 * 
	 * @param task
	 *            a task containing also the meeting at which to be carried out 
	 */
	public void addMeetingTask(final MeetingTask task) {
		addTask(task.getMeeting().getPlace(), task);
	}

	/**
	 * Return the current Meeting object
	 */
	public Meeting getCurrentMeeting() {
		return ((MeetingTask) getCurrentTask()).getMeeting();
	}

	/**
	 * Return the meeting object at the specified index.
	 */
	public Meeting getMeetingAt(final int index) {
		return ((MeetingTask) getTaskAt(index)).getMeeting();
	}
}

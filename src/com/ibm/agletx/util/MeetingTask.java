package com.ibm.agletx.util;

/*
 * @(#)MeetingTask.java
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

import com.ibm.agletx.patterns.Meeting;

/**
 * This class objectifies a task to be perform upon arrival to a meeting.
 * 
 * @version 1.20 $Date: 2009/07/28 07:04:53 $
 * @author Yariv Aridor
 * @see MeetingsItinerary
 */

public abstract class MeetingTask extends Task {

    /**
     * 
     */
    private static final long serialVersionUID = 6406621214633110803L;
    private Meeting meeting = null;

    public MeetingTask(Meeting meeting) {
	this.meeting = meeting;
    }

    /**
     * Aglet programers should not modify this method
     */
    @Override
    public void execute(SeqItinerary itin) throws Exception {
	this.execute(itin, this.meeting.ready(itin.getOwnerAglet().getAglet()));
    }

    /**
     * Define the task to be performed upon arrival to a meeting
     * 
     * @param itin
     *            the MeetingsItinerary object
     * @param participants
     *            enumeration of the current participants in the meeting.
     * @exception Exception
     *                if failed to perform the task
     */
    public abstract void execute(
                                 SeqItinerary itin,
                                 java.util.Enumeration participants)
    throws Exception;

    public Meeting getMeeting() {
	return this.meeting;
    }
}

package com.ibm.aglet.event;

/*
 * @(#)MobilityEvent.java
 * 
 * (c) Copyright IBM Corp. 1997, 1998
 * 
 * IBM grants you a non-exclusive, non-transferrable License to
 * use this program internally solely for the purposes of testing
 * and evaluating Java Aglet API.
 * You may not distribute, sublicense, lease, rent or use this
 * sample program externally.
 * 
 * THIS ROGRAM IS PROVIDED "AS IS" WITHOUT ANY WARRANTY EXPRESS OR
 * IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE WARRANTY OF
 * NON-INFRINGEMENT AND THE WARRANTIES OF MERCHANTIBILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 * IBM WILL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY YOU AS
 * A RESULT OF USING THIS SAMPLE PROGRAM. IN NO EVENT WILL IBM BE
 * LIABLE FOR ANY SPECIAL, INDIRECT CONSEQUENTIAL DAMAGES OR LOST
 * PROFITS EVEN IF IBM HAS BEEN ADVISED OF THE POSSIBILITY OF THEIR
 * OCCURRENCE OR LOSS OF OR DAMAGE TO YOUR RECORDS OR DATA.
 * IBM WILL NOT BE LIABLE FOR ANY THIRD PARTY CLAIMS AGAINST YOU.
 */

import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.Ticket;
import java.net.URL;

/**
 * The mobility event occurs when the aglet is about to move.
 * 
 * @version     1.00    $Date: 2001/07/28 06:34:12 $
 * @author      Mitsuru Oshima
 */
public class MobilityEvent extends AgletEvent {

	/**
	 * Marks the first integer id for the range of mobility event ids.
	 */
	public static final int AGLET_MOBILITY_FIRST = 1200;

	/**
	 * Marks the last integer id for the range of mobility event ids.
	 */
	public static final int AGLET_MOBILITY_LAST = 1202;

	/**
	 * The DISPATCHING event type is delivered just after the dispatch methods
	 * is called.
	 */
	public static final int DISPATCHING = AGLET_MOBILITY_FIRST;

	/**
	 * The REVERTING event type is delivered when the retaction is requested
	 * from the remote site.
	 */
	public static final int REVERTING = AGLET_MOBILITY_FIRST + 1;

	/**
	 * The ARRIVAL event type is delivered when the aglet arrived at the
	 * destination.
	 */
	public static final int ARRIVAL = AGLET_MOBILITY_FIRST + 2;

	private static String name[] = {
		"DISPATCHING", "REVERTING", "ARRIVAL", 
	};

	private Ticket ticket;

	/**
	 * Constructs a mobility event with specified id, target and ticket
	 */
	public MobilityEvent(int id, AgletProxy target, Ticket tick) {
		super(target, id);
		ticket = tick;
	}
	/**
	 * Constructs a mobility event with specified id, target and location
	 */
	public MobilityEvent(int id, AgletProxy target, URL loc) {
		super(target, id);
		ticket = new Ticket(loc);
	}
	/**
	 * Returns the aglet proxy which is the source of the event.
	 */
	public AgletProxy getAgletProxy() {
		return (AgletProxy)source;
	}
	/**
	 * Gets the location. This specifies:
	 * The destination if the event is <tt> DISPATCHING </tt>.
	 * The requester if the event is <tt> REVERTING </tt>.
	 * The host it arrived if the event is <tt> ARRIVED </tt>. This returns
	 * null for the REVERTING event at the present.
	 */
	public URL getLocation() {
		return ticket.getDestination();
	}
	/**
	 * Gets the ticket.
	 */
	public Ticket getTicket() {
		return ticket;
	}
	public String toString() {
		return "MobilityEvent[" + name[id - AGLET_MOBILITY_FIRST] + "]";
	}
}

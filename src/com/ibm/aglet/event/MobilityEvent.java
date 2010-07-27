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

import java.net.URL;

import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.Ticket;

/**
 * The mobility event occurs when the aglet is about to move.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 */
public class MobilityEvent extends AgletEvent {

    
    /**
     * The ticket for the mobility.
     */
    private Ticket ticket;

    /**
     * Constructs a mobility event with the next available id, the specified proxy as target, the ticket and
     * the even type.
     */
    public MobilityEvent(AgletProxy target, Ticket tick, EventType type) {
	super(target, AgletEvent.nextID(), type);
	this.ticket = tick;
	
    }

    /**
     * Constructs a mobility event with specified id, target and location
     */
    public MobilityEvent(AgletProxy target, URL loc, EventType type) {
	super(target, AgletEvent.nextID(), type);
	this.ticket = new Ticket(loc);
    }

    /**
     * Returns the aglet proxy which is the source of the event.
     */
    public AgletProxy getAgletProxy() {
	return (AgletProxy) this.source;
    }

    /**
     * Gets the location. This specifies: The destination if the event is
     * <tt> DISPATCHING </tt>. The requester if the event is
     * <tt> REVERTING </tt>. The host it arrived if the event is
     * <tt> ARRIVED </tt>. This returns null for the REVERTING event at the
     * present.
     */
    public URL getLocation() {
	return this.ticket.getDestination();
    }

    /**
     * Gets the ticket.
     */
    public Ticket getTicket() {
	return this.ticket;
    }

    @Override
    public String toString() {
	return "MobilityEvent[" + this.getEventType().toString() + "]";
    }
}

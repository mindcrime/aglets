package com.ibm.aglet.event;

/*
 * @(#)AgletEvent.java
 * 
 * (c) Copyright IBM Corp. 1996, 1997, 1998
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


/**
 * The top level event of all aglet events.
 * 
 * @version 1.70 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 */
abstract public class AgletEvent extends java.util.EventObject {
    /**
     * Event id
     */
    private int id;
    
    
    /**
     * A sequence to take count of the events.
     */
    private static int idSequence = 0;
    
    /**
     * Provides the next id available for an event.
     * @return the next id available
     */
    public static final synchronized int nextID(){
	return ++idSequence;
    }
    
    
    /**
     * The event type, if the event is of a known type.
     */
    private EventType eventType = null;
    

    /**
     * Constructs an AgletEvent with source and id.
     */
    public AgletEvent(Object source, int id) {
	super(source);
	this.id = id;
    }
    
    /**
     * Builds an event of a specified type.
     * @param source the object that is sending the event
     * @param id the id of the event
     * @param type the type of the event, in the case it is a well known event
     */
    public AgletEvent(Object source, int id, EventType type ){
	this( source, id );
	this.eventType = type;
    }
    

    
    
    /**
     * Gets back the eventType.
     * @return the eventType
     */
    public synchronized final EventType getEventType() {
        return this.eventType;
    }

    /**
     * Gets the id of this event
     */
    public int getID() {
	return this.id;
    }
}

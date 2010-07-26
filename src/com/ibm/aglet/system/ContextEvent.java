package com.ibm.aglet.system;

/*
 * @(#)ContextEvent.java
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

import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.event.AgletEvent;

/**
 * Context level event
 * 
 * @version 1.50 $Date: 2009/07/28 07:04:54 $
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 */
public class ContextEvent extends AgletEvent {

    /**
     * Marks the first integer id for the range of context event ids.
     */
    public static final int CONTEXT_FIRST = 1000;

    /**
     * Marks the last integer id for the range of context event ids.
     */
    public static final int CONTEXT_LAST = 1012;

    /**
     * The STARTED event type is delivered when the aglet is started.
     * 
     * @see aglet.AgletContext#start
     */
    public static final int STARTED = CONTEXT_FIRST; // 1000

    /**
     * The STARTED event type is delivered when the context is being shutting
     * down.
     * 
     * @see aglet.AgletContext#shutdown
     */
    public static final int SHUTDOWN = CONTEXT_FIRST + 1; // 1001

    /**
     * The CREATED event type is delivered when an aglet is created.
     * 
     * @see aglet.AgletContext#createAglet
     */
    public static final int CREATED = CONTEXT_FIRST + 2; // 1002

    /**
     * The CLONED event type is delivered when an aglet is cloned.
     * 
     * @see aglet.Aglet#clone
     */
    public static final int CLONED = CONTEXT_FIRST + 3; // 1003

    /**
     * The DISPOSED event type is delivered when an aglet is disposed.
     * 
     * @see aglet.Aglet#dispose
     */
    public static final int DISPOSED = CONTEXT_FIRST + 4; // 1004

    /**
     * The DISPATCHED event type is delivered when an aglet is dispatched.
     * 
     * @see aglet.Aglet#dispatch
     */
    public static final int DISPATCHED = CONTEXT_FIRST + 5; // 1005

    /**
     * The DISPATCHED event type is delivered when an aglet is retracted.
     * 
     * @see aglet.AgletContext#retractAglet
     */
    public static final int REVERTED = CONTEXT_FIRST + 6; // 1006

    /**
     * The ARRIVED event type is delivered when an aglet is arrived at the
     * context.
     */
    public static final int ARRIVED = CONTEXT_FIRST + 7; // 1007

    /**
     * The DEACTIVATED event type is delivered when an aglet is deactivated
     * 
     * @see aglet.Aglet#deactivate
     */
    public static final int DEACTIVATED = CONTEXT_FIRST + 8; // 1008

    /**
     * The SUSPENDED event type is delivered when an aglet is suspended
     * 
     * @see aglet.Aglet#deactivate
     */
    public static final int SUSPENDED = CONTEXT_FIRST + 9; // 1009

    /**
     * The ACTIVATED event type is delivered when an aglet is activated.
     * 
     * @see aglet.Aglet#activate
     */
    public static final int ACTIVATED = CONTEXT_FIRST + 10; // 1010

    /**
     * The RESUMED event type is delivered when an aglet is resumed.
     * 
     * @see aglet.Aglet#resume
     */
    public static final int RESUMED = CONTEXT_FIRST + 11; // 1011

    /**
     * The STATE_CHANGED event type is delivered when the state of an aglet has
     * been changed.
     */
    public static final int STATE_CHANGED = CONTEXT_FIRST + 12; // 1012

    /**
     * The SHOW_DOCUMENT event type is delivered when an aglet requests to show
     * an document specified by the URL.
     * 
     * @see aglet.AgletContext#showDocument
     */
    public static final int SHOW_DOCUMENT = CONTEXT_FIRST + 13; // 1013

    /**
     * The MESSAGE event type is delivered when an context tries to show an
     * message.
     */
    public static final int MESSAGE = CONTEXT_FIRST + 14; // 1014

    /**
     * Not used.
     */
    public static final int NO_RESPONSE = CONTEXT_FIRST + 15; // 1015

    /**
     * AgletProxy proxy
     */
    protected AgletProxy agletproxy;

    /**
     * Arbitary arguments
     */
    public Object arg = null;

    private static String name[] = { "STARTED", "STOPPED", "CREATED", "CLONED",
	    "DISPOSED", "DISPATCHED", "REVERTED", "ARRIVED", "DEACTIVATED",
	    "SUSPENDED", "ACTIVATED", "RESUMED", "TEXT_CHANGED",
	    "SHOW_DOCUMENT", "MESSAGE", "NO_RESPONSE", };

    /**
     * Constructs an ContextEvent with id.
     */
    public ContextEvent(int id, Object context, AgletProxy target) {
	super(context, id);
	this.agletproxy = target;
    }

    /**
     * Constructs an ContextEvent with id.
     */
    public ContextEvent(int id, Object context, AgletProxy target, Object arg) {
	super(context, id);
	this.agletproxy = target;
	this.arg = arg;
    }

    /**
     * Gets AgletContext object of this event
     */
    public AgletContext getAgletContext() {
	return (AgletContext) this.source;
    }

    /**
     * Gets AgletProxy object of this event null if the event is STARTED or
     * STOPPED
     */
    public AgletProxy getAgletProxy() {
	return this.agletproxy;
    }

    /**
     * Gets the document URL.
     */
    public URL getDocumentURL() {
	if (this.id == SHOW_DOCUMENT) {
	    return (URL) this.arg;
	} else {
	    throw new IllegalAccessError("Event is not SHOW_DOCUMENT");
	}
    }

    /**
     * Gets the message to show
     */
    public String getMessage() {
	if (this.id == MESSAGE) {
	    return (String) this.arg;
	} else {
	    throw new IllegalAccessError("Event is not MESSAGE: "
		    + name[this.id - 1000]);
	}
    }

    /**
	 * 
	 */
    public String getText() {
	if (this.id == STATE_CHANGED) {
	    return (String) this.arg;
	} else {
	    throw new IllegalAccessError("Event is not STATE_CHANGED");
	}
    }

    @Override
    public String toString() {
	return "ContextEvent[" + name[this.id - 1000] + "]";
    }
}

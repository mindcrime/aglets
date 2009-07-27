package com.ibm.aglet.event;

/*
 * @(#)MobilityListener.java
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

/**
 * The listener interface for receiving mobility events on an aglet.
 * 
 * @version     1.00    $Date: 2009/07/27 10:31:41 $
 * @author      Mitsuru Oshima
 */
public interface MobilityListener extends java.util.EventListener, 
										  java.io.Serializable {

	/**
	 * Invoked just after the aglet arrived at the destination
	 */
	public void onArrival(MobilityEvent event);
	/**
	 * Invoked when the aglet is attempted to dispatch.
	 */
	public void onDispatching(MobilityEvent event);
	/**
	 * Invoked when the aglet is retracted.
	 */
	public void onReverting(MobilityEvent event);
}

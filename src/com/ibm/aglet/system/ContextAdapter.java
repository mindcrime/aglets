package com.ibm.aglet.system;

/*
 * @(#)ContextAdapter.java
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
 * The adapter which receives aglet context events. This class is provided
 * as convenience for easily creating listerns by extending this class
 * and overriding only the methods of interest.
 * 
 * @version     1.00    $Date: 2009/07/27 10:31:42 $
 * @author      Misturu Oshima
 */
public class ContextAdapter implements ContextListener {
	public void agletActivated(ContextEvent ev) {
		agletAdded(ev);
	}
	/**
	 */
	public void agletAdded(ContextEvent ev) {}
	public void agletArrived(ContextEvent ev) {
		agletAdded(ev);
	}
	public void agletCloned(ContextEvent ev) {
		agletAdded(ev);
	}
	public void agletCreated(ContextEvent ev) {
		agletAdded(ev);
	}
	public void agletDeactivated(ContextEvent ev) {
		agletRemoved(ev);
	}
	public void agletDispatched(ContextEvent ev) {
		agletRemoved(ev);
	}
	public void agletDisposed(ContextEvent ev) {
		agletRemoved(ev);
	}
	public void agletRemoved(ContextEvent ev) {}
	public void agletResumed(ContextEvent ev) {
		agletAdded(ev);
	}
	public void agletReverted(ContextEvent ev) {
		agletRemoved(ev);
	}
	public void agletStateChanged(ContextEvent ev) {}
	public void agletSuspended(ContextEvent ev) {
		agletRemoved(ev);
	}
	public void contextShutdown(ContextEvent ev) {}
	public void contextStarted(ContextEvent ev) {}
	public void showDocument(ContextEvent ev) {}
	public void showMessage(ContextEvent ev) {}
}

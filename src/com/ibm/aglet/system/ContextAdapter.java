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
 * The adapter which receives aglet context events. This class is provided as
 * convenience for easily creating listerns by extending this class and
 * overriding only the methods of interest.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:54 $
 * @author Misturu Oshima
 */
public class ContextAdapter implements ContextListener {
	@Override
	public void agletActivated(final ContextEvent ev) {
		agletAdded(ev);
	}

	/**
	 */
	public void agletAdded(final ContextEvent ev) {
	}

	@Override
	public void agletArrived(final ContextEvent ev) {
		agletAdded(ev);
	}

	@Override
	public void agletCloned(final ContextEvent ev) {
		agletAdded(ev);
	}

	@Override
	public void agletCreated(final ContextEvent ev) {
		agletAdded(ev);
	}

	@Override
	public void agletDeactivated(final ContextEvent ev) {
		agletRemoved(ev);
	}

	@Override
	public void agletDispatched(final ContextEvent ev) {
		agletRemoved(ev);
	}

	@Override
	public void agletDisposed(final ContextEvent ev) {
		agletRemoved(ev);
	}

	public void agletRemoved(final ContextEvent ev) {
	}

	@Override
	public void agletResumed(final ContextEvent ev) {
		agletAdded(ev);
	}

	@Override
	public void agletReverted(final ContextEvent ev) {
		agletRemoved(ev);
	}

	@Override
	public void agletStateChanged(final ContextEvent ev) {
	}

	@Override
	public void agletSuspended(final ContextEvent ev) {
		agletRemoved(ev);
	}

	@Override
	public void contextShutdown(final ContextEvent ev) {
	}

	@Override
	public void contextStarted(final ContextEvent ev) {
	}

	@Override
	public void showDocument(final ContextEvent ev) {
	}

	@Override
	public void showMessage(final ContextEvent ev) {
	}
}

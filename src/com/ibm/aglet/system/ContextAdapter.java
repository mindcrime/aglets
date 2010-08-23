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
    public void agletActivated(ContextEvent ev) {
	this.agletAdded(ev);
    }

    /**
     */
    public void agletAdded(ContextEvent ev) {
    }

    @Override
    public void agletArrived(ContextEvent ev) {
	this.agletAdded(ev);
    }

    @Override
    public void agletCloned(ContextEvent ev) {
	this.agletAdded(ev);
    }

    @Override
    public void agletCreated(ContextEvent ev) {
	this.agletAdded(ev);
    }

    @Override
    public void agletDeactivated(ContextEvent ev) {
	this.agletRemoved(ev);
    }

    @Override
    public void agletDispatched(ContextEvent ev) {
	this.agletRemoved(ev);
    }

    @Override
    public void agletDisposed(ContextEvent ev) {
	this.agletRemoved(ev);
    }

    public void agletRemoved(ContextEvent ev) {
    }

    @Override
    public void agletResumed(ContextEvent ev) {
	this.agletAdded(ev);
    }

    @Override
    public void agletReverted(ContextEvent ev) {
	this.agletRemoved(ev);
    }

    @Override
    public void agletStateChanged(ContextEvent ev) {
    }

    @Override
    public void agletSuspended(ContextEvent ev) {
	this.agletRemoved(ev);
    }

    @Override
    public void contextShutdown(ContextEvent ev) {
    }

    @Override
    public void contextStarted(ContextEvent ev) {
    }

    @Override
    public void showDocument(ContextEvent ev) {
    }

    @Override
    public void showMessage(ContextEvent ev) {
    }
}

package com.ibm.aglet.system;

/*
 * @(#)ContextListener.java
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

import java.util.EventListener;

import com.ibm.aglet.Aglet;

/**
 * The ContextListener interface specifies a set of methods for receiving
 * context events.
 * 
 * @version 2.00 96/6/10
 * @author Mitsuru Oshima
 */

public interface ContextListener extends EventListener {

    /**
     * Called when an aglet has been activated
     */
    public void agletActivated(ContextEvent ev);

    /**
     * Called when an aglet has arrived
     */
    public void agletArrived(ContextEvent ev);

    /**
     * Called when an aglet has been cloned
     */
    public void agletCloned(ContextEvent ev);

    /**
     * Called when an aglet has been created
     */
    public void agletCreated(ContextEvent ev);

    /**
     * Called when an aglet has been deactivated
     */
    public void agletDeactivated(ContextEvent ev);

    /**
     * Called when an aglet has been dispatched
     */
    public void agletDispatched(ContextEvent ev);

    /**
     * Called when an aglet has been disposed
     */
    public void agletDisposed(ContextEvent ev);

    /**
     * Called when an aglet has been resumed
     */
    public void agletResumed(ContextEvent ev);

    /**
     * Called when an aglet has been reverted.
     */
    public void agletReverted(ContextEvent ev);

    /**
     * Called when the state of an aglet has changed.
     */
    public void agletStateChanged(ContextEvent ev);

    /**
     * Called when an aglet has been suspended
     */
    public void agletSuspended(ContextEvent ev);

    /**
     * Called when shutting down
     */
    public void contextShutdown(ContextEvent ev);

    /**
     * Called when the context is started.
     */
    public void contextStarted(ContextEvent ev);

    /**
     * Called when an aglet request to show the document given as URL
     * 
     * @see Aglet#showDocument
     */
    public void showDocument(ContextEvent ev);

    /**
     * Called to show the message
     */
    public void showMessage(ContextEvent ev);
}

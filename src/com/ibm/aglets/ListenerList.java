package com.ibm.aglets;

/*
 * @(#)AgletContextImpl.java
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

import java.util.Iterator;

import com.ibm.aglet.system.ContextEvent;
import com.ibm.aglet.system.ContextListener;

/**
 * This class is used to hold a list of context listeners and to forward an
 * event to each listener. The class works as a linked list, and for each call
 * to notify an event, the class extract each context listener and forwards the
 * event to it.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 *         25/ago/07
 */
public final class ListenerList extends java.util.LinkedList<ContextListener>
implements ContextListener {

    /**
     * 
     */
    private static final long serialVersionUID = 4801520071583607074L;

    /**
     * Notifies all listeners than the aglet has been activated.
     */
    @Override
    public synchronized void agletActivated(ContextEvent event) {
	Iterator listener = this.iterator();
	while ((listener != null) && listener.hasNext()) {
	    ((ContextListener) listener.next()).agletActivated(event);

	}
    }

    /**
     * Notifies all listeners than an aglet has arrived.
     */
    @Override
    public synchronized void agletArrived(ContextEvent event) {
	Iterator listener = this.iterator();
	while ((listener != null) && listener.hasNext()) {
	    ((ContextListener) listener.next()).agletArrived(event);

	}
    }

    /**
     * Notifies all listeners than an aglet has been cloned.
     */
    @Override
    public synchronized void agletCloned(ContextEvent event) {
	Iterator listener = this.iterator();
	while ((listener != null) && listener.hasNext()) {
	    ((ContextListener) listener.next()).agletCloned(event);

	}
    }

    /**
     * Notifies all listeners than an aglet has been created.
     */
    @Override
    public synchronized void agletCreated(ContextEvent event) {
	Iterator listener = this.iterator();
	while ((listener != null) && listener.hasNext()) {
	    ((ContextListener) listener.next()).agletCreated(event);

	}
    }

    /**
     * Notifies all listeners than the aglet has been deactivated.
     */
    @Override
    public synchronized void agletDeactivated(ContextEvent event) {
	Iterator listener = this.iterator();
	while ((listener != null) && listener.hasNext()) {
	    ((ContextListener) listener.next()).agletDeactivated(event);

	}

    }

    /**
     * Notifies all listeners than an aglet has been dispatched.
     */
    @Override
    public synchronized void agletDispatched(ContextEvent event) {
	Iterator listener = this.iterator();
	while ((listener != null) && listener.hasNext()) {
	    ((ContextListener) listener.next()).agletDispatched(event);

	}

    }

    /**
     * Notifies all listeners than an aglet has been disposed.
     */
    @Override
    public synchronized void agletDisposed(ContextEvent event) {
	Iterator listener = this.iterator();
	while ((listener != null) && listener.hasNext()) {
	    ((ContextListener) listener.next()).agletDisposed(event);

	}
    }

    /**
     * Notifies all listeners than an aglet has been resumed.
     */
    @Override
    public synchronized void agletResumed(ContextEvent event) {
	Iterator listener = this.iterator();
	while ((listener != null) && listener.hasNext()) {
	    ((ContextListener) listener.next()).agletResumed(event);

	}
    }

    /**
     * Notifies all listeners than an aglet has been called home.
     */
    @Override
    public void agletReverted(ContextEvent event) {
	Iterator listener = this.iterator();
	while ((listener != null) && listener.hasNext()) {
	    ((ContextListener) listener.next()).agletReverted(event);

	}
    }

    /**
     * Notifies all listeners than the state of the agent has changed.
     */
    @Override
    public synchronized void agletStateChanged(ContextEvent event) {
	Iterator listener = this.iterator();
	while ((listener != null) && listener.hasNext()) {
	    ((ContextListener) listener.next()).agletStateChanged(event);

	}
    }

    /**
     * Notifies all listeners than the state of the agent has been suspended.
     */
    @Override
    public synchronized void agletSuspended(ContextEvent event) {
	Iterator listener = this.iterator();
	while ((listener != null) && listener.hasNext()) {
	    ((ContextListener) listener.next()).agletSuspended(event);

	}
    }

    /**
     * Notifies all listeners that the context has been shut down.
     */
    @Override
    public synchronized void contextShutdown(ContextEvent event) {
	Iterator listener = this.iterator();
	while ((listener != null) && listener.hasNext()) {
	    ((ContextListener) listener.next()).contextShutdown(event);

	}
    }

    /**
     * Notifies all listeners that the context has been started.
     */
    @Override
    public synchronized void contextStarted(ContextEvent event) {
	Iterator listener = this.iterator();
	while ((listener != null) && listener.hasNext()) {
	    ((ContextListener) listener.next()).contextStarted(event);

	}
    }

    /**
     * Notifies of a show document call.
     */
    @Override
    public synchronized void showDocument(ContextEvent event) {
	Iterator listener = this.iterator();
	while ((listener != null) && listener.hasNext()) {
	    ((ContextListener) listener.next()).showDocument(event);

	}
    }

    /**
     * Notifies of a show message call.
     */
    @Override
    public synchronized void showMessage(ContextEvent event) {
	Iterator listener = this.iterator();
	while ((listener != null) && listener.hasNext()) {
	    ((ContextListener) listener.next()).showMessage(event);

	}
    }
}

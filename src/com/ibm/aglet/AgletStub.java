package com.ibm.aglet;

/*
 * @(#)AgletStub.java
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

import java.io.IOException;
import java.net.URL;
import java.security.PermissionCollection;

import com.ibm.aglet.message.MessageManager;

/**
 * Abstract class AgletStub is used to implement an aglet behavior. It is not
 * normally used by aglet programmers.
 * 
 * @version 1.30 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 * @author ONO Kouichi
 */
abstract public class AgletStub {

    /*
     * Constructs AgletProxy
     */
    protected AgletStub() {
    }

    /*
     * Clones the aglet
     */
    @Override
    abstract protected Object clone() throws CloneNotSupportedException;

    /*
     * Deactivates the aglet
     */
    abstract protected void deactivate(long duration) throws IOException;

    // trip with Ticket
    abstract protected void dispatch(Ticket ticket) throws IOException,
	    RequestRefusedException;

    /*
     * Dispatches the aglet
     */
    abstract protected void dispatch(URL url) throws IOException,
	    RequestRefusedException;

    /*
     * Disposes the aglet
     */
    abstract protected void dispose();

    /*
     * Gets the aglet context in which the aglet lives.
     * 
     * @exception InvalidAgletException if the aglet is invalid.
     */
    abstract protected AgletContext getAgletContext();

    /*
     * Gets the info
     */
    abstract protected AgletInfo getAgletInfo();

    /**
     * Gets the aglet's message manager object.
     * 
     * @see aglet.MessageManager
     * @return the method manager
     * @exception InvalidAgletException
     *                if the aglet is not valid.
     */
    abstract protected MessageManager getMessageManager();

    /**
     * Gets the protections: permission collection about who can send what kind
     * of messages to the aglet
     * 
     * @return collection of protections about who can send what kind of
     *         messages to the aglet
     */
    abstract protected PermissionCollection getProtections();

    /**
     * Gets the current content of the Aglet's message line.
     * 
     * @return the message line.
     * @exception InvalidAgletException
     *                if the aglet is not valid.
     */
    abstract protected String getText();

    /*
     * Sets a aglet.
     */
    abstract protected void setAglet(Aglet aglet);

    // # /*
    // # * Gets the allowance: availability of the aglet's resources.
    // # */
    // # abstract protected Allowance getAllowance();

    /**
     * Sets the protections: permission collection about who can send what kind
     * of messages to the aglet
     * 
     * @param protections
     *            collection of protections about who can send what kind of
     *            messages to the aglet
     */
    abstract protected void setProtections(PermissionCollection protections);

    /**
     * Sets a aglet's text
     */
    abstract protected void setText(String text);

    /*
     * Take a snapshot of the aglet into the 2nd strage. This will be activated
     * only if the system clashed without being dispatched, deactivated or
     * disposed.
     */
    abstract protected void snapshot() throws IOException;

    /*
     * subscribe the specific multicast message
     */
    abstract protected void subscribeMessage(String name);

    /*
     * Suspends the aglet
     */
    abstract protected void suspend(long duration) throws InvalidAgletException;

    /*
     * unsubscribe All messages the aglet had been subscribing.
     */
    abstract protected void unsubscribeAllMessages();

    /*
     * unsubscribe the message
     */
    abstract protected boolean unsubscribeMessage(String name);
}

package com.ibm.aglets;

/*
 * @(#)AgletRef.java
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
import java.io.IOException;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.NotHandledException;
import com.ibm.aglet.message.FutureReply;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageException;

public interface AgletRef extends com.ibm.awb.weakref.Ref {

    /**
     * Activate the aglet
     */
    public void activate() throws IOException, AgletException;

    public void checkValidation() throws InvalidAgletException;

    /**
     * Delegates a message
     */
    public void delegateMessage(Message msg) throws InvalidAgletException;

    // # /**
    // # * Gets the allowance: availability of the aglet's resources.
    // # * @return an Allowance object
    // # */
    // # public Allowance getAllowance();

    /**
     * Gets the address of the target aglet.
     * 
     * @return the address
     */
    public String getAddress() throws InvalidAgletException;

    /**
     * Gets the aglet. If the aglet is access protected it will require the
     * right key to get access.
     * 
     * @return the aglet
     * @exception SecurityException
     *                if the current execution is not allowed.
     */
    public Aglet getAglet() throws InvalidAgletException;

    /**
     * Gets the information of the aglet
     * 
     * @return the AgletInfo of the aglet
     */
    public AgletInfo getAgletInfo();

    /**
     * Checks if it's active.
     */
    public boolean isActive();

    /**
     * Checks if it's valid.
     */
    public boolean isRemote();

    /**
     * Checks a state of the aglet.
     */
    public boolean isState(int s);

    /**
     * Checks if it's valid.
     */
    public boolean isValid();

    /**
     * Resume the aglet
     */
    public void resume() throws AgletException;

    /**
     * Sends a future message in asynchronous way.
     * 
     * @param msg
     *            the message to send
     */
    public FutureReply sendFutureMessage(Message msg)
    throws InvalidAgletException;

    /**
     * Sends a message in synchronous way.
     * 
     * @param msg
     *            the message to send
     */
    public Object sendMessage(Message msg)
    throws MessageException,
    InvalidAgletException,
    NotHandledException;

    /**
     * Sends an oneway message
     * 
     * @param msg
     *            the message to send
     */
    public void sendOnewayMessage(Message msg) throws InvalidAgletException;
}

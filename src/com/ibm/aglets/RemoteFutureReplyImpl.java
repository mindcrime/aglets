package com.ibm.aglets;

/*
 * @(#)RemoteFutureReplyImpl.java
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

import com.ibm.aglet.message.MessageException;
import com.ibm.maf.MAFAgentSystem;

/**
 * The <tt>FutureReplyImpl</tt> class is an implementation of
 * com.ibm.aglet.FutureReply abstract class.
 * 
 * @version 1.30 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 */
final class RemoteFutureReplyImpl extends FutureReplyImpl {
    static final Integer HANDLED = new Integer(0);
    static final Integer MESSAGE_EXCEPTION = new Integer(1);
    static final Integer NOT_HANDLED = new Integer(2);

    private MAFAgentSystem agentsystem;
    private ResourceManager rmanager;
    private long return_id;

    RemoteFutureReplyImpl(MAFAgentSystem as, ResourceManager rm, long id) {
	this.agentsystem = as;
	this.rmanager = rm;
	this.return_id = id;
    }

    @Override
    synchronized void cancel(String msg) {
	super.cancel(msg);

	try {
	    Object ret[] = new Object[2];

	    ret[0] = NOT_HANDLED;
	    ret[1] = msg;
	    byte reply[] = MessageOutputStream.toByteArray(this.rmanager, ret);

	    this.agentsystem.receive_future_reply(this.return_id, reply);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    @Override
    synchronized void setExceptionAndNotify(Throwable ex) {
	super.setExceptionAndNotify(ex);

	try {
	    Object ret[] = new Object[2];

	    ret[0] = MESSAGE_EXCEPTION;
	    ret[1] = new MessageException(ex);
	    byte reply[] = MessageOutputStream.toByteArray(this.rmanager, ret);

	    this.agentsystem.receive_future_reply(this.return_id, reply);
	} catch (Exception exx) {
	    exx.printStackTrace();
	}
    }

    @Override
    synchronized void setReplyAndNotify(Object result) {
	super.setReplyAndNotify(result);

	try {
	    Object ret[] = new Object[2];

	    ret[0] = HANDLED;
	    ret[1] = result;

	    byte[] reply = MessageOutputStream.toByteArray(this.rmanager, ret);

	    this.agentsystem.receive_future_reply(this.return_id, reply);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}

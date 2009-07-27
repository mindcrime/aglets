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

import com.ibm.aglet.NotHandledException;
import com.ibm.aglet.FutureReply;
import com.ibm.aglet.ReplySet;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.message.MessageException;

import java.util.Vector;
import java.util.Enumeration;
import com.ibm.maf.MAFAgentSystem;

/**
 * The <tt>FutureReplyImpl</tt> class is an implementation of
 * com.ibm.aglet.FutureReply abstract class.
 * 
 * @version     1.30    $Date: 2009/07/27 10:31:41 $
 * @author	Mitsuru Oshima
 */
final class RemoteFutureReplyImpl extends FutureReplyImpl {
	static final Integer HANDLED = new Integer(0);
	static final Integer MESSAGE_EXCEPTION = new Integer(1);
	static final Integer NOT_HANDLED = new Integer(2);

	private MAFAgentSystem agentsystem;
	private ResourceManager rmanager;
	private long return_id;

	RemoteFutureReplyImpl(MAFAgentSystem as, ResourceManager rm, long id) {
		agentsystem = as;
		rmanager = rm;
		return_id = id;
	}
	synchronized void cancel(String msg) {
		super.cancel(msg);

		try {
			Object ret[] = new Object[2];

			ret[0] = NOT_HANDLED;
			ret[1] = msg;
			byte reply[] = MessageOutputStream.toByteArray(rmanager, ret);

			agentsystem.receive_future_reply(return_id, reply);
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	synchronized void setExceptionAndNotify(Throwable ex) {
		super.setExceptionAndNotify(ex);

		try {
			Object ret[] = new Object[2];

			ret[0] = MESSAGE_EXCEPTION;
			ret[1] = new MessageException(ex);
			byte reply[] = MessageOutputStream.toByteArray(rmanager, ret);

			agentsystem.receive_future_reply(return_id, reply);
		} catch (Exception exx) {
			exx.printStackTrace();
		} 
	}
	synchronized void setReplyAndNotify(Object result) {
		super.setReplyAndNotify(result);

		try {
			Object ret[] = new Object[2];

			ret[0] = HANDLED;
			ret[1] = result;

			byte[] reply = MessageOutputStream.toByteArray(rmanager, ret);

			agentsystem.receive_future_reply(return_id, reply);
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
}

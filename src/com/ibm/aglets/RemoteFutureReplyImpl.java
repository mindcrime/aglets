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

	private final MAFAgentSystem agentsystem;
	private final ResourceManager rmanager;
	private final long return_id;

	RemoteFutureReplyImpl(final MAFAgentSystem as, final ResourceManager rm, final long id) {
		agentsystem = as;
		rmanager = rm;
		return_id = id;
	}

	@Override
	synchronized void cancel(final String msg) {
		super.cancel(msg);

		try {
			final Object ret[] = new Object[2];

			ret[0] = NOT_HANDLED;
			ret[1] = msg;
			final byte reply[] = MessageOutputStream.toByteArray(rmanager, ret);

			agentsystem.receive_future_reply(return_id, reply);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	synchronized void setExceptionAndNotify(final Throwable ex) {
		super.setExceptionAndNotify(ex);

		try {
			final Object ret[] = new Object[2];

			ret[0] = MESSAGE_EXCEPTION;
			ret[1] = new MessageException(ex);
			final byte reply[] = MessageOutputStream.toByteArray(rmanager, ret);

			agentsystem.receive_future_reply(return_id, reply);
		} catch (final Exception exx) {
			exx.printStackTrace();
		}
	}

	@Override
	synchronized void setReplyAndNotify(final Object result) {
		super.setReplyAndNotify(result);

		try {
			final Object ret[] = new Object[2];

			ret[0] = HANDLED;
			ret[1] = result;

			final byte[] reply = MessageOutputStream.toByteArray(rmanager, ret);

			agentsystem.receive_future_reply(return_id, reply);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}
}

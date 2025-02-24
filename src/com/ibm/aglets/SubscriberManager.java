package com.ibm.aglets;

/*
 * @(#)SubscriberManager.java
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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.ReplySet;

/**
 * The <tt>SubscriberManager</tt> class defines a subscribe machanism for
 * communication among aglets in the same aglet context. Upon any change to the
 * property, all the subscribed aglets are notified.
 * 
 * @version 1.10 96/11/28
 * @author Yariv Aridor
 * @author Mitsuru Oshima
 */

final class SubscriberManager {

	private final Hashtable dependent = new Hashtable();

	public SubscriberManager() {
	}

	// REMIND: performance can be improved.

	/* synchronized */
	public ReplySet multicastMessage(final Message msg) {
		MessageManagerImpl owners_manager = null;

		final ReplySet replySet = new ReplySet();
		Vector v = (Vector) dependent.get(msg.getKind());

		if (v == null) {

			// no subscriber
			return replySet;
		}

		// synchronizing this method will cause the dead lock
		v = (Vector) v.clone(); // for avoid synchronized block

		for (final Enumeration e = v.elements(); e.hasMoreElements();) {
			final LocalAgletRef ref = (LocalAgletRef) e.nextElement();

			// MessageManagerImpl mm =
			// (MessageManagerImpl) ref.getMessageManager();
			final MessageManagerImpl mm = ref.messageManager;

			if (mm == null) {
				System.out.println("MessageManager is null. "
						+ ref.getStateAsString());
				continue;
			}

			try {
				if (mm.isOwner()) {
					owners_manager = mm;
				} else {
					final FutureReplyImpl future = new FutureReplyImpl();
					final MessageImpl m = new MessageImpl(msg, future, Message.FUTURE, System.currentTimeMillis());

					replySet.addFutureReply(future);

					// sendMessage cannot be used in order to avoid
					// security check
					mm.postMessage(m);
				}
			} catch (final RuntimeException ex) {
				ex.printStackTrace();
			}
		}

		if (owners_manager != null) {
			try {
				final FutureReplyImpl future = new FutureReplyImpl();
				final MessageImpl m = new MessageImpl(msg, future, Message.FUTURE, System.currentTimeMillis());

				replySet.addFutureReply(future);

				// sendMessage cannot be used in order to avoid
				// security check
				owners_manager.postMessage(m);
			} catch (final RuntimeException ex) {
				ex.printStackTrace();
			}
		}

		return replySet;
	}

	synchronized public void subscribe(final LocalAgletRef ref, final String name) {
		Vector v = (Vector) dependent.get(name);

		if (v == null) {
			v = new Vector();
			dependent.put(name, v);
		}

		if (v.indexOf(ref) < 0) {
			v.addElement(ref);
		}
	}

	synchronized public boolean unsubscribe(final LocalAgletRef ref, final String name) {
		final Vector v = (Vector) dependent.get(name);

		if (v != null) {
			return v.removeElement(ref);
		}
		return false;
	}

	synchronized public void unsubscribeAll(final LocalAgletRef ref) {
		for (final Enumeration e = dependent.elements(); e.hasMoreElements();) {
			((Vector) e.nextElement()).removeElement(ref);
		}
	}
}

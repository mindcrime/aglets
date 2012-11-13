package com.ibm.aglets;

import com.ibm.aglet.message.MessageManager;

/**
 * The MessageManagerFactory represents a factory for getting the current
 * implementation of a MessageManager. This class is used to uncouple the code
 * from the actual implementation of the MessageManager (e.g.,
 * MessageManagerImpl).
 * 
 * @author Luca Ferrari cat4hire@users.sourceforge.net 25-mag-2005
 * @version 1.0
 */
public class MessageManagerFactory {

	/**
	 * A static method to get the current implementation of the MessageManager
	 * for this aglets installation. By default this method returns a
	 * MessageManagerImpl, but you can override the method in a subclass.
	 * 
	 * @param ref
	 *            the agletref to attach the message manager to
	 * @return a new message manager or null in the case the agletref is not a
	 *         LocalAgletRef (the only one this factory can deal with).
	 */
	public static MessageManager getMessageManager(final AgletRef ref) {
		if (ref instanceof LocalAgletRef) {
			final LocalAgletRef lref = (LocalAgletRef) ref;
			return new MessageManagerImpl(lref);
		}

		return null;
	}
}

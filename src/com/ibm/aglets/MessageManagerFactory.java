package com.ibm.aglets;
<<<<<<< HEAD
import com.ibm.aglet.message.MessageManager;
=======
import com.ibm.aglet.MessageManager;
>>>>>>> 218a26853c9210d659d7703d268e4b377f579a3b

/**
 * The MessageManagerFactory represents a factory for getting
 * the current implementation of a MessageManager. This class is used
 * to uncouple the code from the actual implementation of the
 * MessageManager (e.g., MessageManagerImpl).
 * @author Luca Ferrari cat4hire@users.sourceforge.net
 * 25-mag-2005
 * @version 1.0
 */
public class MessageManagerFactory {
	
	/**
	 * A static method to get the current implementation of the
	 * MessageManager for this aglets installation. By default this
	 * method returns a MessageManagerImpl, but you can override
	 * the method in a subclass.
	 * @param ref the agletref to attach the message manager to
	 * @return a new message manager or null in the case the
	 * agletref is not a LocalAgletRef (the only one this factory can deal
	 * with).
	 */
	public static MessageManager getMessageManager(AgletRef ref){
		if( ref instanceof LocalAgletRef){
			LocalAgletRef lref = (LocalAgletRef) ref;
<<<<<<< HEAD
			return  new MessageManagerImpl(lref);
=======
			return new MessageManagerImpl(lref);
>>>>>>> 218a26853c9210d659d7703d268e4b377f579a3b
		}
		
		return null;
	}
}

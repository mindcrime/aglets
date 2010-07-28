package com.ibm.aglets;

import com.ibm.aglet.message.Message;

/**
 * A factory for messages, in order to decouple the code from the creation of an
 * exact implementation of the Message class.
 * 
 * @author Luca Ferrari cat4hire@users.sourceforge.net 25-mag-2005
 * @version 1.0
 */
public class MessageFactory {

    /**
     * Get the message implementation.
     * 
     * @return the message implementation, by default a MessageImpl
     */
    public static Message getMessage() {
	return new MessageImpl();
    }

    /**
     * Get the message implementation.
     * 
     * @param msg
     *            a message to clone
     * @param future
     *            the future reply of this message
     * @param msg_type
     *            the message type
     * @param timestamp
     *            the timestamp of the message
     * @return the message
     */
    public static Message getMessage(
				     Message msg,
				     FutureReplyImpl future,
				     int msg_type,
				     long timestamp) {
	return new MessageImpl(msg, future, msg_type, timestamp);
    }
}

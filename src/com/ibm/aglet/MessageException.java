package com.ibm.aglet;

/**
 * This class is only a placeholder for backward compatibility, please refer to
 * the new API package com.ibm.aglet.Message for any detail.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 *         27/ago/07
 */
public class MessageException extends com.ibm.aglet.message.MessageException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -67525147958042378L;

	public MessageException(final Throwable t) {
		super(t);
	}

	public MessageException(final Throwable t, final String s) {
		super(t, s);
	}
}

package com.ibm.aglet;

/**
 * This class is only a placeholder for backward compatibility, please refer to the
 * new API package com.ibm.aglet.Message for any detail.
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 *
 * 27/ago/07
 */
public class MessageException extends com.ibm.aglet.message.MessageException {

    public MessageException(Throwable t){
	super(t);
    }
    
    public MessageException(Throwable t, String s){
	super(t,s);
    }
}

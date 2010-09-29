package net.sourceforge.aglets.rolex;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author Sandro Cremonini
 * @version 1.0
 */

import com.ibm.aglets.MessageImpl;

public class RolexMessage extends MessageImpl {

    /**
     * 
     */
    private static final long serialVersionUID = -3978234797697773927L;
    public static final int ASSUMPTION = 1;
    public static final int RELEASE = 2;
    public static final int SOURCE_ROLE = 3;

    int type;

    /**
     * Constructor.
     * 
     * @param kind
     *            String
     * @param arg
     *            Object
     * @param type
     *            int
     */
    public RolexMessage(String kind, Object arg, int type) {
	super(arg);
	this.kind = kind;
	this.type = type;
    }

    /**
     * Returns type.
     * 
     * @return int
     */
    public int getType() {
	return this.type;
    }

    /**
     * If type is ASSUMPTION or RELEASE returns true: the message is exclusive;
     * it's not possible process this message at the same time of other
     * messages.
     * 
     * @return boolean
     */
    public boolean isExclusive() {
	if ((this.type == ASSUMPTION) || (this.type == RELEASE))
	    return true;
	else
	    return false;
    }

    @Override
    public Object clone() {
	return new RolexMessage(this.kind, this.arg, this.type);
    }

}

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

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.MessageManager;
import com.ibm.aglet.RequestRefusedException;
import com.ibm.aglets.MessageImpl;


public class RolexMessage extends MessageImpl {

    public static final int ASSUMPTION = 1;
    public static final int RELEASE = 2;
    public static final int SOURCE_ROLE = 3;

    int type;

    /**
     * Constructor.
     * @param kind String
     * @param arg Object
     * @param type int
     */
    public RolexMessage(String kind, Object arg, int type) {
        super(arg);
        this.kind = kind;
        this.type = type;
    }

    /**
     * Returns type.
     * @return int
     */
    public int getType() {
        return type;
    }

    /**
     * If type is ASSUMPTION or RELEASE returns true: the message is
     * exclusive; it's not possible process this message at the same time of
     * other messages.
     * @return boolean
     */
    public boolean isExclusive() {
        if (type==ASSUMPTION || type==RELEASE)
            return true;
        else
            return false;
    }

    public Object clone(){
    	return new RolexMessage(this.kind,this.arg, this.type);
    }

}

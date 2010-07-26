package com.ibm.aglets;

/*
 * @(#)SystemMessage.java
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

import java.io.IOException;
import java.security.Permission;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.RequestRefusedException;
import com.ibm.aglet.Ticket;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.security.AgletProtection;
import com.ibm.aglets.security.AgletPermission;

final class SystemMessage extends MessageImpl {

    static final int CREATE = 2;
    static final int RUN = 7;

    static final int CLONE_REQUEST = 8;
    static final int DISPOSE_REQUEST = 9;
    static final int DISPATCH_REQUEST = 10;
    static final int DEACTIVATE_REQUEST = 11;
    static final int RETRACT_REQUEST = 12;
    static final int SUSPEND_REQUEST = 13;

    int type;

    SystemMessage(int type, Object arg) {
	super(arg);
	this.future = new FutureReplyImpl();
	this.type = type;
	if (type != RUN) {

	    //
	    // RUN method should NORM priority
	    //
	    this.priority = Message.SYSTEM_PRIORITY;
	}
	this.delegatable = false;
    }

    /*
     * SYSTEM LEVEL MESSAGE for onCreation, onClone, onArrival and onActivation.
     */
    SystemMessage(String kind, Object arg, int type) {
	super(arg);
	this.kind = kind;
	this.type = type;
	this.priority = Message.SYSTEM_PRIORITY;
	this.delegatable = false;
    }

    @Override
    Permission getPermission(String authority) {
	switch (this.type) {
	case CLONE_REQUEST:
	    return new AgletPermission(authority, "clone");
	case DISPOSE_REQUEST:
	    return new AgletPermission(authority, "dispose");
	case DISPATCH_REQUEST:
	    return new AgletPermission(authority, "dispatch");
	case DEACTIVATE_REQUEST:
	case SUSPEND_REQUEST:
	    return new AgletPermission(authority, "deactivate");
	case RETRACT_REQUEST:

	    // REMIND: whoever can ask to retract now
	    return new AgletPermission(authority, "retract");
	default:
	    return null;
	}
    }

    @Override
    Permission getProtection(String authority) {
	switch (this.type) {
	case CLONE_REQUEST:
	    return new AgletProtection(authority, "clone");
	case DISPOSE_REQUEST:
	    return new AgletProtection(authority, "dispose");
	case DISPATCH_REQUEST:
	    return new AgletProtection(authority, "dispatch");
	case DEACTIVATE_REQUEST:
	case SUSPEND_REQUEST:
	    return new AgletProtection(authority, "deactivate");
	case RETRACT_REQUEST:

	    // REMIND: whoever can ask to retract now
	    return new AgletProtection(authority, "retract");
	default:
	    return null;
	}
    }

    /*
     * boolean isDelegatable() { return false; }
     */

    @Override
    public void handle(LocalAgletRef ref) throws InvalidAgletException {
	FutureReplyImpl f = this.future;
	Aglet aglet = ref.aglet;

	switch (this.type) {
	case RUN:
	    aglet.run();
	    break;

	case CREATE:
	    aglet.onCreation(this.arg);
	    break;

	/*
	 * Handles requests
	 */
	case CLONE_REQUEST:
	    try {

		// Debug.check();
		f.setReplyAndNotify(ref._clone());

		// REMIND: we'll get rid of unnecessary
		// exceptions in the future
	    } catch (CloneNotSupportedException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (SecurityException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (RuntimeException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (Error ex) {
		f.sendExceptionIfNeeded(ex);
		throw ex;

	    } finally {

		// Debug.check();
		f.sendReplyIfNeeded(null);
	    }
	    break;

	case DISPOSE_REQUEST:
	    try {

		// Debug.check();
		ref.dispose(this);

		// REMIND: we'll get rid of unnecessary
		// exceptions in the future
	    } catch (InvalidAgletException ex) {

		// not handled!
		f.sendExceptionIfNeeded(ex);
		throw ex;

	    } catch (SecurityException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (RuntimeException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (RequestRefusedException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (Error ex) {
		f.sendExceptionIfNeeded(ex);
		throw ex;

	    } finally {

		// Debug.check();
		f.sendReplyIfNeeded(null);
	    }

	    break;

	case DISPATCH_REQUEST:
	    try {

		// Debug.check();
		ref.dispatch(this, (Ticket) this.arg);

		// REMIND: we'll get rid of unnecessary
		// exceptions in the future
	    } catch (InvalidAgletException ex) {

		// not handled
		f.cancel("In dispatch : " + ex.getMessage());
		throw ex;

	    } catch (RequestRefusedException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (IOException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (SecurityException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (RuntimeException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (Error ex) {
		f.sendExceptionIfNeeded(ex);
		throw ex;

	    } finally {

		// Debug.check();
		f.sendReplyIfNeeded(null);

		// f.complete(false, null, "dispatch");
	    }
	    break;

	case DEACTIVATE_REQUEST:
	    try {

		// Debug.check();
		ref.deactivate(this, ((Long) this.arg).longValue());

		// REMIND: we'll get rid of unnecessary
		// exceptions in the future
	    } catch (InvalidAgletException ex) {

		// not handled
		f.cancel("In deactivate : " + ex.getMessage());
		throw ex;

	    } catch (IOException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (SecurityException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (RuntimeException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (RequestRefusedException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (Error ex) {
		f.sendExceptionIfNeeded(ex);
		throw ex;

	    } finally {

		// Debug.check();
		f.sendReplyIfNeeded(null);
	    }
	    break;

	case SUSPEND_REQUEST:
	    try {

		// Debug.check();
		ref.suspend(this, ((Long) this.arg).longValue());

		// REMIND: we'll get rid of unnecessary
		// exceptions in the future
	    } catch (InvalidAgletException ex) {

		// not handled
		f.cancel("In deactivate : " + ex.getMessage());
		throw ex;

	    } catch (SecurityException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (RuntimeException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (RequestRefusedException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (Error ex) {
		f.sendExceptionIfNeeded(ex);
		throw ex;

	    } finally {

		// Debug.check();
		f.sendReplyIfNeeded(null);
	    }
	    break;

	case RETRACT_REQUEST:
	    try {

		// Debug.check();
		ref.suspendForRetraction((Ticket) this.arg);

		// REMIND: we'll get rid of unnecessary
		// exceptions in the future
	    } catch (InvalidAgletException ex) {

		// not handled!
		f.cancel("In Retraction " + ex.getMessage());

	    } catch (SecurityException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (RuntimeException ex) {
		f.sendExceptionIfNeeded(ex);

	    } catch (Error ex) {
		f.sendExceptionIfNeeded(ex);
		throw ex;

	    } finally {

		// Debug.check();
		f.sendReplyIfNeeded(null);
	    }
	    break;
	}
    }

    @Override
    public String toString() {
	StringBuffer buff = new StringBuffer();

	switch (this.type) {
	case RUN:
	    buff.append("[Run ");
	    break;

	case CREATE:
	    buff.append("[Create ");
	    break;

	case CLONE_REQUEST:
	    buff.append("[Clone ");
	    break;

	case DISPOSE_REQUEST:
	    buff.append("[Dispose ");
	    break;

	case DISPATCH_REQUEST:
	    buff.append("[Dispatch : url = " + String.valueOf(this.arg));
	    break;

	case DEACTIVATE_REQUEST:
	    buff.append("[Deactivate : duration = " + String.valueOf(this.arg));
	    break;

	case SUSPEND_REQUEST:
	    buff.append("[Suspend : duration = " + String.valueOf(this.arg));
	    break;

	case RETRACT_REQUEST:
	    buff.append("[Retract : by = " + String.valueOf(this.arg));
	    break;
	default:
	    return "[Message: None]";
	}

	buff.append(": prority = " + this.priority);
	if (this.waiting) {
	    buff.append(" :waiting ");
	}
	buff.append(']');

	return buff.toString();
    }
}

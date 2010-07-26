package com.ibm.aglet.security;

/*
 * @(#)Protections.java
 * 
 * (c) Copyright IBM Corp. 1998
 * 
 * IBM grants you a non-exclusive, non-transferrable License to
 * use this program internally solely for the purposes of testing
 * and evaluating Java Aglet API.
 * You may not distribute, sublicense, lease, rent or use this
 * sample program externally.
 * 
 * THIS ROGRAM IS PROVIDED "AS IS" WITHOUT ANY WARRANTY EXPRESS OR
 * IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE WARRANTY OF
 * NON-INFRINGEMENT AND THE WARRANTIES OF MERCHANTIBILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 * IBM WILL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY YOU AS
 * A RESULT OF USING THIS SAMPLE PROGRAM. IN NO EVENT WILL IBM BE
 * LIABLE FOR ANY SPECIAL, INDIRECT CONSEQUENTIAL DAMAGES OR LOST
 * PROFITS EVEN IF IBM HAS BEEN ADVISED OF THE POSSIBILITY OF THEIR
 * OCCURRENCE OR LOSS OF OR DAMAGE TO YOUR RECORDS OR DATA.
 * IBM WILL NOT BE LIABLE FOR ANY THIRD PARTY CLAIMS AGAINST YOU.
 */

import java.io.Serializable;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.Vector;

/**
 * The <tt>Protections</tt> class is a Permissions who contains only
 * AgletProtections or MessageProtections.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public class Protections extends PermissionCollection implements Serializable {
    private Vector pset = new Vector();

    public Protections() {
	super();
    }

    @Override
    synchronized public void add(Permission p) {
	if (p == null) {
	    return;
	}
	if (this.isReadOnly()) {
	    throw new SecurityException("attempt to add a Protection to a readonly Protections object");
	}
	if (p instanceof Protection) {
	    this.pset.addElement(p);
	}
    }

    @Override
    public Enumeration elements() {
	return this.pset.elements();
    }

    @Override
    public boolean implies(Permission p) {
	if (p instanceof com.ibm.aglet.security.AgletProtection) {
	    for (Enumeration e = this.pset.elements(); e.hasMoreElements();) {
		Permission t = (Permission) e.nextElement();
		if (t instanceof com.ibm.aglet.security.AgletProtection) {
		    if (!t.implies(p)) {
			return false;
		    }
		}
	    }
	} else {
	    for (Enumeration e = this.pset.elements(); e.hasMoreElements();) {
		Permission t = (Permission) e.nextElement();
		if (t instanceof com.ibm.aglet.security.MessageProtection) {
		    if (!t.implies(p)) {
			return false;
		    }
		}
	    }
	}
	return true;
    }
}

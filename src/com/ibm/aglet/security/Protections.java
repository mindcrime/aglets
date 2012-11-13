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
	/**
	 * 
	 */
	private static final long serialVersionUID = -2786264259022840099L;
	private final Vector pset = new Vector();

	public Protections() {
		super();
	}

	@Override
	synchronized public void add(final Permission p) {
		if (p == null) {
			return;
		}
		if (isReadOnly()) {
			throw new SecurityException("attempt to add a Protection to a readonly Protections object");
		}
		if (p instanceof Protection) {
			pset.addElement(p);
		}
	}

	@Override
	public Enumeration elements() {
		return pset.elements();
	}

	@Override
	public boolean implies(final Permission p) {
		if (p instanceof com.ibm.aglet.security.AgletProtection) {
			for (final Enumeration e = pset.elements(); e.hasMoreElements();) {
				final Permission t = (Permission) e.nextElement();
				if (t instanceof com.ibm.aglet.security.AgletProtection) {
					if (!t.implies(p)) {
						return false;
					}
				}
			}
		} else {
			for (final Enumeration e = pset.elements(); e.hasMoreElements();) {
				final Permission t = (Permission) e.nextElement();
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

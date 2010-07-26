package com.ibm.aglets.security;

/*
 * @(#)AgletPermission.java
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

import java.security.Permission;

public class AgletPermission extends PlainAgletPermission {

    // should be implemented?
    // ==== public PermissionCollection newPermissionCollection();
    public AgletPermission(String name, String actions) {
	super(name, actions);
    }

    @Override
    public boolean implies(Permission p) {
	if (!(p instanceof AgletPermission)) {
	    return false;
	}
	return super.implies(p);
    }
}

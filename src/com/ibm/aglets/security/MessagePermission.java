package com.ibm.aglets.security;

/*
 * @(#)MessagePermission.java
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

public class MessagePermission extends PlainMessagePermission {

	/**
	 * 
	 */
	private static final long serialVersionUID = -841386333497129077L;

	// should be implemented?
	// ==== public PermissionCollection newPermissionCollection();
	public MessagePermission(final String name, final String actions) {
		super(name, actions);
	}

	@Override
	public boolean implies(final Permission p) {
		if (!(p instanceof MessagePermission)) {
			return false;
		}
		return super.implies(p);
	}
}

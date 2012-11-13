package com.ibm.aglets;

/*
 * @(#)DeactivationInfo.java
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

import com.ibm.maf.Name;

final class DeactivationInfo implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1736860897551277584L;
	static final int DEACTIVATED = 0;
	static final int SUSPENDED = 1;

	long created;
	long wakeup;
	String key = null;
	Name agent_name;
	int mode = DEACTIVATED;

	transient DeactivationInfo next;

	DeactivationInfo(final Name n, final long w, final String k) {
		agent_name = n;
		wakeup = w;
		key = k;
		mode = DEACTIVATED;
	}

	DeactivationInfo(final Name n, final long w, final String k, final int m) {
		agent_name = n;
		wakeup = w;
		key = k;
		mode = m;
	}

	boolean isSnapshot() {
		return wakeup == -1;
	}

	boolean isSuspended() {
		return (mode == SUSPENDED);
	}
}

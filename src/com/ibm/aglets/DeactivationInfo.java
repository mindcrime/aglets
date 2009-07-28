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

import java.io.DataOutput;
import java.io.DataInput;
import java.io.IOException;
import com.ibm.maf.Name;

final class DeactivationInfo implements java.io.Serializable {

	static final int DEACTIVATED = 0;
	static final int SUSPENDED = 1;

	long created;
	long wakeup;
	String key = null;
	Name agent_name;
	int mode = DEACTIVATED;

	transient DeactivationInfo next;

	private DeactivationInfo() {}
	DeactivationInfo(Name n, long w, String k) {
		agent_name = n;
		wakeup = w;
		key = k;
		mode = DEACTIVATED;
	}
	DeactivationInfo(Name n, long w, String k, int m) {
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

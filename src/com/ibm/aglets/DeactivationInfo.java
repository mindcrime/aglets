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

    DeactivationInfo(Name n, long w, String k) {
	this.agent_name = n;
	this.wakeup = w;
	this.key = k;
	this.mode = DEACTIVATED;
    }

    DeactivationInfo(Name n, long w, String k, int m) {
	this.agent_name = n;
	this.wakeup = w;
	this.key = k;
	this.mode = m;
    }

    boolean isSnapshot() {
	return this.wakeup == -1;
    }

    boolean isSuspended() {
	return (this.mode == SUSPENDED);
    }
}

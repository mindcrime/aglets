package com.ibm.aglets.security;

/*
 * @(#)PlainMessagePermission.java
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

import java.io.Serializable;
import java.security.Permission;
import java.util.Vector;

public class PlainMessagePermission extends AgletsPermissionBase implements
Serializable {
    private String _actions = null;
    private Vector _messages = new Vector();

    // should be implemented?
    // ==== public PermissionCollection newPermissionCollection();
    public PlainMessagePermission(String name, String actions) {
	super(name);
	String[] actionList = split(actions.toLowerCase(), SEPARATORS);
	int i = 0;

	for (i = 0; i < actionList.length; i++) {
	    this._messages.addElement(actionList[i]);
	}
	this._actions = concat(this._messages);
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == this) {
	    return true;
	}
	if (!(obj instanceof PlainMessagePermission)) {
	    return false;
	}
	PlainMessagePermission o = (PlainMessagePermission) obj;

	return (this.getName().equals(o.getName()) && this._actions.equals(o._actions));
    }

    @Override
    public String getActions() {
	return this._actions;
    }

    @Override
    public int hashCode() {
	return this.getName().hashCode() + this._actions.hashCode();
    }

    @Override
    public boolean implies(Permission p) {
	if (!(p instanceof PlainMessagePermission)) {
	    return false;
	}
	PlainMessagePermission mp = (PlainMessagePermission) p;

	if (checkAglet(this.getName(), mp.getName())) {
	    return matches(this._messages, mp._messages);
	} else {
	    return true;
	}
    }
}

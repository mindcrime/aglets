package com.ibm.aglets.security;

/*
 * @(#)ContextPermission.java
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
import java.util.Vector;

public class ContextPermission extends AgletsPermissionBase {
    String _actions;
    private Vector _actionList = new Vector();

    // should be implemented?
    // ==== public PermissionCollection newPermissionCollection();
    public ContextPermission(String name, String actions) {
	super(name);
	String[] actionList = split(actions.toLowerCase(), SEPARATORS);
	int i = 0;

	for (i = 0; i < actionList.length; i++) {
	    this._actionList.addElement(actionList[i]);
	}
	this._actions = concat(this._actionList);
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == this) {
	    return true;
	}
	if (!(obj instanceof ContextPermission)) {
	    return false;
	}
	ContextPermission o = (ContextPermission) obj;

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
	if (!(p instanceof ContextPermission)) {
	    return false;
	}
	ContextPermission cp = (ContextPermission) p;

	return matches(this.getName(), cp.getName())
		&& matches(this._actionList, cp._actionList);
    }
}

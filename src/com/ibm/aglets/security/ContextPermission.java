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
			_actionList.addElement(actionList[i]);
		} 
		_actions = concat(_actionList);
	}
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} 
		if (!(obj instanceof ContextPermission)) {
			return false;
		} 
		ContextPermission o = (ContextPermission)obj;

		return (getName().equals(o.getName()) && _actions.equals(o._actions));
	}
	public String getActions() {
		return _actions;
	}
	public int hashCode() {
		return getName().hashCode() + _actions.hashCode();
	}
	public boolean implies(Permission p) {
		if (!(p instanceof ContextPermission)) {
			return false;
		} 
		ContextPermission cp = (ContextPermission)p;

		return matches(getName(), cp.getName()) 
			   && matches(_actionList, cp._actionList);
	}
}

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

import java.security.Permission;

import java.io.Serializable;
import java.util.Vector;

public class PlainMessagePermission extends AgletsPermissionBase 
	implements Serializable {
	private String _actions = null;
	private Vector _messages = new Vector();

	// should be implemented?
	// ==== public PermissionCollection newPermissionCollection();
	public PlainMessagePermission(String name, String actions) {
		super(name);
		String[] actionList = split(actions.toLowerCase(), SEPARATORS);
		int i = 0;

		for (i = 0; i < actionList.length; i++) {
			_messages.addElement(actionList[i]);
		} 
		_actions = concat(_messages);
	}
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} 
		if (!(obj instanceof PlainMessagePermission)) {
			return false;
		} 
		PlainMessagePermission o = (PlainMessagePermission)obj;

		return (getName().equals(o.getName()) && _actions.equals(o._actions));
	}
	public String getActions() {
		return _actions;
	}
	public int hashCode() {
		return getName().hashCode() + _actions.hashCode();
	}
	public boolean implies(Permission p) {
		if (!(p instanceof PlainMessagePermission)) {
			return false;
		} 
		PlainMessagePermission mp = (PlainMessagePermission)p;

		if (checkAglet(getName(), mp.getName())) {
			return matches(_messages, mp._messages);
		} else {
			return true;
		}
	}
}

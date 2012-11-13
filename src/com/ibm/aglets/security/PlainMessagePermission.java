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
	/**
	 * 
	 */
	private static final long serialVersionUID = -6199236253073440162L;
	private String _actions = null;
	private final Vector _messages = new Vector();

	// should be implemented?
	// ==== public PermissionCollection newPermissionCollection();
	public PlainMessagePermission(final String name, final String actions) {
		super(name);
		final String[] actionList = split(actions.toLowerCase(), SEPARATORS);
		int i = 0;

		for (i = 0; i < actionList.length; i++) {
			_messages.addElement(actionList[i]);
		}
		_actions = concat(_messages);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof PlainMessagePermission)) {
			return false;
		}
		final PlainMessagePermission o = (PlainMessagePermission) obj;

		return (getName().equals(o.getName()) && _actions.equals(o._actions));
	}

	@Override
	public String getActions() {
		return _actions;
	}

	@Override
	public int hashCode() {
		return getName().hashCode() + _actions.hashCode();
	}

	@Override
	public boolean implies(final Permission p) {
		if (!(p instanceof PlainMessagePermission)) {
			return false;
		}
		final PlainMessagePermission mp = (PlainMessagePermission) p;

		if (checkAglet(getName(), mp.getName())) {
			return matches(_messages, mp._messages);
		} else {
			return true;
		}
	}
}

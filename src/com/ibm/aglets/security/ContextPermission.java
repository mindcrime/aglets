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
	/**
	 * 
	 */
	private static final long serialVersionUID = -1704237134466029296L;
	String _actions;
	private final Vector _actionList = new Vector();

	// should be implemented?
	// ==== public PermissionCollection newPermissionCollection();
	public ContextPermission(final String name, final String actions) {
		super(name);
		final String[] actionList = split(actions.toLowerCase(), SEPARATORS);
		int i = 0;

		for (i = 0; i < actionList.length; i++) {
			_actionList.addElement(actionList[i]);
		}
		_actions = concat(_actionList);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ContextPermission)) {
			return false;
		}
		final ContextPermission o = (ContextPermission) obj;

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
		if (!(p instanceof ContextPermission)) {
			return false;
		}
		final ContextPermission cp = (ContextPermission) p;

		return matches(getName(), cp.getName())
		&& matches(_actionList, cp._actionList);
	}
}

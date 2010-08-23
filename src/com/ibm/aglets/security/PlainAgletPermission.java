package com.ibm.aglets.security;

/*
 * @(#)PlainAgletPermission.java
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

public class PlainAgletPermission extends AgletsPermissionBase implements
Serializable {
    private static final String ACTION_DISPOSE = "dispose";
    private static final String ACTION_CLONE = "clone";
    private static final String ACTION_DISPATCH = "dispatch";
    private static final String ACTION_RETRACT = "retract";
    private static final String ACTION_DEACTIVATE = "deactivate";
    private static final String ACTION_ACTIVATE = "activate";

    private static final String[] ACTIONS = { ACTION_DISPOSE, ACTION_CLONE,
	ACTION_DISPATCH, ACTION_RETRACT, ACTION_DEACTIVATE, ACTION_ACTIVATE };

    private static final int INDEX_DISPOSE = 0;
    private static final int INDEX_CLONE = 1;
    private static final int INDEX_DISPATCH = 2;
    private static final int INDEX_RETRACT = 3;
    private static final int INDEX_DEACTIVATE = 4;
    private static final int INDEX_ACTIVATE = 5;

    private static final int NUMBER_OF_ACTIONS = 6;

    private String _actions = null;
    private boolean[] _actionFlag = new boolean[NUMBER_OF_ACTIONS];

    // should be implemented?
    // ==== public PermissionCollection newPermissionCollection();
    public PlainAgletPermission(String name, String actions) {
	super(name);
	String[] actionList = split(actions.toLowerCase(), SEPARATORS);
	int i = 0;

	for (i = 0; i < actionList.length; i++) {
	    if (actionList[i].equals(ACTION_DISPOSE)) {
		this._actionFlag[INDEX_DISPOSE] = true;
	    } else if (actionList[i].equals(ACTION_CLONE)) {
		this._actionFlag[INDEX_CLONE] = true;
	    } else if (actionList[i].equals(ACTION_DISPATCH)) {
		this._actionFlag[INDEX_DISPATCH] = true;
	    } else if (actionList[i].equals(ACTION_RETRACT)) {
		this._actionFlag[INDEX_RETRACT] = true;
	    } else if (actionList[i].equals(ACTION_DEACTIVATE)) {
		this._actionFlag[INDEX_DEACTIVATE] = true;
	    } else if (actionList[i].equals(ACTION_ACTIVATE)) {
		this._actionFlag[INDEX_ACTIVATE] = true;
	    }
	}
	this._actions = select(ACTIONS, this._actionFlag);
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == this) {
	    return true;
	}
	if (!(obj instanceof PlainAgletPermission)) {
	    return false;
	}
	PlainAgletPermission o = (PlainAgletPermission) obj;

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
	if (!(p instanceof PlainAgletPermission)) {
	    return false;
	}
	PlainAgletPermission ap = (PlainAgletPermission) p;

	if (checkAglet(this.getName(), ap.getName())) {
	    return matches(this._actionFlag, ap._actionFlag);
	} else {
	    return true;
	}
	// return checkAglet(getName(), ap.getName())
	// && matches(_actionFlag, ap._actionFlag);
    }
}

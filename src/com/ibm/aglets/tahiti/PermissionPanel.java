package com.ibm.aglets.tahiti;

/*
 * @(#)SecurityConfigDialog.java
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

import java.awt.TextField;
import java.util.Vector;

import com.ibm.aglets.security.PolicyFileParsingException;
import com.ibm.aglets.security.PolicyPermission;

class PermissionPanel extends EditorPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8376339851700076305L;
	protected static final String LABEL_ACTIONS = "Actions";
	private static final int LENGTH_ACTIONS = 15;

	public static final PolicyPermission toPermission(
	                                                  final String className,
	                                                  final String text) {
		PolicyPermission permission = null;

		try {
			permission = new PolicyPermission(className);
		} catch (final ClassNotFoundException excpt) {
			return null;
		}
		if (permission != null) {
			final Vector args = toVector(text);
			final int num = args.size();
			int idx = 0;

			for (idx = 0; idx < num; idx++) {
				final String str = (String) args.elementAt(idx);

				switch (idx) {
					case 0:
						if ((str != null) && !str.equals("")) {
							permission.setTargetName(str);
						}
						break;
					case 1:
						if ((str != null) && !str.equals("")) {
							permission.setActions(str);
						}
						break;
				}
			}
			try {
				permission.create();
			} catch (final PolicyFileParsingException excpt) {
				return null;
			} catch (final SecurityException excpt) {
				return null;
			}
		}
		return permission;
	}

	protected TextField actions = new TextField(LENGTH_ACTIONS);
}

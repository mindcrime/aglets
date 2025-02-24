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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.TextField;
import java.util.Vector;

class GeneralPermissionEditor extends PermissionEditor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4260455073544559761L;
	private static final String DEFAULT_LABEL_NAME = "Name";
	private static final int LENGTH_NAME = 15;

	private final TextField name = new TextField(LENGTH_NAME);

	GeneralPermissionEditor() {
		this(DEFAULT_LABEL_NAME);
	}

	GeneralPermissionEditor(final String labelName) {
		final GridBagLayout grid = new GridBagLayout();

		setLayout(grid);

		final GridBagConstraints cns = new GridBagConstraints();

		cns.weighty = 0.0;
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.ipadx = cns.ipady = 5;

		Label label = null;

		// name
		label = new Label(labelName);
		this.add(label);
		cns.weightx = 0.2;
		cns.gridwidth = GridBagConstraints.RELATIVE;
		grid.setConstraints(label, cns);

		this.add(name);
		cns.weightx = 1.0;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		grid.setConstraints(name, cns);

		// actions
		label = new Label(LABEL_ACTIONS);
		this.add(label);
		cns.weightx = 0.2;
		cns.gridwidth = GridBagConstraints.RELATIVE;
		grid.setConstraints(label, cns);

		this.add(actions);
		cns.weightx = 1.0;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		grid.setConstraints(actions, cns);
	}

	@Override
	public String getText() {
		final Vector args = new Vector();
		final String nam = name.getText();
		final String acts = actions.getText();
		final boolean n = (nam != null) && !nam.equals("");
		final boolean a = (acts != null) && !acts.equals("");

		if (n || a) {
			args.addElement(nam);
		}
		if (a) {
			args.addElement(acts);
		}
		return toText(args);
	}

	@Override
	public void setText(final String text) {
		this.parseText(text);
		final String nam = this.getArg(0);
		final String acts = this.getArg(1);

		if (nam != null) {
			name.setText(nam);
		} else {
			name.setText("");
		}
		if (acts != null) {
			actions.setText(acts);
		} else {
			actions.setText("");
		}
	}
}

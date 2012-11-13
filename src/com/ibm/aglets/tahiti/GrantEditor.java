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

import com.ibm.aglets.security.PolicyGrant;
import com.ibm.awb.misc.MalformedURIPatternException;

class GrantEditor extends EditorPanel implements Editor {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5058267678244617092L;
	private static final String LABEL_CODEBASE = "CodeBase";
	private static final int LENGTH_CODEBASE = 15;
	private static final String LABEL_SIGNEDBY = "Signed by";
	private static final int LENGTH_SIGNEDBY = 5;
	private static final String LABEL_OWNEDBY = "Owned by";
	private static final int LENGTH_OWNEDBY = 5;

	public static final PolicyGrant toGrant(final String text) {
		final Vector args = toVector(text);
		final int n = args.size();
		final PolicyGrant grant = new PolicyGrant();

		for (int i = 0; i < n; i++) {
			final String str = (String) args.elementAt(i);

			switch (i) {
				case 0:
					try {
						grant.setCodeBase(str);
					} catch (final MalformedURIPatternException excpt) {
						return null;
					}
					break;
				case 1:
					if ((str != null) && !str.equals("")) {
						grant.setSignerNames(str);
					}
					break;
				case 2:
					if ((str != null) && !str.equals("")) {
						grant.setOwnerNames(str);
					}
					break;
			}
		}
		return grant;
	}
	private final TextField codeBase = new TextField(LENGTH_CODEBASE);
	private final TextField signedBy = new TextField(LENGTH_SIGNEDBY);

	private final TextField ownedBy = new TextField(LENGTH_OWNEDBY);

	GrantEditor() {
		final GridBagLayout grid = new GridBagLayout();

		setLayout(grid);

		final GridBagConstraints cns = new GridBagConstraints();

		cns.weighty = 0.0;
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.ipadx = cns.ipady = 5;

		Label label = null;

		// code base
		label = new Label(LABEL_CODEBASE);
		this.add(label);
		cns.weightx = 0.2;
		grid.setConstraints(label, cns);

		this.add(codeBase);
		cns.weightx = 1.0;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		grid.setConstraints(codeBase, cns);

		// signed by
		label = new Label(LABEL_SIGNEDBY);
		this.add(label);
		cns.weightx = 0.2;
		cns.gridwidth = GridBagConstraints.RELATIVE;
		grid.setConstraints(label, cns);

		this.add(signedBy);
		cns.weightx = 1.0;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		grid.setConstraints(signedBy, cns);

		// owned by
		label = new Label(LABEL_OWNEDBY);
		this.add(label);
		cns.weightx = 0.2;
		cns.gridwidth = GridBagConstraints.RELATIVE;
		grid.setConstraints(label, cns);

		this.add(ownedBy);
		cns.weightx = 1.0;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		grid.setConstraints(ownedBy, cns);
	}

	@Override
	public String getText() {
		final Vector args = new Vector();
		final String codebase = codeBase.getText();
		final String signers = signedBy.getText();
		final String owners = ownedBy.getText();
		final boolean cb = (codebase != null) && !codebase.equals("");
		final boolean s = (signers != null) && !signers.equals("");
		final boolean o = (owners != null) && !owners.equals("");

		if (cb || s || o) {
			args.addElement(codebase);
		}
		if (s || o) {
			args.addElement(signers);
		}
		if (o) {
			args.addElement(owners);
		}
		return toText(args);
	}

	@Override
	public void setText(final String text) {
		this.parseText(text);
		final String codebase = this.getArg(0);
		final String signers = this.getArg(1);
		final String owners = this.getArg(2);

		if (codebase != null) {
			codeBase.setText(codebase);
		} else {
			codeBase.setText("");
		}
		if (signers != null) {
			signedBy.setText(signers);
		} else {
			signedBy.setText("");
		}
		if (owners != null) {
			ownedBy.setText(owners);
		} else {
			ownedBy.setText("");
		}
	}
}

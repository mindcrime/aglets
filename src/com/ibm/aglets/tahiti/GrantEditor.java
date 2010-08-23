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
    private static final String LABEL_CODEBASE = "CodeBase";
    private static final int LENGTH_CODEBASE = 15;
    private static final String LABEL_SIGNEDBY = "Signed by";
    private static final int LENGTH_SIGNEDBY = 5;
    private static final String LABEL_OWNEDBY = "Owned by";
    private static final int LENGTH_OWNEDBY = 5;

    private TextField codeBase = new TextField(LENGTH_CODEBASE);
    private TextField signedBy = new TextField(LENGTH_SIGNEDBY);
    private TextField ownedBy = new TextField(LENGTH_OWNEDBY);

    GrantEditor() {
	GridBagLayout grid = new GridBagLayout();

	this.setLayout(grid);

	GridBagConstraints cns = new GridBagConstraints();

	cns.weighty = 0.0;
	cns.fill = GridBagConstraints.HORIZONTAL;
	cns.ipadx = cns.ipady = 5;

	Label label = null;

	// code base
	label = new Label(LABEL_CODEBASE);
	this.add(label);
	cns.weightx = 0.2;
	grid.setConstraints(label, cns);

	this.add(this.codeBase);
	cns.weightx = 1.0;
	cns.gridwidth = GridBagConstraints.REMAINDER;
	grid.setConstraints(this.codeBase, cns);

	// signed by
	label = new Label(LABEL_SIGNEDBY);
	this.add(label);
	cns.weightx = 0.2;
	cns.gridwidth = GridBagConstraints.RELATIVE;
	grid.setConstraints(label, cns);

	this.add(this.signedBy);
	cns.weightx = 1.0;
	cns.gridwidth = GridBagConstraints.REMAINDER;
	grid.setConstraints(this.signedBy, cns);

	// owned by
	label = new Label(LABEL_OWNEDBY);
	this.add(label);
	cns.weightx = 0.2;
	cns.gridwidth = GridBagConstraints.RELATIVE;
	grid.setConstraints(label, cns);

	this.add(this.ownedBy);
	cns.weightx = 1.0;
	cns.gridwidth = GridBagConstraints.REMAINDER;
	grid.setConstraints(this.ownedBy, cns);
    }

    @Override
    public String getText() {
	Vector args = new Vector();
	final String codebase = this.codeBase.getText();
	final String signers = this.signedBy.getText();
	final String owners = this.ownedBy.getText();
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
    public void setText(String text) {
	this.parseText(text);
	final String codebase = this.getArg(0);
	final String signers = this.getArg(1);
	final String owners = this.getArg(2);

	if (codebase != null) {
	    this.codeBase.setText(codebase);
	} else {
	    this.codeBase.setText("");
	}
	if (signers != null) {
	    this.signedBy.setText(signers);
	} else {
	    this.signedBy.setText("");
	}
	if (owners != null) {
	    this.ownedBy.setText(owners);
	} else {
	    this.ownedBy.setText("");
	}
    }

    public static final PolicyGrant toGrant(String text) {
	Vector args = toVector(text);
	final int n = args.size();
	PolicyGrant grant = new PolicyGrant();

	for (int i = 0; i < n; i++) {
	    final String str = (String) args.elementAt(i);

	    switch (i) {
	    case 0:
		try {
		    grant.setCodeBase(str);
		} catch (MalformedURIPatternException excpt) {
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
}

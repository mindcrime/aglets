package com.ibm.aglets.tahiti;

/*
 * @(#)PropertiesDialog.java
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

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglets.AgletRuntime;
import com.ibm.aglets.ResourceManager;

// # import com.ibm.aglets.security.Allowance;

import java.awt.*;

import java.security.Identity;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;

class MyPanel extends BorderPanel {
	Vector v = new Vector();

	GridBagConstraints cns = new GridBagConstraints();
	GridBagLayout grid = new GridBagLayout();

	public MyPanel(String title) {
		super(title);
		setLayout(grid);

		cns.ipadx = cns.ipady = 5;
		cns.weightx = 1.0;
		cns.weighty = 1.0;
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.anchor = GridBagConstraints.EAST;
	}
	public void makeLabeledComponent(String lbl, Label field) {
		if (getComponentCount() == 0) {
			cns.insets = topInsets();
			cns.insets.bottom = bottomInsets().bottom;
		} else {
			cns.insets = bottomInsets();
		} 
		cns.gridwidth = 1;

		Label l = new Label(lbl);

		cns.weightx = 0.1;
		grid.setConstraints(l, cns);
		add(l);

		cns.weightx = 1.0;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		grid.setConstraints(field, cns);
		add(field);
		v.addElement(field);
	}
	public void paint(Graphics g) {
		Enumeration e = v.elements();

		g.setColor(getBackground());
		Dimension size = getSize();

		g.fillRect(0, 0, size.width, size.height);
		while (e.hasMoreElements()) {
			Component l = (Component)e.nextElement();
			Rectangle b = l.getBounds();

			g.draw3DRect(b.x - 1, b.y - 1, b.width + 1, b.height + 1, false);
		} 
		super.paint(g);
	}
}

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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Rectangle;
import java.util.Enumeration;
import java.util.Vector;

class MyPanel extends BorderPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6230438978399025011L;

	Vector v = new Vector();

	GridBagConstraints cns = new GridBagConstraints();
	GridBagLayout grid = new GridBagLayout();

	public MyPanel(final String title) {
		super(title);
		setLayout(grid);

		cns.ipadx = cns.ipady = 5;
		cns.weightx = 1.0;
		cns.weighty = 1.0;
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.anchor = GridBagConstraints.EAST;
	}

	public void makeLabeledComponent(final String lbl, final Label field) {
		if (getComponentCount() == 0) {
			cns.insets = topInsets();
			cns.insets.bottom = bottomInsets().bottom;
		} else {
			cns.insets = bottomInsets();
		}
		cns.gridwidth = 1;

		final Label l = new Label(lbl);

		cns.weightx = 0.1;
		grid.setConstraints(l, cns);
		this.add(l);

		cns.weightx = 1.0;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		grid.setConstraints(field, cns);
		this.add(field);
		v.addElement(field);
	}

	@Override
	public void paint(final Graphics g) {
		final Enumeration e = v.elements();

		g.setColor(getBackground());
		final Dimension size = this.getSize();

		g.fillRect(0, 0, size.width, size.height);
		while (e.hasMoreElements()) {
			final Component l = (Component) e.nextElement();
			final Rectangle b = l.getBounds();

			g.draw3DRect(b.x - 1, b.y - 1, b.width + 1, b.height + 1, false);
		}
		super.paint(g);
	}
}

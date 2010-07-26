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
    Vector v = new Vector();

    GridBagConstraints cns = new GridBagConstraints();
    GridBagLayout grid = new GridBagLayout();

    public MyPanel(String title) {
	super(title);
	this.setLayout(this.grid);

	this.cns.ipadx = this.cns.ipady = 5;
	this.cns.weightx = 1.0;
	this.cns.weighty = 1.0;
	this.cns.fill = GridBagConstraints.HORIZONTAL;
	this.cns.anchor = GridBagConstraints.EAST;
    }

    public void makeLabeledComponent(String lbl, Label field) {
	if (this.getComponentCount() == 0) {
	    this.cns.insets = this.topInsets();
	    this.cns.insets.bottom = this.bottomInsets().bottom;
	} else {
	    this.cns.insets = this.bottomInsets();
	}
	this.cns.gridwidth = 1;

	Label l = new Label(lbl);

	this.cns.weightx = 0.1;
	this.grid.setConstraints(l, this.cns);
	this.add(l);

	this.cns.weightx = 1.0;
	this.cns.gridwidth = GridBagConstraints.REMAINDER;
	this.grid.setConstraints(field, this.cns);
	this.add(field);
	this.v.addElement(field);
    }

    @Override
    public void paint(Graphics g) {
	Enumeration e = this.v.elements();

	g.setColor(this.getBackground());
	Dimension size = this.getSize();

	g.fillRect(0, 0, size.width, size.height);
	while (e.hasMoreElements()) {
	    Component l = (Component) e.nextElement();
	    Rectangle b = l.getBounds();

	    g.draw3DRect(b.x - 1, b.y - 1, b.width + 1, b.height + 1, false);
	}
	super.paint(g);
    }
}

package com.ibm.aglets.tahiti;

/*
 * @(#)GridBagPanel.java
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;

public class GridBagPanel extends Panel {
    public static final int RELATIVE = GridBagConstraints.RELATIVE;
    public static final int REMAINDER = GridBagConstraints.REMAINDER;

    public static final int NONE = GridBagConstraints.NONE;
    public static final int BOTH = GridBagConstraints.BOTH;
    public static final int HORIZONTAL = GridBagConstraints.HORIZONTAL;
    public static final int VERTICAL = GridBagConstraints.VERTICAL;

    public static final int CENTER = GridBagConstraints.CENTER;
    public static final int NORTH = GridBagConstraints.NORTH;
    public static final int NORTHEAST = GridBagConstraints.NORTHEAST;
    public static final int EAST = GridBagConstraints.EAST;
    public static final int SOUTHEAST = GridBagConstraints.SOUTHEAST;
    public static final int SOUTH = GridBagConstraints.SOUTH;
    public static final int SOUTHWEST = GridBagConstraints.SOUTHWEST;
    public static final int WEST = GridBagConstraints.WEST;
    public static final int NORTHWEST = GridBagConstraints.NORTHWEST;

    private GridBagLayout _grid = new GridBagLayout();
    private GridBagConstraints _cns = new GridBagConstraints();

    public GridBagPanel() {
	this.setLayout(this._grid);
    }

    @Override
    public Component add(Component c) {
	return this.add(c, this._cns);
    }

    @Override
    public Component add(Component c, int width) {
	this._cns.gridwidth = width;
	return this.add(c);
    }

    public Component add(Component c, int width, double weightx) {
	this._cns.gridwidth = width;
	this._cns.weightx = weightx;
	return this.add(c);
    }

    public Component add(Component c, int anchor, int fill) {
	this._cns.anchor = anchor;
	this._cns.fill = fill;
	return this.add(c);
    }

    public Component add(Component c, int anchor, int fill, int width) {
	this._cns.gridwidth = width;
	return this.add(c, anchor, fill);
    }

    protected Component add(Component c, GridBagConstraints cns) {
	this._grid.setConstraints(c, cns);
	return super.add(c);
    }

    public void addLabeled(String label, Component c) {
	this._cns.gridwidth = 1;
	this._cns.weightx = 0.1;
	this.add(new Label(label));

	this._cns.weightx = 1.0;
	this._cns.gridwidth = GridBagConstraints.REMAINDER;
	this.add(c);
    }

    public void setConstraints(GridBagConstraints cns) {
	this._cns = cns;
    }
}

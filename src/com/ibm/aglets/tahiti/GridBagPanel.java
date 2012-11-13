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
	/**
	 * 
	 */
	private static final long serialVersionUID = -6827460767013290471L;
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

	private final GridBagLayout _grid = new GridBagLayout();
	private GridBagConstraints _cns = new GridBagConstraints();

	public GridBagPanel() {
		setLayout(_grid);
	}

	@Override
	public Component add(final Component c) {
		return this.add(c, _cns);
	}

	protected Component add(final Component c, final GridBagConstraints cns) {
		_grid.setConstraints(c, cns);
		return super.add(c);
	}

	@Override
	public Component add(final Component c, final int width) {
		_cns.gridwidth = width;
		return this.add(c);
	}

	public Component add(final Component c, final int width, final double weightx) {
		_cns.gridwidth = width;
		_cns.weightx = weightx;
		return this.add(c);
	}

	public Component add(final Component c, final int anchor, final int fill) {
		_cns.anchor = anchor;
		_cns.fill = fill;
		return this.add(c);
	}

	public Component add(final Component c, final int anchor, final int fill, final int width) {
		_cns.gridwidth = width;
		return this.add(c, anchor, fill);
	}

	public void addLabeled(final String label, final Component c) {
		_cns.gridwidth = 1;
		_cns.weightx = 0.1;
		this.add(new Label(label));

		_cns.weightx = 1.0;
		_cns.gridwidth = GridBagConstraints.REMAINDER;
		this.add(c);
	}

	public void setConstraints(final GridBagConstraints cns) {
		_cns = cns;
	}
}

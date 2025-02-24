package com.ibm.aglets.tahiti;

/*
 * @(#)MemoryUsageDialog.java
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

import java.awt.Dimension;

class MemCanvas extends java.awt.Canvas {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1248194503958262102L;

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(100, 20);
	}

	@Override
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	@Override
	public void paint(final java.awt.Graphics g) {
		final java.awt.Rectangle rect = this.getBounds();
		final Runtime r = Runtime.getRuntime();
		final long total = r.totalMemory();
		final long freeMemory = r.freeMemory();
		final int usedWidth = (rect.width - (int) (rect.width * freeMemory / total));

		g.setColor(java.awt.Color.red);
		g.fillRect(0, 0, usedWidth, rect.height);
		g.setColor(java.awt.Color.blue);
		g.fillRect(usedWidth + 1, 0, rect.width, rect.height);
	}
}

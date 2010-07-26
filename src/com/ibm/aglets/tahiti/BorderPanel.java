package com.ibm.aglets.tahiti;

/*
 * @(#)BorderPanel.java
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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

/*
 * @version	1.00	$Date: 2009/07/28 07:04:53 $
 * @author      Yariv Aridor
 */
class BorderPanel extends GridBagPanel {
    private String _title = null;
    private FontMetrics _fm = null;
    private Rectangle _titleBounds = null;
    boolean _raised = false;

    BorderPanel() {
	this(null, false);
    }

    BorderPanel(String title) {
	this(title, false);
    }

    BorderPanel(String title, boolean raised) {
	super();
	this._title = title;
	this._raised = raised;
    }

    public Insets bottomInsets() {
	return new Insets(0, 5, 5, 5);
    }

    public Insets middleInsets() {
	return new Insets(0, 5, 0, 5);
    }

    @Override
    public void paint(Graphics g) {
	if ((this._fm == null) && (this._title != null)) {
	    Container c = this.getParent();

	    this._fm = c.getFontMetrics(c.getFont());
	    this._titleBounds = new Rectangle(20, 0, this._fm.stringWidth(this._title), this._fm.getHeight());
	}
	Dimension size = this.getSize();
	int y = this._fm.getHeight() / 2;

	g.setColor(this.getBackground());
	g.draw3DRect(0, y, size.width - 1, size.height - y - 1, this._raised);
	g.draw3DRect(1, y + 1, size.width - 3, size.height - y - 3, !this._raised);

	if (this._title != null) {
	    g.fillRect(this._titleBounds.x, this._titleBounds.y, this._titleBounds.width, this._titleBounds.height);
	    g.setColor(Color.black);
	    g.drawString(this._title, 5, this._fm.getAscent()
		    + this._fm.getLeading());
	}
    }

    public Insets topInsets() {
	if (this._fm == null) {
	    if (this.getFont() == null) {
		java.awt.Component c = this.getParent();

		while ((c instanceof java.awt.Window) == false) {
		    c = c.getParent();
		}

		// if (c.getPeer() == null) {
		if (c.isDisplayable() == false) {
		    c.addNotify();
		}
	    }
	    this._fm = this.getFontMetrics(this.getFont());
	    this._titleBounds = new Rectangle(5, 0, this._fm.stringWidth(this._title), this._fm.getHeight());
	}
	return new Insets(this._fm.getHeight(), 5, 0, 5);
    }
}

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

import java.awt.Panel;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.FontMetrics;
import java.awt.Graphics;

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
		_title = title;
		_raised = raised;
	}
	public Insets bottomInsets() {
		return new Insets(0, 5, 5, 5);
	}
	public Insets middleInsets() {
		return new Insets(0, 5, 0, 5);
	}
	public void paint(Graphics g) {
		if (_fm == null && _title != null) {
			Container c = getParent();

			_fm = c.getFontMetrics(c.getFont());
			_titleBounds = new Rectangle(20, 0, _fm.stringWidth(_title), 
										 _fm.getHeight());
		} 
		Dimension size = getSize();
		int y = _fm.getHeight() / 2;

		g.setColor(getBackground());
		g.draw3DRect(0, y, size.width - 1, size.height - y - 1, _raised);
		g.draw3DRect(1, y + 1, size.width - 3, size.height - y - 3, !_raised);

		if (_title != null) {
			g.fillRect(_titleBounds.x, _titleBounds.y, _titleBounds.width, 
					   _titleBounds.height);
			g.setColor(Color.black);
			g.drawString(_title, 5, _fm.getAscent() + _fm.getLeading());
		} 
	}
	public Insets topInsets() {
		if (_fm == null) {
			if (getFont() == null) {
				java.awt.Component c = getParent();

				while ((c instanceof java.awt.Window) == false) {
					c = c.getParent();
				} 

				// if (c.getPeer() == null) {
				if (c.isDisplayable() == false) {
					c.addNotify();
				} 
			} 
			_fm = getFontMetrics(getFont());
			_titleBounds = new Rectangle(5, 0, _fm.stringWidth(_title), 
										 _fm.getHeight());
		} 
		return new Insets(_fm.getHeight(), 5, 0, 5);
	}
}

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
import javax.swing.border.*;

/*
 * @version	1.00	$Date: 2009/07/27 10:31:40 $
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
		
		// construct the border
		TitledBorder border = new TitledBorder((AbstractBorder)new LineBorder(Color.BLUE,2));
		border.setTitle(title);
		border.setTitleColor(Color.BLUE);
		border.setTitlePosition(TitledBorder.TOP);
		border.setTitleJustification(TitledBorder.CENTER);
		this.setBorder(border);
	}
	public Insets bottomInsets() {
		return new Insets(0, 5, 5, 5);
	}
	public Insets middleInsets() {
		return new Insets(0, 5, 0, 5);
	}
	public Insets topInsets() {
		return new Insets(0, 5, 0, 5);
		
	}
	
}

package com.ibm.aglets.tahiti;

/*
 * @(#)TahitiWindow.java
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

import java.awt.*;
import java.awt.event.*;

public class TahitiWindow extends Frame {

	static final public String lineSeparator = "\n";

	private Panel buttonPanel = new Panel();
	private boolean shown = false;

	private Button _closeButton = null;

	protected TahitiWindow() {
		this("");
	}
	protected TahitiWindow(String title) {
		super(title);
		setLayout(new BorderLayout());
		setTitle(title);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		add("South", buttonPanel);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (TahitiWindow.this.windowClosing(e)) {
					dispose();
				} else {
					setVisible(false);
				} 
			} 
		});

	}
	public Button addButton(String name) {
		Button b = new Button(name);

		buttonPanel.add(b);
		return b;
	}
	protected Button addButton(String name, ActionListener listener) {
		Button b = addButton(name);

		b.setActionCommand(name);
		b.addActionListener(listener);
		return b;
	}
	protected Button addButton(String name, ActionListener actionListener, 
							   KeyListener keyListener) {
		Button b = addButton(name);

		b.setActionCommand(name);
		b.addActionListener(actionListener);
		b.addKeyListener(keyListener);
		return b;
	}
	protected void addCloseButton(String name) {
		class AlertCloseListener extends ActionAndKeyListener {
			Frame target;

			AlertCloseListener(Frame d) {
				target = d;
			}
			protected void doAction() {
				target.dispose();
				closeButtonPressed();
			} 
		}
		if (name == null) {
			name = "Close";
		} 
		ActionAndKeyListener listener = new AlertCloseListener(this);

		_closeButton = addButton(name, listener, listener);
	}
	public void beep() {
		getToolkit().beep();
	}
	protected void closeButtonPressed() {}
	public void popup() {
		if (shown == false) {
			pack();
			shown = true;
		} 
		show();
	}
	public void popupAtCenterOfScreen() {
		if (shown == false) {
			pack();
			shown = true;
		} 
		Dimension d = getToolkit().getScreenSize();
		Dimension s = getSize();
		Point p = new Point((d.width - s.width) / 2, 
							(d.height - s.height) / 2);

		setLocation(p);
		show();

		// Due to a bug in AIX JDK or Motif/AWT?
		setLocation(p.x - 1, p.y - 1);
	}
	protected boolean windowClosing(WindowEvent e) {
		return true;
	}
}

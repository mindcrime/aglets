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
import java.util.ResourceBundle;

import javax.swing.*;

import com.ibm.aglets.tahiti.utils.IconRepository;

public class TahitiWindow extends JFrame {

    /* Load resources */
    static ResourceBundle bundle = null;
	static {
		bundle = ResourceBundle.getBundle("tahiti");
	} 
	
    
	static final public String lineSeparator = "\n";

	private JPanel buttonPanel = new JPanel();
	private boolean shown = false;

	private JButton _closeButton = null;

	protected TahitiWindow() {
		this("");
	}
	protected TahitiWindow(String title) {
		super(title);
		//this.setIconImage(Toolkit.getDefaultToolkit().getImage(System.getProperty("aglets.home"+"/icons/aglets.jpg")));
		this.getContentPane().setLayout(new BorderLayout());
		setTitle(title);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		this.getContentPane().add("South", buttonPanel);

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
	public JButton addButton(String name) {
		JButton b = new JButton(name);

		buttonPanel.add(b);
		return b;
	}
	protected JButton addButton(String name, ActionListener listener) {
		JButton b = addButton(name);

		b.setActionCommand(name);
		b.addActionListener(listener);
		return b;
	}
	protected JButton addButton(String name, ActionListener actionListener, 
							   KeyListener keyListener) {
		JButton b = addButton(name);

		b.setActionCommand(name);
		b.addActionListener(actionListener);
		b.addKeyListener(keyListener);
		return b;
	}
	protected JButton addButton(String name, ActionListener actionListener,
	        										KeyListener keyListener, String icon){
	    JButton b = addButton(name);
	    b.setIcon(IconRepository.getIcon(icon));
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

		_closeButton = addButton(name, listener, listener,"cancel");
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
			shown = true;
		} 
		Dimension d = getToolkit().getScreenSize();
		Dimension s = getSize();
		Point p = new Point((d.width - s.width) / 2, 
							(d.height - s.height) / 2);

		setLocation(p);
		this.setVisible(true);

		// Due to a bug in AIX JDK or Motif/AWT?
		setLocation(p.x - 1, p.y - 1);
	}
	protected boolean windowClosing(WindowEvent e) {
		return true;
	}
	
	/**
	 * A method to add a jbutton to the window with the name, icon, actionstring, etc. specified.
	 * The button is automatically added to the south panel.
	 *@param label the label of the button
	 *@param action the actionstring of the button
	 *@param icon the icon of the button
	 *@param listener the actionlistener for this button
	 *@param tooltip the tooltip text for this button
	 */
	protected JButton addJButton(String label, String action, Icon icon, ActionListener listener,String tooltip){
	    if(label==null ){
	        return null;
	    }
	    
	    if(action==null || action.equals("")){
	        action = label;
	    }
	    
	    if(tooltip==null || tooltip.equals("")){
	        tooltip = label;
	    }
	    
	    JButton ret = new JButton(label);
	    ret.setActionCommand(action);
	    ret.setIcon(icon);
	    ret.addActionListener(listener);
	    ret.setToolTipText(tooltip);
	    
	    this.buttonPanel.add("South",ret);
	    return ret;
	}

}

package com.ibm.aglets.tahiti;

/*
 * @(#)ActionAndKeyListener.java
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

import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

abstract public class ActionAndKeyListener implements ActionListener, 
		KeyListener {
	public void actionPerformed(ActionEvent ev) {
		doAction();
	}
	abstract protected void doAction();
	public void keyPressed(KeyEvent ev) {
		if (ev.getKeyCode() == KeyEvent.VK_ENTER) {
			doAction();
		} 
	}
	public void keyReleased(KeyEvent ev) {

		// do nothing
	}
	public void keyTyped(KeyEvent ev) {

		// do nothing
	}
}

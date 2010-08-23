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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

abstract public class ActionAndKeyListener implements ActionListener,
KeyListener {
    @Override
    public void actionPerformed(ActionEvent ev) {
	this.doAction();
    }

    abstract protected void doAction();

    @Override
    public void keyPressed(KeyEvent ev) {
	if (ev.getKeyCode() == KeyEvent.VK_ENTER) {
	    this.doAction();
	}
    }

    @Override
    public void keyReleased(KeyEvent ev) {

	// do nothing
    }

    @Override
    public void keyTyped(KeyEvent ev) {

	// do nothing
    }
}

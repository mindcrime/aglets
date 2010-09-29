package com.ibm.aglet.util;

/*
 * @(#)AddressChooser.java
 * 
 * (c) Copyright IBM Corp. 1996
 * 
 * IBM grants you a non-exclusive, non-transferrable License to
 * use this program internally solely for the purposes of testing
 * and evaluating Java Aglet API.
 * You may not distribute, sublicense, lease, rent or use this
 * sample program externally.
 * 
 * THIS ROGRAM IS PROVIDED "AS IS" WITHOUT ANY WARRANTY EXPRESS OR
 * IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE WARRANTY OF
 * NON-INFRINGEMENT AND THE WARRANTIES OF MERCHANTIBILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 * IBM WILL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY YOU AS
 * A RESULT OF USING THIS SAMPLE PROGRAM. IN NO EVENT WILL IBM BE
 * LIABLE FOR ANY SPECIAL, INDIRECT CONSEQUENTIAL DAMAGES OR LOST
 * PROFITS EVEN IF IBM HAS BEEN ADVISED OF THE POSSIBILITY OF THEIR
 * OCCURRENCE OR LOSS OF OR DAMAGE TO YOUR RECORDS OR DATA.
 * IBM WILL NOT BE LIABLE FOR ANY THIRD PARTY CLAIMS AGAINST YOU.
 */

import java.awt.AWTEventMulticaster;
import java.awt.Button;
import java.awt.Component;
import java.awt.Event;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.sourceforge.aglets.log.AgletsLogger;

/**
 * @version 1.50 $Date: 2009/07/28 07:04:54 $
 * @author Mitsuru Oshima
 */
public class AddressChooser extends Panel implements ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = 6596959439213355530L;

    private static AgletsLogger logger = AgletsLogger.getLogger(AddressChooser.class.getName());

    private transient TextField address;
    private transient AddressBook addressbook = null;

    private Button button = new Button("AddressBook");
    private GridBagLayout layout = new GridBagLayout();
    private ActionListener actionListener;
    private String command = "address";

    /**
     * Constructs a new AddressChooser with the default number of colums. The
     * default nubmer is 10.
     * 
     * @param columns
     *            the number of columns
     */
    public AddressChooser() {
	this(10);
    }

    /**
     * Constructs a new AddressChooser with the specified number of colums.
     * 
     * @param columns
     *            the number of columns
     */
    public AddressChooser(int columns) {
	this.setLayout(this.layout);
	GridBagConstraints cns = new GridBagConstraints();

	cns.gridwidth = 1;
	cns.fill = GridBagConstraints.NONE;
	this.addCmp(this.button, cns);

	this.addCmp(new Label("Address:"), cns);

	cns.fill = GridBagConstraints.HORIZONTAL;
	cns.gridwidth = GridBagConstraints.REMAINDER;
	cns.weightx = 1.0;
	this.address = new TextField(columns);
	this.addCmp(this.address, cns);

	this.button.setActionCommand("toggle");

	this.button.addActionListener(this);
	this.address.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
	String cmd = ev.getActionCommand();

	if ("toggle".equals(cmd)) {
	    if (this.addressbook == null) {
		Component c = this.button.getParent();

		while ((c instanceof Frame) == false) {
		    c = c.getParent();
		}
		this.addressbook = new AddressBook((Frame) c, this);

		// to get around a bugs of AWTMotif.
		this.addressbook.setSize(200, 200);
		this.addressbook.pack();
	    }
	    if (this.addressbook.isVisible() == false) {
		this.addressbook.popup(this.button);
	    } else {
		this.addressbook.setVisible(false);
	    }

	    // Open AddressBook
	} else if (this.address == ev.getSource()) {
	    logger.debug("selected = " + this.address.getText());

	    ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, this.command);

	    this.processEvent(e);
	}
    }

    /**
     * Adds the specified action listener to receive action events from this
     * chooser.
     * 
     * @param l
     *            the action listener
     */
    public void addActionListener(ActionListener l) {
	this.actionListener = AWTEventMulticaster.add(this.actionListener, l);
    }

    private void addCmp(Component c, GridBagConstraints cns) {
	this.layout.setConstraints(c, cns);
	this.add(c);
    }

    /* package */
    void addressSelected(String newAddress) {
	this.address.setText(newAddress);
	this.processActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, this.command));
    }

    /**
     * Get the address which is currently chosen by this chooser.
     */
    public String getAddress() {
	return this.address.getText();
    }

    @Override
    public boolean handleEvent(Event ev) {
	if ((ev.id == Event.LOST_FOCUS) && (this.isVisible() == false)
		&& (this.addressbook != null)) {
	    this.addressbook.setVisible(false);
	}
	if ((ev.id == Event.GOT_FOCUS) || (ev.id == Event.LOST_FOCUS)
		|| (ev.id == Event.MOUSE_ENTER)) {
	    if ((this.addressbook != null) && this.addressbook.isVisible()) {
		this.addressbook.adjust();
		this.addressbook.toFront();
	    }
	    return true;
	} else {
	    return super.handleEvent(ev);
	}
    }

    private void processActionEvent(ActionEvent ev) {
	if (this.actionListener != null) {
	    this.actionListener.actionPerformed(ev);
	}
    }

    /**
     * Removes the specified action listener so it no longer receives action
     * events from this chooser.
     * 
     * @param l
     *            the action listener
     */
    public void removeActionListener(ActionListener l) {
	this.actionListener = AWTEventMulticaster.remove(this.actionListener, l);
    }

    @Override
    synchronized public void removeNotify() {
	if (this.addressbook != null) {
	    this.addressbook.dispose();
	    this.addressbook = null;
	}
	super.removeNotify();
    }

    /**
     * Sets the command name of the action event fired by this chooser. By
     * default this will be set to the "address".
     */
    public void setActionCommand(String cmd) {
	this.command = cmd;
    }

    /**
     * Set the specified string as to the address book.
     */
    public void setAddress(String addr) {
	this.address.setText(addr);
    }
}

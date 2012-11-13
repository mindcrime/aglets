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

	private final Button button = new Button("AddressBook");
	private final GridBagLayout layout = new GridBagLayout();
	private ActionListener actionListener;
	private String command = "address";

	/**
	 * Constructs a new AddressChooser with the default number of colums. The
	 * default number is 10.
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
	public AddressChooser(final int columns) {
		setLayout(layout);
		final GridBagConstraints cns = new GridBagConstraints();

		cns.gridwidth = 1;
		cns.fill = GridBagConstraints.NONE;
		addCmp(button, cns);

		addCmp(new Label("Address:"), cns);

		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.weightx = 1.0;
		address = new TextField(columns);
		addCmp(address, cns);

		button.setActionCommand("toggle");

		button.addActionListener(this);
		address.addActionListener(this);
	}

	@Override
	public void actionPerformed(final ActionEvent ev) {
		final String cmd = ev.getActionCommand();

		if ("toggle".equals(cmd)) {
			if (addressbook == null) {
				Component c = button.getParent();

				while ((c instanceof Frame) == false) {
					c = c.getParent();
				}
				addressbook = new AddressBook((Frame) c, this);

				// to get around a bugs of AWTMotif.
				addressbook.setSize(200, 200);
				addressbook.pack();
			}
			if (addressbook.isVisible() == false) {
				addressbook.popup(button);
			} else {
				addressbook.setVisible(false);
			}

			// Open AddressBook
		} else if (address == ev.getSource()) {
			logger.debug("selected = " + address.getText());

			final ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command);

			processEvent(e);
		}
	}

	/**
	 * Adds the specified action listener to receive action events from this
	 * chooser.
	 * 
	 * @param l
	 *            the action listener
	 */
	public void addActionListener(final ActionListener l) {
		actionListener = AWTEventMulticaster.add(actionListener, l);
	}

	private void addCmp(final Component c, final GridBagConstraints cns) {
		layout.setConstraints(c, cns);
		this.add(c);
	}

	/* package */
	void addressSelected(final String newAddress) {
		address.setText(newAddress);
		processActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command));
	}

	/**
	 * Get the address which is currently chosen by this chooser.
	 */
	public String getAddress() {
		return address.getText();
	}

	@Override
	public boolean handleEvent(final Event ev) {
		if ((ev.id == Event.LOST_FOCUS) && (isVisible() == false)
				&& (addressbook != null)) {
			addressbook.setVisible(false);
		}
		if ((ev.id == Event.GOT_FOCUS) || (ev.id == Event.LOST_FOCUS)
				|| (ev.id == Event.MOUSE_ENTER)) {
			if ((addressbook != null) && addressbook.isVisible()) {
				addressbook.adjust();
				addressbook.toFront();
			}
			return true;
		} else {
			return super.handleEvent(ev);
		}
	}

	private void processActionEvent(final ActionEvent ev) {
		if (actionListener != null) {
			actionListener.actionPerformed(ev);
		}
	}

	/**
	 * Removes the specified action listener so it no longer receives action
	 * events from this chooser.
	 * 
	 * @param l
	 *            the action listener
	 */
	public void removeActionListener(final ActionListener l) {
		actionListener = AWTEventMulticaster.remove(actionListener, l);
	}

	@Override
	synchronized public void removeNotify() {
		if (addressbook != null) {
			addressbook.dispose();
			addressbook = null;
		}
		super.removeNotify();
	}

	/**
	 * Sets the command name of the action event fired by this chooser. By
	 * default this will be set to the "address".
	 */
	public void setActionCommand(final String cmd) {
		command = cmd;
	}

	/**
	 * Set the specified string as to the address book.
	 */
	public void setAddress(final String addr) {
		address.setText(addr);
	}
}

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

import java.awt.*;
import java.awt.event.*;
import com.ibm.aglet.AgletContext;
import org.aglets.log.*;

/**
 * @version     1.50    $Date: 2002/01/19 22:10:43 $
 * @author	Mitsuru Oshima
 */
public class AddressChooser extends Panel implements ActionListener {
    static LogCategory logCategory = LogInitializer.getCategory("com.ibm.aglet.util.AddressChooser");
    
	private transient TextField address;
	private transient AddressBook addressbook = null;

	private Button button = new Button("AddressBook");
	private GridBagLayout layout = new GridBagLayout();
	private ActionListener actionListener;
	private String command = "address";

	/**
	 * Constructs a new AddressChooser with the default number of colums.
	 * The default nubmer is 10.
	 * @param columns the number of columns
	 */
	public AddressChooser() {
		this(10);
	}
	/**
	 * Constructs a new AddressChooser with the specified number of colums.
	 * @param columns the number of columns
	 */
	public AddressChooser(int columns) {
		setLayout(layout);
		GridBagConstraints cns = new GridBagConstraints();

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
	public void actionPerformed(ActionEvent ev) {
		String cmd = ev.getActionCommand();

		if ("toggle".equals(cmd)) {
			if (addressbook == null) {
				Component c = button.getParent();

				while (c instanceof Frame == false) {
					c = c.getParent();
				} 
				addressbook = new AddressBook((Frame)c, this);

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
			logCategory.debug("selected = " + address.getText());

			ActionEvent e = new ActionEvent(this, 
											ActionEvent.ACTION_PERFORMED, 
											command);

			processEvent(e);
		} 
	}
	/**
	 * Adds the specified action listener to receive action events
	 * from this chooser.
	 * @param l the action listener
	 */
	public void addActionListener(ActionListener l) {
		actionListener = AWTEventMulticaster.add(actionListener, l);
	}
	private void addCmp(Component c, GridBagConstraints cns) {
		layout.setConstraints(c, cns);
		add(c);
	}
	/* package */
	void addressSelected(String newAddress) {
		address.setText(newAddress);
		processActionEvent(new ActionEvent(this, 
										   ActionEvent.ACTION_PERFORMED, 
										   command));
	}
	/**
	 * Get the address which is currently chosen by this chooser.
	 */
	public String getAddress() {
		return address.getText();
	}
	public boolean handleEvent(Event ev) {
		if (ev.id == Event.LOST_FOCUS && isVisible() == false 
				&& addressbook != null) {
			addressbook.setVisible(false);
		} 
		if (ev.id == Event.GOT_FOCUS || ev.id == Event.LOST_FOCUS 
				|| ev.id == Event.MOUSE_ENTER) {
			if (addressbook != null && addressbook.isVisible()) {
				addressbook.adjust();
				addressbook.toFront();
			} 
			return true;
		} else {
			return super.handleEvent(ev);
		}
	}
	private void processActionEvent(ActionEvent ev) {
		if (actionListener != null) {
			actionListener.actionPerformed(ev);
		} 
	}
	/**
	 * Removes the specified action listener so it no longer receives
	 * action events from this chooser.
	 * @param l the action listener
	 */
	public void removeActionListener(ActionListener l) {
		actionListener = AWTEventMulticaster.remove(actionListener, l);
	}
	synchronized public void removeNotify() {
		if (addressbook != null) {
			addressbook.dispose();
			addressbook = null;
		} 
		super.removeNotify();
	}
	/**
	 * Sets the command name of the action event fired by this chooser.
	 * By default this will be set to the "address".
	 */
	public void setActionCommand(String cmd) {
		command = cmd;
	}
	/**
	 * Set the specified string as to the address book.
	 */
	public void setAddress(String addr) {
		address.setText(addr);
	}
}

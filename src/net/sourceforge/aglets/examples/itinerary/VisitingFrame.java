package net.sourceforge.aglets.examples.itinerary;

/*
 * @(#)VisitingAglet.java
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The program is provided "as is" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 */

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import com.ibm.aglet.util.AddressChooser;

class VisitingFrame extends Frame implements WindowListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7145013172086931418L;
	VisitingAglet aglet;
	List list = new List(10, false);
	AddressChooser address = new AddressChooser(15);

	VisitingFrame(final VisitingAglet a) {
		aglet = a;

		addWindowListener(this);

		setLayout(new BorderLayout());
		this.add("Center", list);

		Panel p = new Panel();

		p.setLayout(new FlowLayout());
		p.add(address);
		final Button ad = new Button("Add");
		final Button remove = new Button("Remove");

		ad.addActionListener(this);
		remove.addActionListener(this);
		p.add(ad);
		p.add(remove);
		this.add("North", p);

		p = new Panel();
		p.setLayout(new FlowLayout());
		final Button start = new Button("Start!");

		start.addActionListener(this);
		p.add(start);
		this.add("South", p);

		this.update();
	}

	/**
	 * Handles the action event
	 * 
	 * @param ae
	 *            the event to be handled
	 */
	@Override
	public void actionPerformed(final ActionEvent ae) {
		if ("Add".equals(ae.getActionCommand())) {
			aglet.addresses.addElement(address.getAddress());
			this.update();
		} else if ("Remove".equals(ae.getActionCommand())) {
			final int i = list.getSelectedIndex();

			if (i >= 0) {
				aglet.addresses.removeElementAt(i);
				list.remove(i);
			}
		} else if ("Start!".equals(ae.getActionCommand())) {
			aglet.start();
		}
	}

	private void update() {
		list.removeAll();
		final Vector addrs = aglet.addresses;
		final int size = addrs.size();

		for (int i = 0; i < size; i++) {
			list.add((String) addrs.elementAt(i));
		}
	}

	@Override
	public void windowActivated(final WindowEvent we) {
	}

	@Override
	public void windowClosed(final WindowEvent we) {
	}

	/**
	 * Handles the window event
	 * 
	 * @param we
	 *            the event to be handled
	 */

	@Override
	public void windowClosing(final WindowEvent we) {
		dispose();
	}

	@Override
	public void windowDeactivated(final WindowEvent we) {
	}

	@Override
	public void windowDeiconified(final WindowEvent we) {
	}

	@Override
	public void windowIconified(final WindowEvent we) {
	}

	@Override
	public void windowOpened(final WindowEvent we) {
	}
}

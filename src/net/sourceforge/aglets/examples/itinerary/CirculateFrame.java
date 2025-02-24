package net.sourceforge.aglets.examples.itinerary;

/*
 * @(#)CirculateAglet.java
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
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import com.ibm.aglet.util.AddressChooser;
import com.ibm.agletx.util.SeqPlanItinerary;

class CirculateFrame extends Frame implements WindowListener, ActionListener,
ItemListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7817874648592803180L;
	CirculateAglet aglet;
	List list = new List(10, false);
	AddressChooser address = new AddressChooser(15);
	Choice choice = new Choice();
	Checkbox check = new Checkbox("Repeat");

	CirculateFrame(final CirculateAglet a) {
		aglet = a;
		setLayout(new BorderLayout());
		this.add("Center", list);
		addWindowListener(this);
		check.addItemListener(this);

		Panel p = new Panel();

		p.setLayout(new FlowLayout());
		p.add(address);
		p.add(choice);

		final Button ad = new Button("Add");
		final Button remove = new Button("Remove");

		ad.addActionListener(this);
		remove.addActionListener(this);
		p.add(ad);
		p.add(remove);
		this.add("North", p);

		p = new Panel();
		p.setLayout(new FlowLayout());
		p.add(check);

		final Button once = new Button("Once More");
		final Button start = new Button("Start!");

		once.addActionListener(this);
		start.addActionListener(this);
		p.add(once);
		p.add(start);
		this.add("South", p);

		choice.addItem("getLocalInfo");
		choice.addItem("getProxies");
		choice.addItem("printResult");

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
		if ("Once More".equals(ae.getActionCommand())) {
			aglet.oncemore();
		} else if ("Start!".equals(ae.getActionCommand())) {
			aglet.start();
		} else if ("Remove".equals(ae.getActionCommand())) {
			final int i = list.getSelectedIndex();

			if (i >= 0) {
				aglet.itinerary.removePlanAt(i);
				list.remove(i);
			}
		} else if ("Add".equals(ae.getActionCommand())) {
			aglet.itinerary.addPlan(address.getAddress(), choice.getSelectedItem());
			this.update();
		}
	}

	@Override
	public void itemStateChanged(final ItemEvent ie) {
		aglet.itinerary.setRepeat(check.getState());
	}

	/*
	 * public boolean handleEvent(java.awt.Event ev) { if (ev.id ==
	 * java.awt.Event.WINDOW_DESTROY) { dispose(); return true; } return
	 * super.handleEvent(ev); }
	 * 
	 * public boolean action(java.awt.Event ev, Object obj) { if (ev.target
	 * instanceof java.awt.Button) { Button b = (Button)ev.target; String l =
	 * b.getLabel(); if ("Once More".equals(l)) { aglet.oncemore(); } else if
	 * ("Start!".equals(l)) { aglet.start(); } else if ("Remove".equals(l)) {
	 * int i = list.getSelectedIndex(); if (i>=0) {
	 * aglet.itinerary.removePlanAt(i); list.delItem(i); } } else if
	 * ("Add".equals(l)){ aglet.itinerary.addPlan(address.getAddress(),
	 * choice.getSelectedItem()); update(); } return true; } else if (ev.target
	 * instanceof java.awt.Checkbox) {
	 * aglet.itinerary.setRepeat(check.getState()); } return false; }
	 */
	private void update() {
		list.removeAll();
		final SeqPlanItinerary spi = aglet.itinerary;
		final int size = spi.size();

		for (int i = 0; i < size; i++) {
			final String s = spi.getAddressAt(i) + " : "
			+ spi.getMessageAt(i).getKind();

			list.add(s);
		}
		check.setState(aglet.itinerary.isRepeat());
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

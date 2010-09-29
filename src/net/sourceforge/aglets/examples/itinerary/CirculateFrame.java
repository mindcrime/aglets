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

    CirculateFrame(CirculateAglet a) {
	this.aglet = a;
	this.setLayout(new BorderLayout());
	this.add("Center", this.list);
	this.addWindowListener(this);
	this.check.addItemListener(this);

	Panel p = new Panel();

	p.setLayout(new FlowLayout());
	p.add(this.address);
	p.add(this.choice);

	Button ad = new Button("Add");
	Button remove = new Button("Remove");

	ad.addActionListener(this);
	remove.addActionListener(this);
	p.add(ad);
	p.add(remove);
	this.add("North", p);

	p = new Panel();
	p.setLayout(new FlowLayout());
	p.add(this.check);

	Button once = new Button("Once More");
	Button start = new Button("Start!");

	once.addActionListener(this);
	start.addActionListener(this);
	p.add(once);
	p.add(start);
	this.add("South", p);

	this.choice.addItem("getLocalInfo");
	this.choice.addItem("getProxies");
	this.choice.addItem("printResult");

	this.update();
    }

    /**
     * Handles the action event
     * 
     * @param ae
     *            the event to be handled
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
	if ("Once More".equals(ae.getActionCommand())) {
	    this.aglet.oncemore();
	} else if ("Start!".equals(ae.getActionCommand())) {
	    this.aglet.start();
	} else if ("Remove".equals(ae.getActionCommand())) {
	    int i = this.list.getSelectedIndex();

	    if (i >= 0) {
		this.aglet.itinerary.removePlanAt(i);
		this.list.remove(i);
	    }
	} else if ("Add".equals(ae.getActionCommand())) {
	    this.aglet.itinerary.addPlan(this.address.getAddress(), this.choice.getSelectedItem());
	    this.update();
	}
    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
	this.aglet.itinerary.setRepeat(this.check.getState());
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
	this.list.removeAll();
	SeqPlanItinerary spi = this.aglet.itinerary;
	int size = spi.size();

	for (int i = 0; i < size; i++) {
	    String s = spi.getAddressAt(i) + " : "
	    + spi.getMessageAt(i).getKind();

	    this.list.add(s);
	}
	this.check.setState(this.aglet.itinerary.isRepeat());
    }

    @Override
    public void windowActivated(WindowEvent we) {
    }

    @Override
    public void windowClosed(WindowEvent we) {
    }

    /**
     * Handles the window event
     * 
     * @param we
     *            the event to be handled
     */

    @Override
    public void windowClosing(WindowEvent we) {
	this.dispose();
    }

    @Override
    public void windowDeactivated(WindowEvent we) {
    }

    @Override
    public void windowDeiconified(WindowEvent we) {
    }

    @Override
    public void windowIconified(WindowEvent we) {
    }

    @Override
    public void windowOpened(WindowEvent we) {
    }
}

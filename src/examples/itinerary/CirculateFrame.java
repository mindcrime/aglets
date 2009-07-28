package examples.itinerary;

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

import com.ibm.aglet.*;
import com.ibm.aglet.util.*;
import com.ibm.agletx.util.SeqPlanItinerary;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.*;
import java.awt.event.*;
class CirculateFrame extends Frame implements WindowListener, ActionListener, 
		ItemListener {
	CirculateAglet aglet;
	List list = new List(10, false);
	AddressChooser address = new AddressChooser(15);
	Choice choice = new Choice();
	Checkbox check = new Checkbox("Repeat");

	CirculateFrame(CirculateAglet a) {
		aglet = a;
		setLayout(new BorderLayout());
		add("Center", list);
		addWindowListener(this);
		check.addItemListener(this);

		Panel p = new Panel();

		p.setLayout(new FlowLayout());
		p.add(address);
		p.add(choice);

		Button ad = new Button("Add");
		Button remove = new Button("Remove");

		ad.addActionListener(this);
		remove.addActionListener(this);
		p.add(ad);
		p.add(remove);
		add("North", p);

		p = new Panel();
		p.setLayout(new FlowLayout());
		p.add(check);

		Button once = new Button("Once More");
		Button start = new Button("Start!");

		once.addActionListener(this);
		start.addActionListener(this);
		p.add(once);
		p.add(start);
		add("South", p);


		choice.addItem("getLocalInfo");
		choice.addItem("getProxies");
		choice.addItem("printResult");

		update();
	}
	/**
	 * Handles the action event
	 * @param ae the event to be handled
	 */
	public void actionPerformed(ActionEvent ae) {
		if ("Once More".equals(ae.getActionCommand())) {
			aglet.oncemore();
		} else if ("Start!".equals(ae.getActionCommand())) {
			aglet.start();
		} else if ("Remove".equals(ae.getActionCommand())) {
			int i = list.getSelectedIndex();

			if (i >= 0) {
				aglet.itinerary.removePlanAt(i);
				list.remove(i);
			} 
		} else if ("Add".equals(ae.getActionCommand())) {
			aglet.itinerary.addPlan(address.getAddress(), 
									choice.getSelectedItem());
			update();
		} 
	}
	public void itemStateChanged(ItemEvent ie) {
		aglet.itinerary.setRepeat(check.getState());
	}
	/*
	 * public boolean handleEvent(java.awt.Event ev) {
	 * if (ev.id == java.awt.Event.WINDOW_DESTROY) {
	 * dispose();
	 * return true;
	 * }
	 * return super.handleEvent(ev);
	 * }
	 * 
	 * public boolean action(java.awt.Event ev, Object obj) {
	 * if (ev.target instanceof java.awt.Button) {
	 * Button b = (Button)ev.target;
	 * String l = b.getLabel();
	 * if ("Once More".equals(l)) {
	 * aglet.oncemore();
	 * } else if ("Start!".equals(l)) {
	 * aglet.start();
	 * } else if ("Remove".equals(l)) {
	 * int i = list.getSelectedIndex();
	 * if (i>=0) {
	 * aglet.itinerary.removePlanAt(i);
	 * list.delItem(i);
	 * }
	 * } else if ("Add".equals(l)){
	 * aglet.itinerary.addPlan(address.getAddress(),
	 * choice.getSelectedItem());
	 * update();
	 * }
	 * return true;
	 * } else if (ev.target instanceof java.awt.Checkbox) {
	 * aglet.itinerary.setRepeat(check.getState());
	 * }
	 * return false;
	 * }
	 */
	private void update() {
		list.removeAll();
		SeqPlanItinerary spi = aglet.itinerary;
		int size = spi.size();

		for (int i = 0; i < size; i++) {
			String s = spi.getAddressAt(i) + " : " 
					   + spi.getMessageAt(i).getKind();

			list.add(s);
		} 
		check.setState(aglet.itinerary.isRepeat());
	}
	public void windowActivated(WindowEvent we) {}
	public void windowClosed(WindowEvent we) {}
	/**
	 * Handles the window event
	 * @param we the event to be handled
	 */

	public void windowClosing(WindowEvent we) {
		dispose();
	}
	public void windowDeactivated(WindowEvent we) {}
	public void windowDeiconified(WindowEvent we) {}
	public void windowIconified(WindowEvent we) {}
	public void windowOpened(WindowEvent we) {}
}

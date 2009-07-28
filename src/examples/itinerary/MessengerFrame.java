package examples.itinerary;

/*
 * @(#)MessengerAglet.java
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
import com.ibm.aglet.system.*;
import com.ibm.agletx.util.MessengerItinerary;
import com.ibm.aglet.util.*;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.*;
import java.awt.event.*;

class MessengerFrame extends Frame implements WindowListener, ActionListener {
	MessengerAglet aglet;
	List list = new List(10, false);
	AddressChooser address = new AddressChooser(15);
	Choice choice = new Choice();

	MessengerFrame(MessengerAglet a) {
		aglet = a;

		addWindowListener(this);
		setLayout(new BorderLayout());
		add("Center", list);

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
		Button start = new Button("Start!");

		start.addActionListener(this);
		p.add(start);
		add("South", p);

		choice.addItem("dispose");
		update();
	}
	/**
	 * Handles the action event
	 * @param ae the event to be handled
	 */
	public void actionPerformed(ActionEvent ae) {
		if ("Start!".equals(ae.getActionCommand())) {
			if (aglet.itinerary == null) {
				aglet.itinerary = 
					new MessengerItinerary(aglet, choice.getSelectedItem());
			} 
			aglet.start();
		} else if ("Remove".equals(ae.getActionCommand())) {
			int i = list.getSelectedIndex();

			if (i >= 0) {
				aglet.addresses.removeElementAt(i);
				list.remove(i);
			} 
		} else if ("Add".equals(ae.getActionCommand())) {
			aglet.addresses.addElement(address.getAddress());
			update();
		} 

	}
	/*
	 * public boolean handleEvent(Event ev) {
	 * if (ev.id == Event.WINDOW_DESTROY) {
	 * dispose();
	 * return true;
	 * }
	 * return super.handleEvent(ev);
	 * }
	 * 
	 * public boolean action(Event ev, Object obj) {
	 * if (ev.target instanceof Button) {
	 * Button b = (Button)ev.target;
	 * String l = b.getLabel();
	 * if ("Start!".equals(l)) {
	 * if (aglet.itinerary==null) {
	 * aglet.itinerary = new MessengerItinerary(aglet,choice.getSelectedItem());
	 * }
	 * aglet.start();
	 * } else if ("Remove".equals(l)) {
	 * int i = list.getSelectedIndex();
	 * if (i>=0) {
	 * aglet.removeURL(i);
	 * list.delItem(i);
	 * }
	 * } else if ("Add".equals(l)){
	 * aglet.addURL(ac.getAddress());
	 * update();
	 * }
	 * return true;
	 * }
	 * return false;
	 * }
	 */

	private void update() {
		list.removeAll();
		Vector addrs = aglet.addresses;
		int size = addrs.size();

		for (int i = 0; i < size; i++) {
			list.add((String)addrs.elementAt(i));
		} 
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

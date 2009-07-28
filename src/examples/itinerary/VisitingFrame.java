package examples.itinerary;

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

import com.ibm.aglet.*;
import com.ibm.aglet.system.*;
import com.ibm.aglet.util.*;
import com.ibm.agletx.util.*;
import com.ibm.agletx.patterns.Meeting;
import java.util.Vector;
import java.util.Enumeration;
import java.net.URL;
import java.awt.*;
import java.awt.event.*;

class VisitingFrame extends Frame implements WindowListener, ActionListener {
	VisitingAglet aglet;
	List list = new List(10, false);
	AddressChooser address = new AddressChooser(15);

	VisitingFrame(VisitingAglet a) {
		aglet = a;

		addWindowListener(this);

		setLayout(new BorderLayout());
		add("Center", list);

		Panel p = new Panel();

		p.setLayout(new FlowLayout());
		p.add(address);
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

		update();
	}
	/**
	 * Handles the action event
	 * @param ae the event to be handled
	 */
	public void actionPerformed(ActionEvent ae) {
		if ("Add".equals(ae.getActionCommand())) {
			aglet.addresses.addElement(address.getAddress());
			update();
		} else if ("Remove".equals(ae.getActionCommand())) {
			int i = list.getSelectedIndex();

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

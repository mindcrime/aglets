package examples.simplemasterslave;

/*
 * @(#)CommandWindow.java
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
import com.ibm.aglet.event.*;
import com.ibm.aglet.util.*;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;

class CommandWindow extends Frame implements ActionListener {
	AgletProxy ap = null;
	AddressChooser ac = null;
	java.awt.List list = null;
	Button addbutton = null, removebutton = null, gobutton = null;

	CommandWindow(AgletProxy ap) {
		super("Simple Master_Slave Pattern Sample");
		this.ap = ap;
		setUp();
	}
	// handle action event
	public void actionPerformed(ActionEvent ae) {
		try {
			if ("go".equals(ae.getActionCommand())) {
				ap.sendMessage(new Message("go"));
			} else if ("add".equals(ae.getActionCommand())) {
				ap.sendMessage(new Message("add", new URL(ac.getAddress())));
				update();
			} else if ("remove".equals(ae.getActionCommand())) {
				int i = list.getSelectedIndex();

				if (i >= 0) {
					ap.sendMessage(new Message("remove", i));
					list.remove(i);
				} 
			} 
		} catch (Exception e) {
			System.out.println("Error:" + e.getMessage());
		} 
	}
	public void setUp() {

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			} 
		});

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		setLayout(gridbag);
		c.fill = GridBagConstraints.BOTH;

		ac = new AddressChooser(15);
		c.weightx = 1.0;
		gridbag.setConstraints(ac, c);
		add(ac);

		addbutton = new Button("add");
		addbutton.addActionListener(this);

		removebutton = new Button("remove");
		removebutton.addActionListener(this);

		gobutton = new Button("go");
		gobutton.addActionListener(this);

		Panel bp = new Panel(new GridLayout(1, 3));

		bp.add(addbutton);
		bp.add(removebutton);
		bp.add(gobutton);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.0;
		gridbag.setConstraints(bp, c);
		add(bp);

		list = new java.awt.List(10, false);
		c.weighty = 1.0;
		gridbag.setConstraints(list, c);
		add(list);

		setSize(500, 200);
	}
	private void update() {
		list.removeAll();
		try {
			Vector addrs = (Vector)ap.sendMessage(new Message("getlist"));
			int size = addrs.size();

			for (int i = 0; i < size; i++) {
				list.add((addrs.elementAt(i)).toString());
			}
		} catch (Exception e) {
			System.out.println("Error:" + e.getMessage());
		} 
	}
}

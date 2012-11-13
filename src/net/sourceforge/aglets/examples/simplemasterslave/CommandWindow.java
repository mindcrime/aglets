package net.sourceforge.aglets.examples.simplemasterslave;

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

import java.awt.Button;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.Vector;

import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.util.AddressChooser;

class CommandWindow extends Frame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5621520180358200272L;
	AgletProxy ap = null;
	AddressChooser ac = null;
	java.awt.List list = null;
	Button addbutton = null, removebutton = null, gobutton = null;

	CommandWindow(final AgletProxy ap) {
		super("Simple Master_Slave Pattern Sample");
		this.ap = ap;
		setUp();
	}

	// handle action event
	@Override
	public void actionPerformed(final ActionEvent ae) {
		try {
			if ("go".equals(ae.getActionCommand())) {
				ap.sendMessage(new Message("go"));
			} else if ("add".equals(ae.getActionCommand())) {
				ap.sendMessage(new Message("add", new URL(ac.getAddress())));
				this.update();
			} else if ("remove".equals(ae.getActionCommand())) {
				final int i = list.getSelectedIndex();

				if (i >= 0) {
					ap.sendMessage(new Message("remove", i));
					list.remove(i);
				}
			}
		} catch (final Exception e) {
			System.out.println("Error:" + e.getMessage());
		}
	}

	public void setUp() {

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				CommandWindow.this.setVisible(false);
			}
		});

		final GridBagLayout gridbag = new GridBagLayout();
		final GridBagConstraints c = new GridBagConstraints();

		setLayout(gridbag);
		c.fill = GridBagConstraints.BOTH;

		ac = new AddressChooser(15);
		c.weightx = 1.0;
		gridbag.setConstraints(ac, c);
		this.add(ac);

		addbutton = new Button("add");
		addbutton.addActionListener(this);

		removebutton = new Button("remove");
		removebutton.addActionListener(this);

		gobutton = new Button("go");
		gobutton.addActionListener(this);

		final Panel bp = new Panel(new GridLayout(1, 3));

		bp.add(addbutton);
		bp.add(removebutton);
		bp.add(gobutton);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.0;
		gridbag.setConstraints(bp, c);
		this.add(bp);

		list = new java.awt.List(10, false);
		c.weighty = 1.0;
		gridbag.setConstraints(list, c);
		this.add(list);

		this.setSize(500, 200);
	}

	private void update() {
		list.removeAll();
		try {
			final Vector addrs = (Vector) ap.sendMessage(new Message("getlist"));
			final int size = addrs.size();

			for (int i = 0; i < size; i++) {
				list.add((addrs.elementAt(i)).toString());
			}
		} catch (final Exception e) {
			System.out.println("Error:" + e.getMessage());
		}
	}
}

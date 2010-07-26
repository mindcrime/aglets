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
    AgletProxy ap = null;
    AddressChooser ac = null;
    java.awt.List list = null;
    Button addbutton = null, removebutton = null, gobutton = null;

    CommandWindow(AgletProxy ap) {
	super("Simple Master_Slave Pattern Sample");
	this.ap = ap;
	this.setUp();
    }

    // handle action event
    public void actionPerformed(ActionEvent ae) {
	try {
	    if ("go".equals(ae.getActionCommand())) {
		this.ap.sendMessage(new Message("go"));
	    } else if ("add".equals(ae.getActionCommand())) {
		this.ap.sendMessage(new Message("add", new URL(this.ac.getAddress())));
		this.update();
	    } else if ("remove".equals(ae.getActionCommand())) {
		int i = this.list.getSelectedIndex();

		if (i >= 0) {
		    this.ap.sendMessage(new Message("remove", i));
		    this.list.remove(i);
		}
	    }
	} catch (Exception e) {
	    System.out.println("Error:" + e.getMessage());
	}
    }

    public void setUp() {

	this.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent e) {
		CommandWindow.this.setVisible(false);
	    }
	});

	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();

	this.setLayout(gridbag);
	c.fill = GridBagConstraints.BOTH;

	this.ac = new AddressChooser(15);
	c.weightx = 1.0;
	gridbag.setConstraints(this.ac, c);
	this.add(this.ac);

	this.addbutton = new Button("add");
	this.addbutton.addActionListener(this);

	this.removebutton = new Button("remove");
	this.removebutton.addActionListener(this);

	this.gobutton = new Button("go");
	this.gobutton.addActionListener(this);

	Panel bp = new Panel(new GridLayout(1, 3));

	bp.add(this.addbutton);
	bp.add(this.removebutton);
	bp.add(this.gobutton);

	c.gridwidth = GridBagConstraints.REMAINDER;
	c.weightx = 0.0;
	gridbag.setConstraints(bp, c);
	this.add(bp);

	this.list = new java.awt.List(10, false);
	c.weighty = 1.0;
	gridbag.setConstraints(this.list, c);
	this.add(this.list);

	this.setSize(500, 200);
    }

    private void update() {
	this.list.removeAll();
	try {
	    Vector addrs = (Vector) this.ap.sendMessage(new Message("getlist"));
	    int size = addrs.size();

	    for (int i = 0; i < size; i++) {
		this.list.add((addrs.elementAt(i)).toString());
	    }
	} catch (Exception e) {
	    System.out.println("Error:" + e.getMessage());
	}
    }
}
